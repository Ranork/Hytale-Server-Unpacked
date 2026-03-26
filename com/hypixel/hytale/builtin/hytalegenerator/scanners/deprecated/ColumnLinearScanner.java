package com.hypixel.hytale.builtin.hytalegenerator.scanners.deprecated;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ColumnLinearScanner extends Scanner {
   private final int minY;
   private final int maxY;
   private final boolean isRelativeToPosition;
   private final double baseHeight;
   private final int resultsCap;
   private final boolean topDownOrder;
   @Nonnull
   private final Bounds3i bounds_voxelGrid;

   public ColumnLinearScanner(int minY, int maxY, int resultsCap, boolean topDownOrder, boolean isRelativeToPosition, double baseHeight) {
      if (resultsCap < 0) {
         throw new IllegalArgumentException();
      } else {
         this.baseHeight = baseHeight;
         this.minY = minY;
         this.maxY = maxY;
         this.isRelativeToPosition = isRelativeToPosition;
         this.resultsCap = resultsCap;
         this.topDownOrder = topDownOrder;
         if (!isRelativeToPosition) {
            int MIN_SCAN_Y = -1073741824;
            int MAX_SCAN_Y = 1073741823;
            this.bounds_voxelGrid = new Bounds3i(new Vector3i(0, -1073741824, 0), new Vector3i(1, 1073741823, 1));
         } else {
            this.bounds_voxelGrid = new Bounds3i(new Vector3i(0, minY, 0), new Vector3i(1, maxY, 1));
         }
      }
   }

   @Override
   public void scan(@NonNullDecl Scanner.Context context) {
      Bounds3i bounds = context.materialSpace.getBounds();
      int scanMinY;
      int scanMaxY;
      if (this.isRelativeToPosition) {
         scanMinY = Math.max(context.position.y + this.minY, bounds.min.y);
         scanMaxY = Math.min(context.position.y + this.maxY, bounds.max.y);
      } else {
         int bedY = (int)this.baseHeight;
         scanMinY = Math.max(bedY + this.minY, bounds.min.y);
         scanMaxY = Math.min(bedY + this.maxY, bounds.max.y);
      }

      Vector3i patternPosition = context.position.clone();
      Pattern.Context patternContext = new Pattern.Context(patternPosition, context.materialSpace);
      if (this.topDownOrder) {
         for (patternPosition.y = scanMaxY - 1; patternPosition.y >= scanMinY; patternPosition.y--) {
            if (context.pattern.matches(patternContext)) {
               context.validPositions_out.add(patternPosition.clone());
               if (context.validPositions_out.size() >= this.resultsCap) {
                  return;
               }
            }
         }
      } else {
         for (patternPosition.y = scanMinY; patternPosition.y < scanMaxY; patternPosition.y++) {
            if (context.pattern.matches(patternContext)) {
               context.validPositions_out.add(patternPosition.clone());
               if (context.validPositions_out.size() >= this.resultsCap) {
                  return;
               }
            }
         }
      }
   }

   @Override
   public void scan(@NonNullDecl Vector3i anchor, @NonNullDecl Pipe.One<Vector3i> pipe) {
   }

   @Override
   public Bounds3i getBounds_voxelGrid() {
      return this.bounds_voxelGrid;
   }
}
