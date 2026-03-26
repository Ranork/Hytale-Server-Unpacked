package com.hypixel.hytale.builtin.hytalegenerator.scanners.deprecated;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.builtin.hytalegenerator.pipe.Pipe;
import com.hypixel.hytale.builtin.hytalegenerator.rng.RngField;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ColumnRandomScanner extends Scanner {
   private final int minY;
   private final int maxY;
   private final boolean isRelativeToPosition;
   private final double baseHeight;
   private final int resultsCap;
   @Nonnull
   private final RngField rngField;
   @Nonnull
   private final ColumnRandomScanner.Strategy strategy;
   @Nonnull
   private final Bounds3i bounds_voxelGrid;
   @Nonnull
   private final List<Vector3i> rPositions;

   public ColumnRandomScanner(
      int minY, int maxY, int resultsCap, int seed, @Nonnull ColumnRandomScanner.Strategy strategy, boolean isRelativeToPosition, double baseHeight
   ) {
      if (resultsCap < 0) {
         throw new IllegalArgumentException();
      } else {
         this.baseHeight = baseHeight;
         this.minY = minY;
         this.maxY = maxY;
         this.isRelativeToPosition = isRelativeToPosition;
         this.resultsCap = resultsCap;
         this.rngField = new RngField(seed);
         this.strategy = strategy;
         if (!isRelativeToPosition) {
            int MIN_SCAN_Y = -1073741824;
            int MAX_SCAN_Y = 1073741823;
            this.bounds_voxelGrid = new Bounds3i(new Vector3i(0, -1073741824, 0), new Vector3i(1, 1073741823, 1));
         } else {
            this.bounds_voxelGrid = new Bounds3i(new Vector3i(0, minY, 0), new Vector3i(1, maxY, 1));
         }

         this.rPositions = new ArrayList<>();
      }
   }

   @Override
   public void scan(@Nonnull Scanner.Context context) {
      switch (this.strategy) {
         case DART_THROW:
            this.scanDartThrow(context);
            break;
         case PICK_VALID:
            this.scanPickValid(context);
      }
   }

   @Override
   public void scan(@Nonnull Vector3i anchor, @Nonnull Pipe.One<Vector3i> pipe) {
   }

   private void scanPickValid(@Nonnull Scanner.Context context) {
      if (this.resultsCap != 0) {
         this.rPositions.clear();
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

         int numberOfPossiblePositions = Math.max(0, scanMaxY - scanMinY);
         Vector3i patternPosition = context.position.clone();
         Pattern.Context patternContext = new Pattern.Context(patternPosition, context.materialSpace);

         for (int y = scanMinY; y < scanMaxY; y++) {
            patternPosition.y = y;
            if (context.pattern.matches(patternContext)) {
               Vector3i position = context.position.clone();
               position.setY(y);
               this.rPositions.add(position);
            }
         }

         if (!this.rPositions.isEmpty()) {
            if (this.rPositions.size() > this.resultsCap) {
               ArrayList<Integer> usedIndices = new ArrayList<>(this.resultsCap);
               FastRandom random = new FastRandom(this.rngField.get(context.position.x, context.position.y, context.position.z));

               for (int i = 0; i < this.resultsCap; i++) {
                  int pickedIndex = random.nextInt(this.rPositions.size());
                  if (!usedIndices.contains(pickedIndex)) {
                     usedIndices.add(pickedIndex);
                     context.validPositions_out.add(this.rPositions.get(pickedIndex));
                  }
               }
            }
         }
      }
   }

   public void scanDartThrow(@NonNullDecl Scanner.Context context) {
      if (this.resultsCap != 0) {
         Bounds3i bounds = context.materialSpace.getBounds();
         int scanMinY = this.isRelativeToPosition ? Math.max(context.position.y + this.minY, bounds.min.y) : Math.max(this.minY, bounds.min.y);
         int scanMaxY = this.isRelativeToPosition ? Math.min(context.position.y + this.maxY, bounds.max.y) : Math.min(this.maxY, bounds.max.y);
         int range = scanMaxY - scanMinY;
         if (range != 0) {
            int TRY_MULTIPLIER = 1;
            int numberOfTries = range * 1;
            FastRandom random = new FastRandom(this.rngField.get(context.position.x, context.position.y, context.position.z));
            ArrayList<Integer> usedYs = new ArrayList<>(this.resultsCap);
            Vector3i patternPosition = context.position.clone();
            Pattern.Context patternContext = new Pattern.Context(patternPosition, context.materialSpace);

            for (int i = 0; i < numberOfTries; i++) {
               patternPosition.y = random.nextInt(range) + scanMinY;
               if (context.pattern.matches(patternContext) && !usedYs.contains(patternPosition.y)) {
                  usedYs.add(patternPosition.y);
                  Vector3i position = patternPosition.clone();
                  context.validPositions_out.add(position);
                  if (context.validPositions_out.size() == this.resultsCap) {
                     break;
                  }
               }
            }
         }
      }
   }

   @Override
   public Bounds3i getBounds_voxelGrid() {
      return this.bounds_voxelGrid;
   }

   public static enum Strategy {
      DART_THROW,
      PICK_VALID;
   }
}
