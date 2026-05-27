package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.builtin.hytalegenerator.BlockMask;
import com.hypixel.hytale.builtin.hytalegenerator.EntityPlacementData;
import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.FluidMaterial;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.material.SolidMaterial;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.ConstantPattern;
import com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.directionality.RotatedPosition;
import com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.directionality.StaticDirectionality;
import com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.prefab.PrefabMoldingConfiguration;
import com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.prefab.PrefabPropUtil;
import com.hypixel.hytale.builtin.hytalegenerator.rng.RngField;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.DirectScanner;
import com.hypixel.hytale.common.util.ExceptionUtil;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferCall;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBuffer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.joml.Vector3d;
import org.joml.Vector3i;

public class PrefabProp extends Prop {
   @Nonnull
   private final Bounds3i writeBounds;
   @Nonnull
   private final WeightedMap<List<IPrefabBuffer>> prefabPool;
   @Nonnull
   private final MaterialCache materialCache;
   @Nonnull
   private final RngField rngField;
   @Nonnull
   private final FastRandom random;
   @Nonnull
   private final List<com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.prefab.PrefabProp> childProps;
   @Nonnull
   private final List<RotatedPosition> childPositions;
   @Nonnull
   private final Vector3i rPrefabPosition;
   @Nonnull
   private final PrefabProp.IntersectingColumnPredicate<PrefabBufferCall> rColumnPredicate;
   @Nonnull
   private final Vector3i rWorldPosition;
   @Nonnull
   private final Vector3d rEntityWorldPosition;
   @Nonnull
   private final RotatedPosition rRotatedWorldPosition;

   public PrefabProp(
      @Nonnull WeightedMap<List<IPrefabBuffer>> prefabPool,
      @Nonnull MaterialCache materialCache,
      @Nonnull SeedBox seedBox,
      @Nullable Function<String, List<IPrefabBuffer>> childPrefabLoader
   ) {
      this.materialCache = materialCache;
      this.rngField = new RngField(seedBox.createSupplier().get());
      this.random = new FastRandom();
      this.childProps = new ArrayList<>(0);
      this.childPositions = new ArrayList<>(0);
      this.prefabPool = new WeightedMap<>();
      this.writeBounds = new Bounds3i();
      prefabPool.forEach(
         (sourceList, weight) -> {
            if (!sourceList.isEmpty()) {
               List<IPrefabBuffer> prefabList = new ArrayList<>();

               for (IPrefabBuffer prefab : sourceList) {
                  assert prefab != null;

                  if (prefab == null) {
                     return;
                  }

                  prefabList.add(prefab);
                  this.writeBounds.encompass(getWriteBounds(prefab));
                  PrefabBuffer.ChildPrefab[] childPrefabs = prefab.getChildPrefabs();
                  int childId = 0;

                  for (PrefabBuffer.ChildPrefab child : childPrefabs) {
                     RotatedPosition childPosition = new RotatedPosition(child.getX(), child.getY(), child.getZ(), child.getRotation());
                     String childPath = child.getPath().replace('.', '/');
                     childPath = childPath.replace("*", "");
                     List<IPrefabBuffer> childPrefabBuffers = childPrefabLoader.apply(childPath);
                     WeightedMap<List<IPrefabBuffer>> weightedChildPrefabs = new WeightedMap<>();
                     weightedChildPrefabs.add(childPrefabBuffers, 1.0);
                     StaticDirectionality childDirectionality = new StaticDirectionality(child.getRotation(), ConstantPattern.INSTANCE_TRUE);
                     com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.prefab.PrefabProp childProp = new com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.prefab.PrefabProp(
                        weightedChildPrefabs,
                        new DirectScanner(),
                        childDirectionality,
                        materialCache,
                        new BlockMask(),
                        PrefabMoldingConfiguration.none(),
                        childPrefabLoader,
                        seedBox.child(String.valueOf(childId++)),
                        true
                     );
                     this.childProps.add(childProp);
                     this.childPositions.add(childPosition);
                     Bounds3i localChildBounds = childProp.getWriteBounds_voxelGrid().clone();
                     localChildBounds.offset(childPosition.x, childPosition.y, childPosition.z);
                     RotationTuple prefabRotationTuple = RotationTuple.of(childPosition.rotation.getRotation(), Rotation.None, Rotation.None);
                     localChildBounds.applyRotationAroundVoxel(prefabRotationTuple, Vector3iUtil.ZERO);
                     this.writeBounds.encompass(localChildBounds);
                  }
               }

               this.prefabPool.add(prefabList, weight);
            }
         }
      );
      this.rPrefabPosition = new Vector3i();
      this.rColumnPredicate = new PrefabProp.IntersectingColumnPredicate<>();
      this.rWorldPosition = new Vector3i();
      this.rEntityWorldPosition = new Vector3d();
      this.rRotatedWorldPosition = new RotatedPosition(0, 0, 0, PrefabRotation.ROTATION_0);
   }

