package com.hypixel.hytale.procedurallib.logic;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.procedurallib.NoiseFunction;
import com.hypixel.hytale.procedurallib.condition.IIntCondition;
import com.hypixel.hytale.procedurallib.logic.cell.CellDistanceFunction;
import com.hypixel.hytale.procedurallib.logic.cell.evaluator.PointEvaluator;
import com.hypixel.hytale.procedurallib.property.NoiseFormulaProperty;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import javax.annotation.Nonnull;

public class BranchNoise implements NoiseFunction {
   protected final CellDistanceFunction parentFunction;
   protected final PointEvaluator parentEvaluator;
   protected final double parentValue;
   protected final double emptyValue;
   protected final IDoubleRange parentFade;
   protected final IIntCondition parentDensity;
   protected final DistanceNoise.Distance2Function distance2Function;
   protected final NoiseFormulaProperty.NoiseFormula.Formula noiseFormula;
   protected final CellDistanceFunction lineFunction;
   protected final PointEvaluator lineEvaluator;
   protected final double lineScale;
   protected final IDoubleRange lineThickness;

   public BranchNoise(
      CellDistanceFunction parentFunction,
      PointEvaluator parentEvaluator,
      double parentValue,
      IDoubleRange parentFade,
      IIntCondition parentDensity,
      DistanceNoise.Distance2Function distance2Function,
      NoiseFormulaProperty.NoiseFormula.Formula noiseFormula,
      CellDistanceFunction lineFunction,
      PointEvaluator lineEvaluator,
      double lineScale,
      IDoubleRange lineThickness
   ) {
      this.parentFunction = parentFunction;
      this.parentEvaluator = parentEvaluator;
      this.parentValue = parentValue;
      this.emptyValue = toOutputRange(parentValue);
      this.parentFade = parentFade;
      this.parentDensity = parentDensity;
      this.distance2Function = distance2Function;
      this.noiseFormula = noiseFormula;
      this.lineFunction = lineFunction;
      this.lineEvaluator = lineEvaluator;
      this.lineScale = lineScale;
      this.lineThickness = lineThickness;
   }

   @Override
   public double get(int seed, int offsetSeed, double x, double y) {
      ResultBuffer.ResultBuffer2d parent = this.getParentNoise(offsetSeed, x, y);
      if (!this.parentDensity.eval(parent.hash)) {
         return this.emptyValue;
      } else {
         double parentDistance = this.noiseFormula.eval(this.distance2Function.eval(parent.distance, parent.distance2));
         double lineValue = this.getLineValue(offsetSeed, x, y, parent.hash, parent.x, parent.y, parentDistance, parent);
         double parentFade = this.parentFade.getValue(parentDistance);
         double noiseValue = MathUtil.lerp(this.parentValue, lineValue, parentFade);
         return toOutputRange(noiseValue);
      }
   }

   @Override
   public double get(int seed, int offsetSeed, double x, double y, double z) {
      throw new UnsupportedOperationException();
   }

   @Nonnull
   protected ResultBuffer.ResultBuffer2d localBuffer2d() {
      return ResultBuffer.buffer2d;
   }

   @Nonnull
   protected ResultBuffer.ResultBuffer2d getParentNoise(int seed, double x, double y) {
      ResultBuffer.ResultBuffer2d buffer = this.localBuffer2d();
      buffer.distance = Double.POSITIVE_INFINITY;
      buffer.distance2 = Double.POSITIVE_INFINITY;
      x = this.parentFunction.scale(x);
      y = this.parentFunction.scale(y);
      int cellX = this.parentFunction.getCellX(x, y);
      int cellY = this.parentFunction.getCellY(x, y);
      this.parentFunction.transition2D(seed, x, y, cellX, cellY, buffer, this.parentEvaluator);
      return buffer;
   }

   protected double getLineValue(
      int seed, double x, double y, int parentHash, double parentX, double parentY, double parentDistance, @Nonnull ResultBuffer.ResultBuffer2d buffer
   ) {
      double thickness = this.lineThickness.getValue(parentDistance);
      if (thickness == 0.0) {
         return 1.0;
      } else {
         buffer.distance = Double.POSITIVE_INFINITY;
         buffer.x2 = parentX;
         buffer.y2 = parentY;
         buffer.ix2 = parentHash;
         buffer.distance2 = thickness;
         x *= this.lineScale;
         y *= this.lineScale;
         x = this.lineFunction.scale(x);
         y = this.lineFunction.scale(y);
         int cellX = this.lineFunction.getCellX(x, y);
         int cellY = this.lineFunction.getCellY(x, y);
         this.lineFunction.nearest2D(seed, x, y, cellX, cellY, buffer, this.lineEvaluator);
         double distance = buffer.distance;
         return distance >= thickness * thickness ? 1.0 : Math.sqrt(distance) / thickness;
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "BranchNoise{parentFunction="
         + this.parentFunction
         + ", parentEvaluator="
         + this.parentEvaluator
         + ", parentValue="
         + this.parentValue
         + ", parentFade="
         + this.parentFade
         + ", distance2Function="
         + this.distance2Function
         + ", noiseFormula="
         + this.noiseFormula
         + ", lineFunction="
         + this.lineFunction
         + ", lineEvaluator="
         + this.lineEvaluator
         + ", lineScale="
         + this.lineScale
         + ", lineThickness="
         + this.lineThickness
         + "}";
   }

   protected static double toOutputRange(double value) {
      return 2.0 * value - 1.0;
   }
}
