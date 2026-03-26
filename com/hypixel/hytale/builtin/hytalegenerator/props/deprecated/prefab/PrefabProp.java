package com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.prefab;

import com.hypixel.hytale.builtin.hytalegenerator.BlockMask;
import com.hypixel.hytale.builtin.hytalegenerator.EntityPlacementData;
import com.hypixel.hytale.builtin.hytalegenerator.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.engine.entityfunnel.EntityFunnel;
import com.hypixel.hytale.builtin.hytalegenerator.material.FluidMaterial;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.material.SolidMaterial;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.ConstantPattern;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.ScanResult;
import com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.directionality.Directionality;
import com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.directionality.RotatedPosition;
import com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.directionality.RotatedPositionsScanResult;
import com.hypixel.hytale.builtin.hytalegenerator.props.deprecated.directionality.StaticDirectionality;
import com.hypixel.hytale.builtin.hytalegenerator.rng.RngField;
import com.hypixel.hytale.builtin.hytalegenerator.rng.SeedBox;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.DirectScanner;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.ArrayVoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.VoxelSpace;
import com.hypixel.hytale.common.util.ExceptionUtil;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.prefab.PrefabRotation;
import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferCall;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBuffer;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class PrefabProp extends Prop {
   @Nonnull
   private final WeightedMap<List<IPrefabBuffer>> prefabPool;
   @Nonnull
   private final Scanner scanner;
   @Nonnull
   private final MaterialCache materialCache;
   @Nonnull
   private final RngField rngField;
   @Nonnull
   private final BlockMask materialMask;
   @Nonnull
   private final Directionality directionality;
   @Nonnull
   private final Bounds3i readBounds_voxelGrid;
   @Nonnull
   private final Bounds3i writeBounds_voxelGrid;
   @Nonnull
   private final List<PrefabProp> childProps;
   @Nonnull
   private final List<RotatedPosition> childPositions;
   @Nonnull
   private final Function<String, List<IPrefabBuffer>> childPrefabLoader;
   private final Scanner moldingScanner;
   private final Pattern moldingPattern;
   private final MoldingDirection moldingDirection;
   private final boolean moldChildren;
   private final int prefabId = this.hashCode();
   private boolean loadEntities;

   public PrefabProp(
      @Nonnull WeightedMap<List<IPrefabBuffer>> prefabPool,
      @Nonnull Scanner scanner,
      @Nonnull Directionality directionality,
      @Nonnull MaterialCache materialCache,
      @Nonnull BlockMask materialMask,
      @Nonnull PrefabMoldingConfiguration prefabMoldingConfiguration,
      @Nullable Function<String, List<IPrefabBuffer>> childPrefabLoader,
      @Nonnull SeedBox seedBox,
      boolean loadEntities
   ) {
      this.prefabPool = prefabPool;
      this.scanner = scanner;
      this.directionality = directionality;
      this.materialCache = materialCache;
      this.rngField = new RngField(seedBox.createSupplier().get());
      this.materialMask = materialMask;
      this.loadEntities = loadEntities;
      this.childProps = new ArrayList<>();
      this.childPositions = new ArrayList<>();
      this.childPrefabLoader = childPrefabLoader == null ? s -> null : childPrefabLoader;
      this.moldingScanner = prefabMoldingConfiguration.moldingScanner;
      this.moldingPattern = prefabMoldingConfiguration.moldingPattern;
      this.moldingDirection = prefabMoldingConfiguration.moldingDirection;
      this.moldChildren = prefabMoldingConfiguration.moldChildren;
      this.readBounds_voxelGrid = directionality.getBoundsWith_voxelGrid(scanner);
      this.writeBounds_voxelGrid = new Bounds3i();

      for (List<IPrefabBuffer> prefabList : prefabPool.allElements()) {
         if (prefabList.isEmpty()) {
            throw new IllegalArgumentException("prefab pool contains empty list");
         }

         for (IPrefabBuffer prefab : prefabList) {
            if (prefab == null) {
               throw new IllegalArgumentException("prefab pool contains list with null element");
            }

            this.writeBounds_voxelGrid.encompass(this.getWriteBounds_voxelGrid(prefab));
            PrefabBuffer.ChildPrefab[] childPrefabs = prefab.getChildPrefabs();
            int childId = 0;

            for (PrefabBuffer.ChildPrefab child : childPrefabs) {
               RotatedPosition childPosition = new RotatedPosition(child.getX(), child.getY(), child.getZ(), child.getRotation());
               String childPath = child.getPath().replace('.', '/');
               childPath = childPath.replace("*", "");
               List<IPrefabBuffer> childPrefabBuffers = this.childPrefabLoader.apply(childPath);
               WeightedMap<List<IPrefabBuffer>> weightedChildPrefabs = new WeightedMap<>();
               weightedChildPrefabs.add(childPrefabBuffers, 1.0);
               StaticDirectionality childDirectionality = new StaticDirectionality(child.getRotation(), ConstantPattern.INSTANCE_TRUE);
               PrefabProp childProp = new PrefabProp(
                  weightedChildPrefabs,
                  new DirectScanner(),
                  childDirectionality,
                  materialCache,
                  materialMask,
                  this.moldChildren ? prefabMoldingConfiguration : PrefabMoldingConfiguration.none(),
                  childPrefabLoader,
                  seedBox.child(String.valueOf(childId++)),
                  loadEntities
               );
               this.childProps.add(childProp);
               this.childPositions.add(childPosition);
            }

            for (int i = 0; i < this.childPositions.size(); i++) {
               PrefabProp child = this.childProps.get(i);
               Vector3i childPosition_voxelGrid = this.childPositions.get(i).toVector3i();
               Bounds3i childWriteBounds_voxelGrid = child.getWriteBounds_voxelGrid().clone();
               childWriteBounds_voxelGrid.offset(childPosition_voxelGrid);
               this.writeBounds_voxelGrid.encompass(childWriteBounds_voxelGrid);
            }
         }
      }

      this.writeBounds_voxelGrid.stack(scanner.getBounds_voxelGrid());
   }

   @Nonnull
   private Bounds3i getWriteBounds_voxelGrid(@Nonnull IPrefabBuffer prefabAccess) {
      Bounds3i bounds_voxelGrid = new Bounds3i();

      for (PrefabRotation rotation : this.directionality.getPossibleRotations()) {
         Vector3i max = PrefabPropUtil.getMax(prefabAccess, rotation);
         max.add(1, 1, 1);
         Vector3i min = PrefabPropUtil.getMin(prefabAccess, rotation);
         bounds_voxelGrid.encompass(min);
         bounds_voxelGrid.encompass(max);
      }

      return bounds_voxelGrid;
   }

   @Override
   public boolean generate(@NonNullDecl Prop.Context context) {
      ScanResult scanResult = this.scan_deprecated(context.position, context.materialReadSpace);
      this.place_deprecated(context, scanResult);
      return !scanResult.isNegative();
   }

   @Nonnull
   public ScanResult scan_deprecated(@Nonnull Vector3i position, @Nonnull VoxelSpace<Material> materialSpace) {
      List<Vector3i> validPositions = new ArrayList<>();
      Scanner.Context scannerContext = new Scanner.Context(position, this.directionality.getGeneralPattern(), materialSpace, validPositions);
      this.scanner.scan(scannerContext);
      Vector3i patternPosition = new Vector3i();
      Pattern.Context patternContext = new Pattern.Context(patternPosition, materialSpace);
      RotatedPositionsScanResult scanResult = new RotatedPositionsScanResult(new ArrayList<>());

      for (Vector3i validPosition : validPositions) {
         patternPosition.assign(validPosition);
         PrefabRotation rotation = this.directionality.getRotationAt(patternContext);
         if (rotation != null) {
            scanResult.positions.add(new RotatedPosition(validPosition.x, validPosition.y, validPosition.z, rotation));
         }
      }

      return scanResult;
   }

   public void place_deprecated(@Nonnull Prop.Context context, @Nonnull ScanResult scanResult) {
      if (this.prefabPool.size() != 0) {
         List<RotatedPosition> positions = RotatedPositionsScanResult.cast(scanResult).positions;
         if (positions != null) {
            Bounds3i localWriteBounds_voxelGrid = this.writeBounds_voxelGrid.clone().offset(context.position);
            if (localWriteBounds_voxelGrid.intersects(localWriteBounds_voxelGrid)) {
               Bounds3i writeSpaceBounds_voxelGrid = context.materialWriteSpace.getBounds();

               for (RotatedPosition position : positions) {
                  this.place(position, context.materialWriteSpace, context.entityWriteBuffer);
               }
            }
         }
      }
   }

   private IPrefabBuffer pickPrefab(@Nonnull Random rand) {
      List<IPrefabBuffer> list = this.prefabPool.pick(rand);
      int randomIndex = rand.nextInt(list.size());
      return list.get(randomIndex);
   }

   private void place(@Nonnull RotatedPosition position, @Nonnull VoxelSpace<Material> materialSpace, @Nonnull EntityFunnel entityBuffer) {
      Random random = new Random(this.rngField.get(position.x, position.y, position.z));
      PrefabBufferCall callInstance = new PrefabBufferCall(random, position.rotation);
      IPrefabBuffer prefab = this.pickPrefab(random);
      VoxelSpace<Integer> moldingOffsets = null;
      if (this.moldingDirection != MoldingDirection.NONE) {
         int prefabMinX = prefab.getMinX(position.rotation);
         int prefabMinZ = prefab.getMinZ(position.rotation);
         int prefabMaxX = prefab.getMaxX(position.rotation);
         int prefabMaxZ = prefab.getMaxZ(position.rotation);
         Bounds3i bounds_voxelGrid = new Bounds3i(
            new Vector3i(prefabMinX + position.x, 0, prefabMinZ + position.z), new Vector3i(prefabMaxX + position.x, 1, prefabMaxZ + position.z)
         );
         moldingOffsets = new ArrayVoxelSpace<>(bounds_voxelGrid);
         if (this.moldingDirection == MoldingDirection.DOWN || this.moldingDirection == MoldingDirection.UP) {
            Vector3i pointer = new Vector3i(0, position.y, 0);
            Scanner.Context scannerContext = new Scanner.Context(pointer, this.moldingPattern, materialSpace, new ArrayList<>());

            for (pointer.x = bounds_voxelGrid.min.x; pointer.x < bounds_voxelGrid.max.x; pointer.x++) {
               for (pointer.z = bounds_voxelGrid.min.z; pointer.z < bounds_voxelGrid.max.z; pointer.z++) {
                  scannerContext.validPositions_out.clear();
                  this.moldingScanner.scan(scannerContext);
                  Integer offset = scannerContext.validPositions_out.isEmpty() ? null : scannerContext.validPositions_out.getFirst().y - position.y;
                  if (offset != null && this.moldingDirection == MoldingDirection.UP) {
                     offset = offset - 1;
                  }

                  moldingOffsets.set(offset, pointer.x, 0, pointer.z);
               }
            }
         }
      }

      try {
         Vector3i prefabPositionVector = position.toVector3i();
         VoxelSpace<Integer> moldingOffsetsFinal = moldingOffsets;
         prefab.forEach(
            IPrefabBuffer.iterateAllColumns(),
            (x, yx, z, blockId, holder, support, rotation, filler, call, fluidId, fluidLevel) -> {
               int worldX = position.x + x;
               int worldY = position.y + yx;
               int worldZ = position.z + z;
               if (materialSpace.getBounds().contains(worldX, worldY, worldZ)) {
                  SolidMaterial solid = this.materialCache.getSolidMaterial(blockId, support, rotation, filler, holder != null ? holder.clone() : null);
                  FluidMaterial fluid = this.materialCache.getFluidMaterial(fluidId, (byte)fluidLevel);
                  Material material = this.materialCache.getMaterial(solid, fluid);
                  int materialHash = material.hashMaterialIds();
                  if (this.materialMask.canPlace(materialHash)) {
                     if (this.moldingDirection == MoldingDirection.DOWN || this.moldingDirection == MoldingDirection.UP) {
                        Integer offsetx = null;
                        if (moldingOffsetsFinal.getBounds().contains(worldX, 0, worldZ)) {
                           offsetx = moldingOffsetsFinal.get(worldX, 0, worldZ);
                        }

                        if (offsetx == null) {
                           return;
                        }

                        worldY += offsetx;
                     }

                     Material worldMaterial = materialSpace.get(worldX, worldY, worldZ);
                     int worldMaterialHash = worldMaterial.hashMaterialIds();
                     if (this.materialMask.canReplace(materialHash, worldMaterialHash)) {
                        if (filler == 0) {
                           materialSpace.set(material, worldX, worldY, worldZ);
                        }
                     }
                  }
               }
            },
            (cx, cz, entityWrappers, buffer) -> {
               if (this.loadEntities) {
                  if (entityWrappers != null) {
                     for (int ix = 0; ix < entityWrappers.length; ix++) {
                        TransformComponent transformComp = entityWrappers[ix].getComponent(TransformComponent.getComponentType());
                        if (transformComp != null) {
                           Vector3d entityPosition = transformComp.getPosition().clone();
                           buffer.rotation.rotate(entityPosition);
                           Vector3d entityWorldPosition = entityPosition.add(prefabPositionVector);
                           if (entityBuffer.getBounds().contains(entityWorldPosition)) {
                              Holder<EntityStore> entityClone = entityWrappers[ix].clone();
                              transformComp = entityClone.getComponent(TransformComponent.getComponentType());
                              if (transformComp != null) {
                                 entityPosition = transformComp.getPosition();
                                 entityPosition.x = entityWorldPosition.x;
                                 entityPosition.y = entityWorldPosition.y;
                                 entityPosition.z = entityWorldPosition.z;
                                 if (!materialSpace.getBounds()
                                    .contains((int)Math.floor(entityPosition.x), (int)Math.floor(entityPosition.y), (int)Math.floor(entityPosition.z))) {
                                    return;
                                 }

                                 EntityPlacementData placementData = new EntityPlacementData(
                                    new Vector3i(), PrefabRotation.ROTATION_0, entityClone, this.prefabId
                                 );
                                 entityBuffer.addEntity(placementData);
                              }
                           }
                        }
                     }
                  }
               }
            },
            (x, yx, z, path, fitHeightmap, inheritSeed, inheritHeightCondition, weights, rotation, t) -> {},
            callInstance
         );
      } catch (Exception var16) {
         String msg = "Couldn't place prefab prop.";
         msg = msg + "\n";
         msg = msg + ExceptionUtil.toStringWithStack(var16);
         ((HytaleLogger.Api)HytaleLogger.getLogger().atWarning()).log(msg);
      }

      for (int i = 0; i < this.childProps.size(); i++) {
         PrefabProp prop = this.childProps.get(i);
         RotatedPosition childPosition = this.childPositions.get(i).getRelativeTo(position);
         Vector3i rotatedChildPositionVec = new Vector3i(childPosition.x, childPosition.y, childPosition.z);
         position.rotation.rotate(rotatedChildPositionVec);
         if (moldingOffsets != null && moldingOffsets.getBounds().contains(childPosition.x, 0, childPosition.z)) {
            Integer offset = moldingOffsets.get(childPosition.x, 0, childPosition.z);
            if (offset == null) {
               continue;
            }

            int y = childPosition.y + offset;
            childPosition = new RotatedPosition(childPosition.x, y, childPosition.z, childPosition.rotation);
         }

         prop.place(childPosition, materialSpace, entityBuffer);
      }
   }

   @NonNullDecl
   @Override
   public Bounds3i getReadBounds_voxelGrid() {
      return this.readBounds_voxelGrid;
   }

   @Nonnull
   @Override
   public Bounds3i getWriteBounds_voxelGrid() {
      return this.writeBounds_voxelGrid;
   }
}