   @Override
   public boolean generate(@NonNullDecl Prop.Context context) {
      if (this.prefabPool.size() == 0) {
         return true;
      } else {
         this.random.setSeed(this.rngField.get(context.position.x, context.position.y, context.position.z));
         PrefabBufferCall callInstance = new PrefabBufferCall(this.random, PrefabRotation.ROTATION_0);
         IPrefabBuffer prefab = this.pickPrefab(this.random);
         this.rPrefabPosition.set(context.position);
         this.rColumnPredicate.bounds.assign(context.materialWriteSpace.getBounds());
         this.rColumnPredicate.bounds.offsetOpposite(context.position);
         int prefabInstanceId = Objects.hash(context.position.x, context.position.y, context.position.z, prefab.hashCode());

         try {
            prefab.forEach(
               this.rColumnPredicate,
               (x, y, z, blockId, holder, support, rotation, filler, call, fluidId, fluidLevel) -> {
                  this.rWorldPosition.set(x + context.position.x, y + context.position.y, z + context.position.z);
                  if (context.materialWriteSpace.getBounds().contains(this.rWorldPosition)) {
                     SolidMaterial solid = this.materialCache.getSolidMaterial(blockId, support, rotation, filler, holder != null ? holder.clone() : null);
                     FluidMaterial fluid = this.materialCache.getFluidMaterial(fluidId, (byte)fluidLevel);
                     Material material = this.materialCache.getMaterial(solid, fluid);
                     context.materialWriteSpace.set(material, this.rWorldPosition);
                  }
               },
               (cx, cz, entityWrappers, buffer) -> {
                  if (entityWrappers != null) {
                     for (int ix = 0; ix < entityWrappers.length; ix++) {
                        TransformComponent transformComp = entityWrappers[ix].getComponent(TransformComponent.getComponentType());
                        if (transformComp != null) {
                           Vector3d localPosition = new Vector3d(transformComp.getPosition());
                           buffer.rotation.rotate(localPosition);
                           this.rEntityWorldPosition.set(localPosition).add(context.position.x, context.position.y, context.position.z);
                           if (context.entityWriteBuffer.getBounds().contains(this.rEntityWorldPosition)
                              && context.materialWriteSpace.getBounds().contains(this.rEntityWorldPosition)) {
                              Holder<EntityStore> entityClone = entityWrappers[ix].clone();
                              transformComp = entityClone.getComponent(TransformComponent.getComponentType());
                              if (transformComp != null) {
                                 transformComp.getPosition().set(this.rEntityWorldPosition);
                                 EntityPlacementData placementData = new EntityPlacementData(
                                    new Vector3i(), PrefabRotation.ROTATION_0, entityClone, prefabInstanceId
                                 );
                                 context.entityWriteBuffer.addEntity(placementData);
                              }
                           }
                        }
                     }
                  }
               },
               (x, y, z, path, fitHeightmap, inheritSeed, inheritHeightCondition, weights, rotation, t) -> {},
               callInstance
            );
         } catch (Exception var9) {
            String msg = "Couldn't place prefab prop.";
            msg = msg + "\n";
            msg = msg + ExceptionUtil.toStringWithStack(var9);
            ((HytaleLogger.Api)HytaleLogger.getLogger().atWarning()).log(msg);
         }

         this.rRotatedWorldPosition.x = context.position.x;
         this.rRotatedWorldPosition.y = context.position.y;
         this.rRotatedWorldPosition.z = context.position.z;

         for (int i = 0; i < this.childProps.size(); i++) {
            com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.prefab.PrefabProp prop = this.childProps.get(i);
            RotatedPosition childPosition = this.childPositions.get(i).getRelativeTo(this.rRotatedWorldPosition);
            Vector3i rotatedChildPositionVec = new Vector3i(childPosition.x, childPosition.y, childPosition.z);
            this.rRotatedWorldPosition.rotation.rotate(rotatedChildPositionVec);
            prop.place(childPosition, context.materialWriteSpace, context.entityWriteBuffer);
         }

         return true;
      }
   }

   @Nonnull
   private IPrefabBuffer pickPrefab(@Nonnull Random rand) {
      List<IPrefabBuffer> list = this.prefabPool.pick(rand);
      int randomIndex = rand.nextInt(list.size());
      return list.get(randomIndex);
   }

   @Nonnull
   private static Bounds3i getWriteBounds(@Nonnull IPrefabBuffer prefab) {
      Bounds3i bounds = PrefabPropUtil.getTotalBounds(prefab);
      bounds.max.add(1, 1, 1);
      return bounds;
   }

   @NonNullDecl
   @Override
   public Bounds3i getReadBounds_voxelGrid() {
      return Bounds3i.ZERO;
   }

   @NonNullDecl
   @Override
   public Bounds3i getWriteBounds_voxelGrid() {
      return this.writeBounds;
   }

   private static class IntersectingColumnPredicate<T> implements IPrefabBuffer.ColumnPredicate<T> {
      public Bounds3i bounds = new Bounds3i();

      public IntersectingColumnPredicate() {
      }

      @Override
      public boolean test(int x, int z, int blocks, T o) {
         return x >= this.bounds.min.x && x < this.bounds.max.x && z >= this.bounds.min.z && z < this.bounds.max.z;
      }
   }
}
