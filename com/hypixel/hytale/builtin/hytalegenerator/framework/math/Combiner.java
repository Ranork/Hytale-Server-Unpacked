package com.hypixel.hytale.builtin.hytalegenerator.framework.math;

import javax.annotation.Nonnull;

public class Combiner {
   private final double y;
   private double value;

   public Combiner(double background, double y) {
      this.value = background;
      this.y = y;
   }

   @Nonnull
   public Combiner.Layer addLayer(double density) {
      return new Combiner.Layer(this, density);
   }

   public double getValue() {
      return this.value;
   }

   public static enum IntersectionPolicy {
      MAX_POLICY,
      MIN_POLICY;
   }

   public class Layer {
      @Nonnull
      private final Combiner parent;
      private double value;
      private double floor;
      private double ceiling;
      private double paddingFloor;
      private double paddingCeiling;
      private Combiner.IntersectionPolicy intersectionPolicy;
      private double intersectionSmoothingRange;
      private boolean withLimitsCheck;
      private boolean withPaddingCheck;
      private boolean withIntersectionPolicyCheck;
      private boolean isFinished = false;

      private Layer(@Nonnull Combiner combiner, double value) {
         if (combiner == null) {
            throw new NullPointerException();
         } else {
            this.parent = combiner;
            this.value = value;
         }
      }

      @Nonnull
      public Combiner finishLayer() {
         if (!this.withPaddingCheck || !this.withIntersectionPolicyCheck || !this.withLimitsCheck) {
            throw new IllegalStateException("incomplete");
         } else if (this.isFinished) {
            throw new IllegalStateException("method was already called");
         } else {
            this.isFinished = true;
            if (this.intersectionPolicy == Combiner.IntersectionPolicy.MAX_POLICY) {
               this.ceiling = Calculator.smoothMax(this.intersectionSmoothingRange, this.floor, this.ceiling);
            } else if (this.intersectionPolicy == Combiner.IntersectionPolicy.MIN_POLICY) {
               this.floor = Calculator.smoothMin(this.intersectionSmoothingRange, this.floor, this.ceiling);
            } else {
               this.ceiling = this.floor;
            }

            if (!(Combiner.this.y < this.floor) && !(Combiner.this.y >= this.ceiling)) {
               double floorPaddingMultiplier;
               if (this.paddingFloor == 0.0) {
                  floorPaddingMultiplier = 1.0;
               } else {
                  floorPaddingMultiplier = (Combiner.this.y - this.floor) / this.paddingFloor;
                  floorPaddingMultiplier = Calculator.clamp(0.0, floorPaddingMultiplier, 1.0);
               }

               double ceilingPaddingMultiplier;
               if (this.paddingCeiling == 0.0) {
                  ceilingPaddingMultiplier = 1.0;
               } else {
                  ceilingPaddingMultiplier = (this.ceiling - Combiner.this.y) / this.paddingCeiling;
                  ceilingPaddingMultiplier = Calculator.clamp(0.0, ceilingPaddingMultiplier, 1.0);
               }

               double paddingMultiplier = Calculator.smoothMin(0.2, floorPaddingMultiplier, ceilingPaddingMultiplier);
               this.value *= paddingMultiplier;
               this.parent.value = this.parent.value + this.value;
               return this.parent;
            } else {
               return this.parent;
            }
         }
      }

      @Nonnull
      public Combiner.Layer withLimits(double floor, double ceiling) {
         this.withLimitsCheck = true;
         this.floor = floor;
         this.ceiling = ceiling;
         return this;
      }

      @Nonnull
      public Combiner.Layer withPadding(double paddingFloor, double paddingCeiling) {
         if (!(paddingFloor < 0.0) && !(paddingCeiling < 0.0)) {
            this.withPaddingCheck = true;
            this.paddingFloor = paddingFloor;
            this.paddingCeiling = paddingCeiling;
            return this;
         } else {
            throw new IllegalArgumentException("negative padding values");
         }
      }

      @Nonnull
      public Combiner.Layer withIntersectionPolicy(@Nonnull Combiner.IntersectionPolicy policy, double smoothRange) {
         if (policy == null) {
            throw new NullPointerException();
         } else {
            this.withIntersectionPolicyCheck = true;
            this.intersectionPolicy = policy;
            this.intersectionSmoothingRange = smoothRange;
            return this;
         }
      }
   }
}
