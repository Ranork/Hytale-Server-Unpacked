package com.hypixel.hytale.procedurallib.logic;

import com.hypixel.hytale.procedurallib.NoiseFunction;
import com.hypixel.hytale.procedurallib.logic.cell.CellDistanceFunction;
import com.hypixel.hytale.procedurallib.logic.cell.evaluator.PointEvaluator;
import javax.annotation.Nonnull;

public abstract class DistanceNoise implements NoiseFunction {
   protected final CellDistanceFunction cellDistanceFunction;
   protected final PointEvaluator pointEvaluator;
   protected final DistanceNoise.Distance2Function distance2Function;

   public DistanceNoise(CellDistanceFunction cellDistanceFunction, PointEvaluator pointEvaluator, DistanceNoise.Distance2Function distance2Function) {
      this.cellDistanceFunction = cellDistanceFunction;
      this.pointEvaluator = pointEvaluator;
      this.distance2Function = distance2Function;
   }

   public CellDistanceFunction getCellDistanceFunction() {
      return this.cellDistanceFunction;
   }

   public DistanceNoise.Distance2Function getDistance2Function() {
      return this.distance2Function;
   }

   @Override
   public double get(int seed, int offsetSeed, double x, double y) {
      x = this.cellDistanceFunction.scale(x);
      y = this.cellDistanceFunction.scale(y);
      int xr = this.cellDistanceFunction.getCellX(x, y);
      int yr = this.cellDistanceFunction.getCellY(x, y);
      ResultBuffer.ResultBuffer2d buffer = this.localBuffer2d();
      buffer.distance = Double.POSITIVE_INFINITY;
      buffer.distance2 = Double.POSITIVE_INFINITY;
      this.cellDistanceFunction.transition2D(offsetSeed, x, y, xr, yr, buffer, this.pointEvaluator);
      buffer.distance = Math.sqrt(buffer.distance);
      buffer.distance2 = Math.sqrt(buffer.distance2);
      return GeneralNoise.limit(this.distance2Function.eval(buffer.distance, buffer.distance2)) * 2.0 - 1.0;
   }

   @Override
   public double get(int seed, int offsetSeed, double x, double y, double z) {
      x = this.cellDistanceFunction.scale(x);
      y = this.cellDistanceFunction.scale(y);
      z = this.cellDistanceFunction.scale(z);
      int xr = this.cellDistanceFunction.getCellX(x, y, z);
      int yr = this.cellDistanceFunction.getCellY(x, y, z);
      int zr = this.cellDistanceFunction.getCellZ(x, y, z);
      ResultBuffer.ResultBuffer3d buffer = this.localBuffer3d();
      buffer.distance = Double.POSITIVE_INFINITY;
      buffer.distance2 = Double.POSITIVE_INFINITY;
      this.cellDistanceFunction.transition3D(offsetSeed, x, y, z, xr, yr, zr, buffer, this.pointEvaluator);
      buffer.distance = Math.sqrt(buffer.distance);
      buffer.distance2 = Math.sqrt(buffer.distance2);
      return GeneralNoise.limit(this.distance2Function.eval(buffer.distance, buffer.distance2)) * 2.0 - 1.0;
   }

   protected abstract ResultBuffer.ResultBuffer2d localBuffer2d();

   protected abstract ResultBuffer.ResultBuffer3d localBuffer3d();

   @Nonnull
   @Override
   public String toString() {
      return "DistanceNoise{cellDistanceFunction="
         + this.cellDistanceFunction
         + ", pointEvaluator="
         + this.pointEvaluator
         + ", distance2Function="
         + this.distance2Function
         + "}";
   }

   @FunctionalInterface
   public interface Distance2Function {
      double eval(double var1, double var3);
   }

   public static enum Distance2Mode {
      ADD(new DistanceNoise.Distance2Function() {
         @Override
         public double eval(double distance, double distance2) {
            return distance + distance2;
         }

         @Nonnull
         @Override
         public String toString() {
            return "AddDistance2Function{}";
         }
      }),
      SUB(new DistanceNoise.Distance2Function() {
         @Override
         public double eval(double distance, double distance2) {
            return distance2 - distance;
         }

         @Nonnull
         @Override
         public String toString() {
            return "SubDistance2Function{}";
         }
      }),
      MUL(new DistanceNoise.Distance2Function() {
         @Override
         public double eval(double distance, double distance2) {
            return distance * distance2;
         }

         @Nonnull
         @Override
         public String toString() {
            return "MulDistance2Function{}";
         }
      }),
      DIV(new DistanceNoise.Distance2Function() {
         @Override
         public double eval(double distance, double distance2) {
            return distance / distance2;
         }

         @Nonnull
         @Override
         public String toString() {
            return "DivDistance2Function{}";
         }
      }),
      MIN(new DistanceNoise.Distance2Function() {
         @Override
         public double eval(double distance, double distance2) {
            return distance;
         }

         @Nonnull
         @Override
         public String toString() {
            return "MinDistance2Function{}";
         }
      }),
      MAX(new DistanceNoise.Distance2Function() {
         @Override
         public double eval(double distance, double distance2) {
            return distance2;
         }

         @Nonnull
         @Override
         public String toString() {
            return "MaxDistance2Function{}";
         }
      });

      private final DistanceNoise.Distance2Function function;

      private Distance2Mode(DistanceNoise.Distance2Function function) {
         this.function = function;
      }

      public DistanceNoise.Distance2Function getFunction() {
         return this.function;
      }
   }
}
