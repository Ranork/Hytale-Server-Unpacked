package com.hypixel.hytale.procedurallib.logic.cell;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public enum DistanceCalculationMode {
   EUCLIDEAN(new PointDistanceFunction() {
      @Override
      public double distance2D(double deltaX, double deltaY) {
         return deltaX * deltaX + deltaY * deltaY;
      }

      @Override
      public double distance3D(double deltaX, double deltaY, double deltaZ) {
         return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
      }

      @Nonnull
      @Override
      public String toString() {
         return "EuclideanPointDistanceFunction{}";
      }
   }),
   MANHATTAN(new PointDistanceFunction() {
      @Override
      public double distance2D(double deltaX, double deltaY) {
         return Math.abs(deltaX) + Math.abs(deltaY);
      }

      @Override
      public double distance3D(double deltaX, double deltaY, double deltaZ) {
         return Math.abs(deltaX) + Math.abs(deltaY) + Math.abs(deltaZ);
      }

      @Nonnull
      @Override
      public String toString() {
         return "ManhattanPointDistanceFunction{}";
      }
   }),
   NATURAL(new PointDistanceFunction() {
      @Override
      public double distance2D(double deltaX, double deltaY) {
         return Math.abs(deltaX) + Math.abs(deltaY) + deltaX * deltaX + deltaY * deltaY;
      }

      @Override
      public double distance3D(double deltaX, double deltaY, double deltaZ) {
         return Math.abs(deltaX) + Math.abs(deltaY) + Math.abs(deltaZ) + deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
      }

      @Nonnull
      @Override
      public String toString() {
         return "NaturalPointDistanceFunction{}";
      }
   }),
   MAX(new PointDistanceFunction() {
      @Override
      public double distance2D(double deltaX, double deltaY) {
         return Math.max(Math.abs(deltaX), Math.abs(deltaY));
      }

      @Override
      public double distance3D(double deltaX, double deltaY, double deltaZ) {
         return Math.max(Math.abs(deltaX), Math.max(Math.abs(deltaY), Math.abs(deltaZ)));
      }

      @Nonnull
      @Override
      public String toString() {
         return "MaxPointDistanceFunction{}";
      }
   });

   protected static final DistanceCalculationMode[] VALUES = values();
   private final PointDistanceFunction function;

   private DistanceCalculationMode(PointDistanceFunction function) {
      this.function = function;
   }

   public PointDistanceFunction getFunction() {
      return this.function;
   }

   @Nullable
   public static DistanceCalculationMode from(PointDistanceFunction function) {
      for (DistanceCalculationMode mode : VALUES) {
         if (mode.function == function) {
            return mode;
         }
      }

      return null;
   }
}
