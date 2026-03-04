package com.hypixel.hytale.procedurallib.logic;

import javax.annotation.Nonnull;

public class DoubleArray {
   public static class Double2 {
      public final double x;
      public final double y;

      public Double2(double x, double y) {
         this.x = x;
         this.y = y;
      }

      @Nonnull
      @Override
      public String toString() {
         return "DoubleArray.Double2{x=" + this.x + ", y=" + this.y + "}";
      }
   }

   public static class Double3 {
      public final double x;
      public final double y;
      public final double z;

      public Double3(double x, double y, double z) {
         this.x = x;
         this.y = y;
         this.z = z;
      }

      @Nonnull
      @Override
      public String toString() {
         return "DoubleArray.Double3{x=" + this.x + ", y=" + this.y + ", z=" + this.z + "}";
      }
   }
}
