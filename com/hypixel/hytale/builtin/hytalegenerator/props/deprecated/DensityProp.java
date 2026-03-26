package com.hypixel.hytale.builtin.hytalegenerator.props.deprecated;

import com.hypixel.hytale.builtin.hytalegenerator.BlockMask;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.ArrayVoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.VoxelSpace;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayList;
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
      this.readBounds_voxelGrid = scanner.getBoundsWithPattern_voxelGrid(pattern);
      this.writeBounds_voxelGrid = new Bounds3i(new Vector3i(-range.x, -range.y, -range.z), new Vector3i(range.x, range.y, range.z));
      this.writeBounds_voxelGrid.stack(scanner.getBounds_voxelGrid());
   }

   @Override
   public boolean generate(@NonNullDecl Prop.Context context) {
      PositionListScanResult scanResult = this.scan_deprecated(context.position, context.materialReadSpace);
      this.place_deprecated(context, scanResult);
      return !scanResult.isNegative();
   }

   @Nonnull
   public PositionListScanResult scan_deprecated(@Nonnull Vector3i position, @Nonnull VoxelSpace<Material> materialSpace) {
      Scanner.Context scannerContext = new Scanner.Context(position, this.pattern, materialSpace, new ArrayList<>());
      this.scanner.scan(scannerContext);
      return new PositionListScanResult(scannerContext.validPositions_out);
   }

   public void place_deprecated(@Nonnull Prop.Context context, @Nonnull PositionListScanResult scanResult) {
      List<Vector3i> positions = scanResult.getPositions();
      if (positions != null) {
         for (Vector3i position : positions) {
            this.place(position, context.materialWriteSpace);
         }
      }
   }

   private void place(@Nonnull Vector3i position, @Nonnull VoxelSpace<Material> materialSpace) {
      Bounds3i bounds = materialSpace.getBounds();
      Vector3i min = position.clone().add(-this.range.x, -this.range.y, -this.range.z);
      Vector3i max = position.clone().add(this.range.x, this.range.y, this.range.z);
      Vector3i writeMin = Vector3i.max(min, bounds.min);
      Vector3i writeMax = Vector3i.min(max, bounds.max);
      int bottomInclusive = min.y;
      int topExclusive = max.y;
      int height = topExclusive - bottomInclusive;
      ArrayVoxelSpace<Boolean> solidityBuffer = new ArrayVoxelSpace<>(new Bounds3i(min, max));
      Density.Context childContext = new Density.Context();
      childContext.densityAnchor = position.toVector3d();
      Vector3i itPosition = new Vector3i(position);

      for (itPosition.x = min.x; itPosition.x < max.x; itPosition.x++) {
         for (itPosition.z = min.z; itPosition.z < max.z; itPosition.z++) {
            for (itPosition.y = min.y; itPosition.y < max.y; itPosition.y++) {
               if (solidityBuffer.getBounds().contains(itPosition.x, itPosition.y, itPosition.z)) {
                  childContext.position.x = itPosition.x;
                  childContext.position.y = itPosition.y;
                  childContext.position.z = itPosition.z;
                  double densityValue = this.density.process(childContext);
                  solidityBuffer.set(densityValue > 0.0, itPosition.x, itPosition.y, itPosition.z);
               }
            }
         }
      }

      for (itPosition.x = min.x; itPosition.x < max.x; itPosition.x++) {
         for (itPosition.z = min.z; itPosition.z < max.z; itPosition.z++) {
            int[] depthIntoCeiling = new int[height + 1];
            int[] depthIntoFloor = new int[height + 1];
            int[] spaceBelowCeiling = new int[height + 1];
            int[] spaceAboveFloor = new int[height + 1];

            for (itPosition.y = topExclusive - 1; itPosition.y >= bottomInclusive; itPosition.y--) {
               int i = itPosition.y - bottomInclusive;
               boolean density = solidityBuffer.get(itPosition.x, itPosition.y, itPosition.z);
               if (itPosition.y == topExclusive - 1) {
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
                  if (solidityBuffer.get(itPosition.x, itPosition.y + 1, itPosition.z)) {
                     spaceAboveFloor[i] = 0;
                  } else {
                     spaceAboveFloor[i] = spaceAboveFloor[i + 1] + 1;
                  }
               }
            }

            for (itPosition.y = bottomInclusive; itPosition.y < topExclusive; itPosition.y++) {
               int i = itPosition.y - bottomInclusive;
               boolean density = solidityBuffer.get(itPosition.x, itPosition.y, itPosition.z);
               if (itPosition.y == bottomInclusive) {
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
                  if (solidityBuffer.get(itPosition.x, itPosition.y - 1, itPosition.z)) {
                     spaceBelowCeiling[i] = 0;
                  } else {
                     spaceBelowCeiling[i] = spaceBelowCeiling[i - 1] + 1;
                  }
               }
            }

            for (itPosition.y = topExclusive - 1; itPosition.y >= bottomInclusive; itPosition.y--) {
               if (itPosition.x >= writeMin.x
                  && itPosition.y >= writeMin.y
                  && itPosition.z >= writeMin.z
                  && itPosition.x < writeMax.x
                  && itPosition.y < writeMax.y
                  && itPosition.z < writeMax.z) {
                  int i = itPosition.y - bottomInclusive;
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
                     Material worldMaterial = materialSpace.get(itPosition.x, itPosition.y, itPosition.z);
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
