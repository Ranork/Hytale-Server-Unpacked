package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.builtin.hytalegenerator.EntityPlacementData;
import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.FluidMaterial;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.material.SolidMaterial;
import com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.prefab.PrefabPropUtil;
import com.hypixel.hytale.builtin.hytalegenerator.rng.RngField;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.common.util.ExceptionUtil;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferCall;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

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
   private final int prefabId;
   @Nonnull
   private final Vector3i rPrefabPosition;
   @Nonnull
   private final PrefabProp.IntersectingColumnPredicate<PrefabBufferCall> rColumnPredicate;
   @Nonnull
   private final Vector3i rWorldPosition;
   @Nonnull
   private final Vector3d rEntityWorldPosition;

   public PrefabProp(@Nonnull WeightedMap<List<IPrefabBuffer>> prefabPool, @Nonnull MaterialCache materialCache, @Nonnull SeedBox seedBox) {
      this.materialCache = materialCache;
      this.rngField = new RngField(seedBox.createSupplier().get());
      this.random = new FastRandom();
      this.prefabPool = new WeightedMap<>();
      this.writeBounds = new Bounds3i();
      prefabPool.forEach((sourceList, weight) -> {
         if (!sourceList.isEmpty()) {
            List<IPrefabBuffer> prefabList = new ArrayList<>();

            for (IPrefabBuffer prefab : sourceList) {
               assert prefab != null;

               if (prefab == null) {
                  return;
               }

               prefabList.add(prefab);
               this.writeBounds.encompass(getWriteBounds(prefab));
            }

            this.prefabPool.add(prefabList, weight);
         }
      });
      this.prefabId = this.hashCode();
      this.rPrefabPosition = new Vector3i();
      this.rColumnPredicate = new PrefabProp.IntersectingColumnPredicate<>();
      this.rWorldPosition = new Vector3i();
      this.rEntityWorldPosition = new Vector3d();
   }

   @Override
   public boolean generate(@NonNullDecl Prop.Context context) {
      if (this.prefabPool.size() == 0) {
         return true;
      } else {
         this.random.setSeed(this.rngField.get(context.position.x, context.position.y, context.position.z));
         PrefabBufferCall callInstance = new PrefabBufferCall(this.random, PrefabRotation.ROTATION_0);
         IPrefabBuffer prefab = this.pickPrefab(this.random);
         this.rPrefabPosition.assign(context.position);
         this.rColumnPredicate.bounds.assign(context.materialWriteSpace.getBounds());
         this.rColumnPredicate.bounds.offsetOpposite(context.position);

         try {
            prefab.forEach(
               this.rColumnPredicate,
               (x, y, z, blockId, holder, support, rotation, filler, call, fluidId, fluidLevel) -> {
                  this.rWorldPosition.assign(x + context.position.x, y + context.position.y, z + context.position.z);
                  if (context.materialWriteSpace.getBounds().contains(this.rWorldPosition)) {
                     SolidMaterial solid = this.materialCache.getSolidMaterial(blockId, support, rotation, filler, holder != null ? holder.clone() : null);
                     FluidMaterial fluid = this.materialCache.getFluidMaterial(fluidId, (byte)fluidLevel);
                     Material material = this.materialCache.getMaterial(solid, fluid);
                     if (filler == 0) {
                        context.materialWriteSpace.set(material, this.rWorldPosition);
                     }
                  }
               },
               (cx, cz, entityWrappers, buffer) -> {
                  if (entityWrappers != null) {
                     for (int i = 0; i < entityWrappers.length; i++) {
                        TransformComponent transformComp = entityWrappers[i].getComponent(TransformComponent.getComponentType());
                        if (transformComp != null) {
                           buffer.rotation.rotate(transformComp.getPosition());
                           if (context.entityWriteBuffer.getBounds().contains(this.rEntityWorldPosition)
                              && context.materialWriteSpace.getBounds().contains(this.rEntityWorldPosition)) {
                              Holder<EntityStore> entityClone = entityWrappers[i].clone();
                              transformComp = entityClone.getComponent(TransformComponent.getComponentType());
                              if (transformComp != null) {
                                 transformComp.getPosition().assign(this.rEntityWorldPosition);
                                 EntityPlacementData placementData = new EntityPlacementData(
                                    new Vector3i(), PrefabRotation.ROTATION_0, entityClone, this.prefabId
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
         } catch (Exception var6) {
            String msg = "Couldn't place prefab prop.";
            msg = msg + "\n";
            msg = msg + ExceptionUtil.toStringWithStack(var6);
            ((HytaleLogger.Api)HytaleLogger.getLogger().atWarning()).log(msg);
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
      Vector3i max = PrefabPropUtil.getMax(prefab, PrefabRotation.ROTATION_0);
      max.add(1, 1, 1);
      Vector3i min = PrefabPropUtil.getMin(prefab, PrefabRotation.ROTATION_0);
      return new Bounds3i(min, max);
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
