package com.hypixel.hytale.builtin.hytalegenerator.props;

import com.hypixel.hytale.builtin.hytalegenerator.VectorUtil;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.SpaceSize;
import com.hypixel.hytale.builtin.hytalegenerator.conveyor.stagedconveyor.ContextDependency;
import com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace.VoxelSpace;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.newsystem.GridUtils;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.List;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

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
   private final ContextDependency contextDependency;
   @Nonnull
   private final Bounds3i readBounds_voxelGrid;
   @Nonnull
   private final Bounds3i writeBounds_voxelGrid;
   @Nonnull
   private final Bounds3i boxBounds_voxelGrid;

   public BoxProp(@Nonnull Vector3i range, @Nonnull Material material, @Nonnull Scanner scanner, @Nonnull Pattern pattern) {
      if (VectorUtil.isAnySmaller(range, new Vector3i())) {
         throw new IllegalArgumentException("negative range");
      } else {
         this.range = range.clone();
         this.material = material;
         this.scanner = scanner;
         this.pattern = pattern;
         SpaceSize writeSpace = new SpaceSize(new Vector3i(-range.x - 1, 0, -range.z - 1), new Vector3i(range.x + 2, 0, range.z + 2));
         writeSpace = SpaceSize.stack(writeSpace, scanner.readSpaceWith(pattern));
         Vector3i writeRange = writeSpace.getRange();
         Vector3i readRange = scanner.readSpaceWith(pattern).getRange();
         this.contextDependency = new ContextDependency(readRange, writeRange);
         this.readBounds_voxelGrid = this.contextDependency.getReadBounds_voxelGrid();
         this.writeBounds_voxelGrid = this.contextDependency.getWriteBounds_voxelGrid();
         this.boxBounds_voxelGrid = GridUtils.createBounds_fromVector_originVoxelInclusive(range);
      }
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
         Bounds3i writeSpaceBounds_voxelGrid = context.materialSpace.getBounds();

         for (Vector3i position : positions) {
            Bounds3i localBoxBounds_voxelGrid = this.boxBounds_voxelGrid.clone().offset(position);
            if (localBoxBounds_voxelGrid.intersects(writeSpaceBounds_voxelGrid)) {
               this.place(position, context.materialSpace);
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
               if (materialSpace.isInsideSpace(x, y, z)) {
                  materialSpace.set(this.material, x, y, z);
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
