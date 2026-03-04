package com.hypixel.hytale.builtin.hytalegenerator.props.prefab;

import com.hypixel.hytale.builtin.hytalegenerator.BlockMask;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.SpaceSize;
import com.hypixel.hytale.builtin.hytalegenerator.conveyor.stagedconveyor.ContextDependency;
import com.hypixel.hytale.builtin.hytalegenerator.datastructures.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace.ArrayVoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace.VoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.Calculator;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.SeedGenerator;
import com.hypixel.hytale.builtin.hytalegenerator.material.FluidMaterial;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.material.SolidMaterial;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.views.EntityContainer;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.props.ScanResult;
import com.hypixel.hytale.builtin.hytalegenerator.props.directionality.Directionality;
import com.hypixel.hytale.builtin.hytalegenerator.props.directionality.RotatedPosition;
import com.hypixel.hytale.builtin.hytalegenerator.props.directionality.RotatedPositionsScanResult;
import com.hypixel.hytale.builtin.hytalegenerator.props.directionality.StaticDirectionality;
import com.hypixel.hytale.builtin.hytalegenerator.props.entity.EntityPlacementData;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.OriginScanner;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
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
   private final WeightedMap<List<PrefabBuffer>> prefabPool;
   @Nonnull
   private final Scanner scanner;
   private ContextDependency contextDependency;
   @Nonnull
   private final MaterialCache materialCache;
   @Nonnull
   private final SeedGenerator seedGenerator;
   @Nonnull
   private final BlockMask materialMask;
   @Nonnull
   private final Directionality directionality;
   @Nonnull
   private final Bounds3i readBounds_voxelGrid;
   @Nonnull
   private final Bounds3i writeBounds_voxelGrid;
   @Nonnull
   private final Bounds3i prefabBounds_voxelGrid;
   @Nonnull
   private final List<PrefabProp> childProps;
   @Nonnull
   private final List<RotatedPosition> childPositions;
   @Nonnull
   private final Function<String, List<PrefabBuffer>> childPrefabLoader;
   private final Scanner moldingScanner;
   private final Pattern moldingPattern;
   private final MoldingDirection moldingDirection;
   private final boolean moldChildren;
   private final int prefabId = this.hashCode();
   private boolean loadEntities;

   public PrefabProp(
      @Nonnull WeightedMap<List<PrefabBuffer>> prefabPool,
      @Nonnull Scanner scanner,
      @Nonnull Directionality directionality,
      @Nonnull MaterialCache materialCache,
      @Nonnull BlockMask materialMask,
      @Nonnull PrefabMoldingConfiguration prefabMoldingConfiguration,
      @Nullable Function<String, List<PrefabBuffer>> childPrefabLoader,
      @Nonnull SeedBox seedBox,
      boolean loadEntities
   ) {
      this.prefabPool = prefabPool;
      this.scanner = scanner;
      this.directionality = directionality;
      this.materialCache = materialCache;
      this.seedGenerator = new SeedGenerator(seedBox.createSupplier().get().intValue());
      this.materialMask = materialMask;
      this.loadEntities = loadEntities;
      this.childProps = new ArrayList<>();
      this.childPositions = new ArrayList<>();
      this.childPrefabLoader = childPrefabLoader == null ? s -> null : childPrefabLoader;
      this.moldingScanner = prefabMoldingConfiguration.moldingScanner;
      this.moldingPattern = prefabMoldingConfiguration.moldingPattern;
      this.moldingDirection = prefabMoldingConfiguration.moldingDirection;
      this.moldChildren = prefabMoldingConfiguration.moldChildren;
      this.contextDependency = new ContextDependency();
      Vector3i readRange = directionality.getReadRangeWith(scanner);

      for (List<PrefabBuffer> prefabList : prefabPool.allElements()) {
         if (prefabList.isEmpty()) {
            throw new IllegalArgumentException("prefab pool contains empty list");
         }

         for (PrefabBuffer prefab : prefabList) {
            if (prefab == null) {
               throw new IllegalArgumentException("prefab pool contains list with null element");
            }

            PrefabBuffer.PrefabBufferAccessor prefabAccess = prefab.newAccess();
            PrefabBuffer.ChildPrefab[] childPrefabs = prefabAccess.getChildPrefabs();
            int childId = 0;

            for (PrefabBuffer.ChildPrefab child : childPrefabs) {
               RotatedPosition childPosition = new RotatedPosition(child.getX(), child.getY(), child.getZ(), child.getRotation());
               String childPath = child.getPath().replace('.', '/');
               childPath = childPath.replace("*", "");
               List<PrefabBuffer> childPrefabBuffers = this.childPrefabLoader.apply(childPath);
               WeightedMap<List<PrefabBuffer>> weightedChildPrefabs = new WeightedMap<>();
               weightedChildPrefabs.add(childPrefabBuffers, 1.0);
               StaticDirectionality childDirectionality = new StaticDirectionality(child.getRotation(), Pattern.yesPattern());
               PrefabProp childProp = new PrefabProp(
                  weightedChildPrefabs,
                  OriginScanner.getInstance(),
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

            Vector3i writeRange = this.getWriteRange(prefabAccess);

            for (int i = 0; i < this.childPositions.size(); i++) {
               PrefabProp child = this.childProps.get(i);
               Vector3i position = this.childPositions.get(i).toVector3i();
               Vector3i childWriteRange = child.getContextDependency().getWriteRange();
               int maxRange = Calculator.max(position.x, position.y, position.z);
               maxRange += Calculator.max(childWriteRange.x, childWriteRange.y, childWriteRange.z);
               writeRange.x = Math.max(writeRange.x, maxRange);
               writeRange.y = Math.max(writeRange.y, maxRange);
               writeRange.z = Math.max(writeRange.z, maxRange);
            }

            ContextDependency contextDependency = new ContextDependency(readRange, writeRange);
            this.contextDependency = ContextDependency.mostOf(this.contextDependency, contextDependency);
            prefabAccess.release();
         }
      }

      this.readBounds_voxelGrid = this.contextDependency.getReadBounds_voxelGrid();
      this.writeBounds_voxelGrid = this.contextDependency.getWriteBounds_voxelGrid();
      this.prefabBounds_voxelGrid = new Bounds3i();
      this.prefabBounds_voxelGrid.min.assign(this.contextDependency.getWriteRange()).scale(-1);
      this.prefabBounds_voxelGrid.max.assign(this.contextDependency.getWriteRange()).add(Vector3i.ALL_ONES);
   }

   @Nonnull
   private Vector3i getWriteRange(@Nonnull PrefabBuffer.PrefabBufferAccessor prefabAccess) {
      SpaceSize space = new SpaceSize();

      for (PrefabRotation rotation : this.directionality.getPossibleRotations()) {
         Vector3i max = PropPrefabUtil.getMax(prefabAccess, rotation);
         max.add(1, 1, 1);
         Vector3i min = PropPrefabUtil.getMin(prefabAccess, rotation);
         space = SpaceSize.merge(space, new SpaceSize(min, max));
      }

      space = SpaceSize.stack(space, this.scanner.readSpaceWith(this.directionality.getGeneralPattern()));
      return space.getRange();
   }

   @Nonnull
   @Override
   public ScanResult scan(@Nonnull Vector3i position, @Nonnull VoxelSpace<Material> materialSpace, @Nonnull WorkerIndexer.Id id) {
      Scanner.Context scannerContext = new Scanner.Context(position, this.directionality.getGeneralPattern(), materialSpace, id);
      List<Vector3i> validPositions = this.scanner.scan(scannerContext);
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

   @Override
   public void place(@Nonnull Prop.Context context) {
      if (this.prefabPool.size() != 0) {
         List<RotatedPosition> positions = RotatedPositionsScanResult.cast(context.scanResult).positions;
         if (positions != null) {
            Bounds3i writeSpaceBounds_voxelGrid = context.materialSpace.getBounds();

            for (RotatedPosition position : positions) {
               Bounds3i localPrefabWriteBounds_voxelGrid = this.prefabBounds_voxelGrid.clone().offset(position.toVector3i());
               if (localPrefabWriteBounds_voxelGrid.intersects(writeSpaceBounds_voxelGrid)) {
                  this.place(position, context.materialSpace, context.entityBuffer, context.workerId);
               }
            }
         }
      }
   }

   private PrefabBuffer pickPrefab(@Nonnull Random rand) {
      List<PrefabBuffer> list = this.prefabPool.pick(rand);
      int randomIndex = rand.nextInt(list.size());
      return list.get(randomIndex);
   }

   private void place(
      @Nonnull RotatedPosition position, @Nonnull VoxelSpace<Material> materialSpace, @Nonnull EntityContainer entityBuffer, @Nonnull WorkerIndexer.Id id
   ) {
      Random random = new Random(this.seedGenerator.seedAt((long)position.x, (long)position.y, (long)position.z));
      PrefabBufferCall callInstance = new PrefabBufferCall(random, position.rotation);
      PrefabBuffer prefab = this.pickPrefab(random);
      PrefabBuffer.PrefabBufferAccessor prefabAccess = prefab.newAccess();
      VoxelSpace<Integer> moldingOffsets = null;
      if (this.moldingDirection != MoldingDirection.NONE) {
         int prefabMinX = prefabAccess.getMinX(position.rotation);
         int prefabMinZ = prefabAccess.getMinZ(position.rotation);
         int prefabMaxX = prefabAccess.getMaxX(position.rotation);
         int prefabMaxZ = prefabAccess.getMaxZ(position.rotation);
         int prefabSizeX = prefabMaxX - prefabMinX;
         int prefabSizeZ = prefabMaxZ - prefabMinZ;
         moldingOffsets = new ArrayVoxelSpace<>(prefabSizeX, 1, prefabSizeZ);
         moldingOffsets.setOrigin(-position.x - prefabMinX, 0, -position.z - prefabMinZ);
         if (this.moldingDirection == MoldingDirection.DOWN || this.moldingDirection == MoldingDirection.UP) {
            Vector3i pointer = new Vector3i(0, position.y, 0);
            Scanner.Context scannerContext = new Scanner.Context(pointer, this.moldingPattern, materialSpace, id);

            for (pointer.x = moldingOffsets.minX(); pointer.x < moldingOffsets.maxX(); pointer.x++) {
               for (pointer.z = moldingOffsets.minZ(); pointer.z < moldingOffsets.maxZ(); pointer.z++) {
                  List<Vector3i> scanResult = this.moldingScanner.scan(scannerContext);
                  Integer offset = scanResult.isEmpty() ? null : scanResult.getFirst().y - position.y;
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
         prefabAccess.forEach(
            IPrefabBuffer.iterateAllColumns(),
            (x, yx, z, blockId, holder, support, rotation, filler, call, fluidId, fluidLevel) -> {
               int worldX = position.x + x;
               int worldY = position.y + yx;
               int worldZ = position.z + z;
               if (materialSpace.isInsideSpace(worldX, worldY, worldZ)) {
                  SolidMaterial solid = this.materialCache.getSolidMaterial(blockId, support, rotation, filler, holder != null ? holder.clone() : null);
                  FluidMaterial fluid = this.materialCache.getFluidMaterial(fluidId, (byte)fluidLevel);
                  Material material = this.materialCache.getMaterial(solid, fluid);
                  int materialHash = material.hashMaterialIds();
                  if (this.materialMask.canPlace(materialHash)) {
                     if (this.moldingDirection == MoldingDirection.DOWN || this.moldingDirection == MoldingDirection.UP) {
                        Integer offsetx = null;
                        if (moldingOffsetsFinal.isInsideSpace(worldX, 0, worldZ)) {
                           offsetx = moldingOffsetsFinal.getContent(worldX, 0, worldZ);
                        }

                        if (offsetx == null) {
                           return;
                        }

                        worldY += offsetx;
                     }

                     Material worldMaterial = materialSpace.getContent(worldX, worldY, worldZ);
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
                           if (entityBuffer.isInsideBuffer((int)entityWorldPosition.x, (int)entityWorldPosition.y, (int)entityWorldPosition.z)) {
                              Holder<EntityStore> entityClone = entityWrappers[ix].clone();
                              transformComp = entityClone.getComponent(TransformComponent.getComponentType());
                              if (transformComp != null) {
                                 entityPosition = transformComp.getPosition();
                                 entityPosition.x = entityWorldPosition.x;
                                 entityPosition.y = entityWorldPosition.y;
                                 entityPosition.z = entityWorldPosition.z;
                                 if (!materialSpace.isInsideSpace(
                                    (int)Math.floor(entityPosition.x), (int)Math.floor(entityPosition.y), (int)Math.floor(entityPosition.z)
                                 )) {
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
      } catch (Exception var23) {
         String msg = "Couldn't place prefab prop.";
         msg = msg + "\n";
         msg = msg + ExceptionUtil.toStringWithStack(var23);
         ((HytaleLogger.Api)HytaleLogger.getLogger().atWarning()).log(msg);
      } finally {
         prefabAccess.release();
      }

      for (int i = 0; i < this.childProps.size(); i++) {
         PrefabProp prop = this.childProps.get(i);
         RotatedPosition childPosition = this.childPositions.get(i).getRelativeTo(position);
         Vector3i rotatedChildPositionVec = new Vector3i(childPosition.x, childPosition.y, childPosition.z);
         position.rotation.rotate(rotatedChildPositionVec);
         if (moldingOffsets != null && moldingOffsets.isInsideSpace(childPosition.x, 0, childPosition.z)) {
            Integer offset = moldingOffsets.getContent(childPosition.x, 0, childPosition.z);
            if (offset == null) {
               continue;
            }

            int y = childPosition.y + offset;
            childPosition = new RotatedPosition(childPosition.x, y, childPosition.z, childPosition.rotation);
         }

         prop.place(childPosition, materialSpace, entityBuffer, id);
      }
   }

   @Override
   public ContextDependency getContextDependency() {
      return this.contextDependency;
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
