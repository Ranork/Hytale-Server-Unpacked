package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.builtin.hytalegenerator.BlockMask;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.SpaceSize;
import com.hypixel.hytale.builtin.hytalegenerator.conveyor.stagedconveyor.ContextDependency;
import com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace.ArrayVoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace.VoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.List;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class DensityProp extends Prop {
   @Nonnull
   private final Vector3i range;
   @Nonnull
   private final Density density;
   @Nonnull
   private final MaterialProvider<Material> materialProvider;
   @Nonnull
   private final Scanner scanner;
   @Nonnull
   private final Pattern pattern;
   @Nonnull
   private final ContextDependency contextDependency;
   @Nonnull
   private final BlockMask placementMask;
   @Nonnull
   private final Material defaultMaterial;
   @Nonnull
   private final Bounds3i readBounds_voxelGrid;
   @Nonnull
   private final Bounds3i writeBounds_voxelGrid;

   public DensityProp(
      @Nonnull Vector3i range,
      @Nonnull Density density,
      @Nonnull MaterialProvider<Material> materialProvider,
      @Nonnull Scanner scanner,
      @Nonnull Pattern pattern,
      @Nonnull BlockMask placementMask,
      @Nonnull Material defaultMaterial
   ) {
      this.range = range.clone();
      this.density = density;
      this.materialProvider = materialProvider;
      this.scanner = scanner;
      this.pattern = pattern;
      this.placementMask = placementMask;
      this.defaultMaterial = defaultMaterial;
      SpaceSize writeSpace = new SpaceSize(new Vector3i(-range.x - 1, 0, -range.z - 1), new Vector3i(range.x + 2, 0, range.z + 2));
      writeSpace = SpaceSize.stack(writeSpace, scanner.readSpaceWith(pattern));
      Vector3i writeRange = writeSpace.getRange();
      Vector3i readRange = scanner.readSpaceWith(pattern).getRange();
      this.contextDependency = new ContextDependency(readRange, writeRange);
      this.readBounds_voxelGrid = this.contextDependency.getReadBounds_voxelGrid();
      this.writeBounds_voxelGrid = this.contextDependency.getWriteBounds_voxelGrid();
   }

   @Nonnull
   public PositionListScanResult scan(@Nonnull Vector3i position, @Nonnull VoxelSpace<Material> materialSpace, @Nonnull WorkerIndexer.Id id) {
      Scanner.Context scannerContext = new Scanner.Context(position, this.pattern, materialSpace, id);
      List<Vector3i> validPositions = this.scanner.scan(scannerContext);
      return new PositionListScanResult(validPositions);
   }

   @Override
   public void place(@Nonnull Prop.Context context) {
      List<Vector3i> positions = PositionListScanResult.cast(context.scanResult).getPositions();
      if (positions != null) {
         for (Vector3i position : positions) {
            this.place(position, context.materialSpace, context.workerId);
         }
      }
   }

   private void place(@Nonnull Vector3i position, @Nonnull VoxelSpace<Material> materialSpace, @Nonnull WorkerIndexer.Id id) {
      Vector3i min = position.clone().add(-this.range.x, -this.range.y, -this.range.z);
      Vector3i max = position.clone().add(this.range.x, this.range.y, this.range.z);
      Vector3i writeMin = Vector3i.max(min, new Vector3i(materialSpace.minX(), materialSpace.minY(), materialSpace.minZ()));
      Vector3i writeMax = Vector3i.min(max, new Vector3i(materialSpace.maxX(), materialSpace.maxY(), materialSpace.maxZ()));
      int bottom = min.y;
      int top = max.y;
      int height = top - bottom;
      ArrayVoxelSpace<Boolean> densitySpace = new ArrayVoxelSpace<>(max.x - min.x + 1, max.y - min.y + 1, max.z - min.z + 1);
      densitySpace.setOrigin(-min.x, -min.y, -min.z);
      Density.Context childContext = new Density.Context();
      childContext.densityAnchor = position.toVector3d();
      Vector3i itPosition = new Vector3i(position);

      for (itPosition.x = min.x; itPosition.x <= max.x; itPosition.x++) {
         for (itPosition.z = min.z; itPosition.z <= max.z; itPosition.z++) {
            for (itPosition.y = min.y; itPosition.y <= max.y; itPosition.y++) {
               if (densitySpace.isInsideSpace(itPosition.x, itPosition.y, itPosition.z)) {
                  childContext.position.x = itPosition.x;
                  childContext.position.y = itPosition.y;
                  childContext.position.z = itPosition.z;
                  double densityValue = this.density.process(childContext);
                  densitySpace.set(densityValue > 0.0, itPosition.x, itPosition.y, itPosition.z);
               }
            }
         }
      }

      for (itPosition.x = min.x; itPosition.x <= max.x; itPosition.x++) {
         for (itPosition.z = min.z; itPosition.z <= max.z; itPosition.z++) {
            int[] depthIntoCeiling = new int[height + 1];
            int[] depthIntoFloor = new int[height + 1];
            int[] spaceBelowCeiling = new int[height + 1];
            int[] spaceAboveFloor = new int[height + 1];

            for (itPosition.y = top; itPosition.y >= bottom; itPosition.y--) {
               int i = itPosition.y - bottom;
               boolean density = densitySpace.getContent(itPosition.x, itPosition.y, itPosition.z);
               if (itPosition.y == top) {
                  if (density) {
                     depthIntoFloor[i] = 1;
                  } else {
                     depthIntoFloor[i] = 0;
                  }

                  spaceAboveFloor[i] = 1073741823;
               } else if (density) {
                  depthIntoFloor[i] = depthIntoFloor[i + 1] + 1;
                  spaceAboveFloor[i] = spaceAboveFloor[i + 1];
               } else {
                  depthIntoFloor[i] = 0;
                  if (densitySpace.getContent(itPosition.x, itPosition.y + 1, itPosition.z)) {
                     spaceAboveFloor[i] = 0;
                  } else {
                     spaceAboveFloor[i] = spaceAboveFloor[i + 1] + 1;
                  }
               }
            }

            for (itPosition.y = bottom; itPosition.y < top; itPosition.y++) {
               int i = itPosition.y - bottom;
               boolean density = densitySpace.getContent(itPosition.x, itPosition.y, itPosition.z);
               if (itPosition.y == bottom) {
                  if (density) {
                     depthIntoCeiling[i] = 1;
                  } else {
                     depthIntoCeiling[i] = 0;
                  }

                  spaceBelowCeiling[i] = Integer.MAX_VALUE;
               } else if (density) {
                  depthIntoCeiling[i] = depthIntoCeiling[i - 1] + 1;
                  spaceBelowCeiling[i] = spaceBelowCeiling[i - 1];
               } else {
                  depthIntoCeiling[i] = 0;
                  if (densitySpace.getContent(itPosition.x, itPosition.y - 1, itPosition.z)) {
                     spaceBelowCeiling[i] = 0;
                  } else {
                     spaceBelowCeiling[i] = spaceBelowCeiling[i - 1] + 1;
                  }
               }
            }

            for (itPosition.y = top; itPosition.y >= bottom; itPosition.y--) {
               if (itPosition.x >= writeMin.x
                  && itPosition.y >= writeMin.y
                  && itPosition.z >= writeMin.z
                  && itPosition.x < writeMax.x
                  && itPosition.y < writeMax.y
                  && itPosition.z < writeMax.z) {
                  int i = itPosition.y - bottom;
                  MaterialProvider.Context materialContext = new MaterialProvider.Context(
                     position, 0.0, depthIntoFloor[i], depthIntoCeiling[i], spaceAboveFloor[i], spaceBelowCeiling[i], functionPosition -> {
                        childContext.position = functionPosition.toVector3d();
                        return this.density.process(childContext);
                     }, childContext.distanceToBiomeEdge
                  );
                  Material material = this.materialProvider.getVoxelTypeAt(materialContext);
                  if (material == null) {
                     material = this.defaultMaterial;
                  }

                  if (this.placementMask.canPlace(material)) {
                     Material worldMaterial = materialSpace.getContent(itPosition.x, itPosition.y, itPosition.z);
                     int worldMaterialHash = worldMaterial.hashMaterialIds();
                     if (this.placementMask.canReplace(material.hashCode(), worldMaterialHash)) {
                        materialSpace.set(material, itPosition.x, itPosition.y, itPosition.z);
                     }
                  }
               }
            }
         }
      }
   }

   @Nonnull
   @Override
   public ContextDependency getContextDependency() {
      return this.contextDependency.clone();
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
