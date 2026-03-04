package com.hypixel.hytale.procedurallib.json;

import com.hypixel.hytale.procedurallib.logic.ResultBuffer;
import com.hypixel.hytale.procedurallib.logic.cell.CellDistanceFunction;
import com.hypixel.hytale.procedurallib.logic.cell.evaluator.PointEvaluator;
import com.hypixel.hytale.procedurallib.logic.point.PointGenerator;
import javax.annotation.Nonnull;

public class SeedResourcePointGenerator extends PointGenerator {
   private final SeedResource seedResource;

   public SeedResourcePointGenerator(int seedOffset, CellDistanceFunction cellDistanceFunction, PointEvaluator pointEvaluator, SeedResource seedResource) {
      super(seedOffset, cellDistanceFunction, pointEvaluator);
      this.seedResource = seedResource;
   }

   @Nonnull
   @Override
   protected ResultBuffer.Bounds2d localBounds2d() {
      return this.seedResource.localBounds2d();
   }

   @Nonnull
   @Override
   protected ResultBuffer.ResultBuffer2d localBuffer2d() {
      return this.seedResource.localBuffer2d();
   }

   @Nonnull
   @Override
   protected ResultBuffer.ResultBuffer3d localBuffer3d() {
      return this.seedResource.localBuffer3d();
   }
}
