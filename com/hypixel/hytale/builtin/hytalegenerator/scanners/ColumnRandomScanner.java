package com.hypixel.hytale.builtin.hytalegenerator.scanners;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.SpaceSize;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.SeedGenerator;
import com.hypixel.hytale.builtin.hytalegenerator.patterns.Pattern;
import com.hypixel.hytale.math.util.FastRandom;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

public class ColumnRandomScanner extends Scanner {
   private final int minY;
   private final int maxY;
   private final boolean isRelativeToPosition;
   private final double baseHeight;
   private final int resultsCap;
   @Nonnull
   private final SeedGenerator seedGenerator;
   @Nonnull
   private final ColumnRandomScanner.Strategy strategy;
   @Nonnull
   private final SpaceSize scanSpaceSize;

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
         this.seedGenerator = new SeedGenerator(seed);
         this.strategy = strategy;
         this.scanSpaceSize = new SpaceSize(new Vector3i(0, 0, 0), new Vector3i(1, 0, 1));
      }
   }

   @Nonnull
   @Override
   public List<Vector3i> scan(@Nonnull Scanner.Context context) {
      return switch (this.strategy) {
         case DART_THROW -> this.scanDartThrow(context);
         case PICK_VALID -> this.scanPickValid(context);
      };
   }

   @Nonnull
   private List<Vector3i> scanPickValid(@Nonnull Scanner.Context context) {
      if (this.resultsCap == 0) {
         return Collections.emptyList();
      } else {
         int scanMinY;
         int scanMaxY;
         if (this.isRelativeToPosition) {
            scanMinY = Math.max(context.position.y + this.minY, context.materialSpace.minY());
            scanMaxY = Math.min(context.position.y + this.maxY, context.materialSpace.maxY());
         } else {
            int bedY = (int)this.baseHeight;
            scanMinY = Math.max(bedY + this.minY, context.materialSpace.minY());
            scanMaxY = Math.min(bedY + this.maxY, context.materialSpace.maxY());
         }

         int numberOfPossiblePositions = Math.max(0, scanMaxY - scanMinY);
         ArrayList<Vector3i> validPositions = new ArrayList<>(numberOfPossiblePositions);
         Vector3i patternPosition = context.position.clone();
         Pattern.Context patternContext = new Pattern.Context(patternPosition, context.materialSpace);

         for (int y = scanMinY; y < scanMaxY; y++) {
            patternPosition.y = y;
            if (context.pattern.matches(patternContext)) {
               Vector3i position = context.position.clone();
               position.setY(y);
               validPositions.add(position);
            }
         }

         if (validPositions.isEmpty()) {
            return validPositions;
         } else if (validPositions.size() <= this.resultsCap) {
            return validPositions;
         } else {
            ArrayList<Integer> usedIndices = new ArrayList<>(this.resultsCap);
            ArrayList<Vector3i> outPositions = new ArrayList<>(this.resultsCap);
            FastRandom random = new FastRandom(this.seedGenerator.seedAt((long)context.position.x, (long)context.position.y, (long)context.position.z));

            for (int i = 0; i < this.resultsCap; i++) {
               int pickedIndex = random.nextInt(validPositions.size());
               if (!usedIndices.contains(pickedIndex)) {
                  usedIndices.add(pickedIndex);
                  outPositions.add(validPositions.get(pickedIndex));
               }
            }

            return outPositions;
         }
      }
   }

   @Nonnull
   private List<Vector3i> scanDartThrow(@Nonnull Scanner.Context context) {
      if (this.resultsCap == 0) {
         return Collections.emptyList();
      } else {
         int scanMinY = this.isRelativeToPosition
            ? Math.max(context.position.y + this.minY, context.materialSpace.minY())
            : Math.max(this.minY, context.materialSpace.minY());
         int scanMaxY = this.isRelativeToPosition
            ? Math.min(context.position.y + this.maxY, context.materialSpace.maxY())
            : Math.min(this.maxY, context.materialSpace.maxY());
         int range = scanMaxY - scanMinY;
         if (range == 0) {
            return Collections.emptyList();
         } else {
            int TRY_MULTIPLIER = 1;
            int numberOfTries = range * 1;
            ArrayList<Vector3i> validPositions = new ArrayList<>(this.resultsCap);
            FastRandom random = new FastRandom(this.seedGenerator.seedAt((long)context.position.x, (long)context.position.y, (long)context.position.z));
            ArrayList<Integer> usedYs = new ArrayList<>(this.resultsCap);
            Vector3i patternPosition = context.position.clone();
            Pattern.Context patternContext = new Pattern.Context(patternPosition, context.materialSpace);

            for (int i = 0; i < numberOfTries; i++) {
               patternPosition.y = random.nextInt(range) + scanMinY;
               if (context.pattern.matches(patternContext) && !usedYs.contains(patternPosition.y)) {
                  usedYs.add(patternPosition.y);
                  Vector3i position = patternPosition.clone();
                  validPositions.add(position);
                  if (validPositions.size() == this.resultsCap) {
                     break;
                  }
               }
            }

            return validPositions;
         }
      }
   }

   @Nonnull
   @Override
   public SpaceSize scanSpace() {
      return this.scanSpaceSize.clone();
   }

   public static enum Strategy {
      DART_THROW,
      PICK_VALID;
   }
}
