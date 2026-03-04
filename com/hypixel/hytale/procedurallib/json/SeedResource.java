package com.hypixel.hytale.procedurallib.json;

import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface SeedResource {
   String INFO_SEED_REPORT = "Seed Value: %s for seed %s / %s";
   String INFO_SEED_OVERWRITE_REPORT = "Seed Value: %s for seed %s / %s overwritten by %s";

   @Nonnull
   default ResultBuffer.Bounds2d localBounds2d() {
      return ResultBuffer.bounds2d;
   }

   @Nonnull
   default ResultBuffer.ResultBuffer2d localBuffer2d() {
      return ResultBuffer.buffer2d;
   }

   @Nonnull
   default ResultBuffer.ResultBuffer3d localBuffer3d() {
      return ResultBuffer.buffer3d;
   }

   default boolean shouldReportSeeds() {
      return false;
   }

   default void reportSeeds(int seedVal, String original, String seed, @Nullable String overwritten) {
      if (this.shouldReportSeeds()) {
         if (overwritten == null) {
            this.writeSeedReport(String.format("Seed Value: %s for seed %s / %s", seedVal, original, seed));
         } else {
            this.writeSeedReport(String.format("Seed Value: %s for seed %s / %s overwritten by %s", seedVal, original, seed, overwritten));
         }
      }
   }

   default void writeSeedReport(String seedReport) {
      System.out.println(seedReport);
   }
}
