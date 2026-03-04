package com.hypixel.hytale.procedurallib.logic.cell.jitter;

import com.hypixel.hytale.procedurallib.logic.DoubleArray;
import javax.annotation.Nonnull;

public class ConstantCellJitter implements CellJitter {
   protected final double jitterX;
   protected final double jitterY;
   protected final double jitterZ;

   public ConstantCellJitter(double jitterX, double jitterY, double jitterZ) {
      this.jitterX = jitterX;
      this.jitterY = jitterY;
      this.jitterZ = jitterZ;
   }

   @Override
   public double getMaxX() {
      return this.jitterX;
   }

   @Override
   public double getMaxY() {
      return this.jitterY;
   }

   @Override
   public double getMaxZ() {
      return this.jitterZ;
   }

   @Override
   public double getPointX(int cx, @Nonnull DoubleArray.Double2 vec) {
      return cx + vec.x * this.jitterX;
   }

   @Override
   public double getPointY(int cy, @Nonnull DoubleArray.Double2 vec) {
      return cy + vec.y * this.jitterY;
   }

   @Override
   public double getPointX(int cx, @Nonnull DoubleArray.Double3 vec) {
      return cx + vec.x * this.jitterX;
   }

   @Override
   public double getPointY(int cy, @Nonnull DoubleArray.Double3 vec) {
      return cy + vec.y * this.jitterY;
   }

   @Override
   public double getPointZ(int cz, @Nonnull DoubleArray.Double3 vec) {
      return cz + vec.z * this.jitterZ;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ConstantCellJitter{jitterX=" + this.jitterX + ", jitterY=" + this.jitterY + ", jitterZ=" + this.jitterZ + "}";
   }
}
