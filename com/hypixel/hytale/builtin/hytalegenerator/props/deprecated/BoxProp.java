package com.hypixel.hytale.builtin.hytalegenerator.props.deprecated;

import com.hypixel.hytale.builtin.hytalegenerator.VectorUtil;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.props.Prop;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.builtin.hytalegenerator.voxelspace.VoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.workerindexer.WorkerIndexer;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

@Deprecated
public class BoxProp extends Prop {
   @Nonnull
   private final Vector3i range;
   @Nonnull
   private final Material material;
   @Nonnull
   private final Scanner scanner;
   @Nonnull
   private final Pattern pattern;
   @Nonnull
   private final Bounds3i readBounds_voxelGrid;
   @Nonnull
   private final Bounds3i writeBounds_voxelGrid;

   public BoxProp(@Nonnull Vector3i range, @Nonnull Material material, @Nonnull Scanner scanner, @Nonnull Pattern pattern) {
      if (VectorUtil.isAnySmaller(range, new Vector3i())) {
         throw new IllegalArgumentException("negative range");
      } else {
         this.range = range.clone();
         this.material = material;
         this.scanner = scanner;
         this.pattern = pattern;
         this.readBounds_voxelGrid = scanner.getBoundsWithPattern_voxelGrid(pattern);
         this.writeBounds_voxelGrid = new Bounds3i(this.range.clone().scale(-1), this.range.clone().add(Vector3i.ALL_ONES));
         this.writeBounds_voxelGrid.stack(pattern.getBounds_voxelGrid());
      }
   }

   @Override
   public boolean generate(@NonNullDecl Prop.Context context) {
      PositionListScanResult scanResult = this.scan_deprecated(context.position, context.materialReadSpace, WorkerIndexer.Id.MAIN);
      this.place_deprecated(context, scanResult);
      return !scanResult.isNegative();
   }

   @Nonnull
   public PositionListScanResult scan_deprecated(@Nonnull Vector3i position, @Nonnull VoxelSpace<Material> materialSpace, @Nonnull WorkerIndexer.Id id) {
      List<Vector3i> validPositions = new ArrayList<>();
      Scanner.Context scannerContext = new Scanner.Context(position, this.pattern, materialSpace, validPositions);
      this.scanner.scan(scannerContext);
      return new PositionListScanResult(validPositions);
   }

   public void place_deprecated(@Nonnull Prop.Context context, @Nonnull PositionListScanResult scanResult) {
      List<Vector3i> positions = scanResult.getPositions();
      if (positions != null) {
         Bounds3i writeSpaceBounds_voxelGrid = context.materialWriteSpace.getBounds();

         for (Vector3i position : positions) {
            Bounds3i localBoxBounds_voxelGrid = this.writeBounds_voxelGrid.clone().offset(position);
            if (localBoxBounds_voxelGrid.intersects(writeSpaceBounds_voxelGrid)) {
               this.place(position, context.materialWriteSpace);
            }
         }
      }
   }

   private void place(@Nonnull Vector3i position, @Nonnull VoxelSpace<Material> materialSpace) {
      Vector3i min = position.clone().add(-this.range.x, 0, -this.range.z);
      Vector3i max = position.clone().add(this.range.x, this.range.y + this.range.y, this.range.z);

      for (int x = min.x; x <= max.x; x++) {
         for (int y = min.y; y <= max.y; y++) {
            for (int z = min.z; z <= max.z; z++) {
               if (materialSpace.getBounds().contains(x, y, z)) {
                  materialSpace.set(this.material, x, y, z);
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
