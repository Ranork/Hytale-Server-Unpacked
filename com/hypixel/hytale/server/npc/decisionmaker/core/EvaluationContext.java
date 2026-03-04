package com.hypixel.hytale.server.npc.decisionmaker.core;

public class EvaluationContext {
   private double minimumUtility;
   private double minimumWeightCoefficient;
   private float predictability;
   private long lastUsedNanos;

   public double getMinimumUtility() {
      return this.minimumUtility;
   }

   public void setMinimumUtility(double minimumUtility) {
      if (!(minimumUtility >= 1.0) && !(minimumUtility < 0.0)) {
         this.minimumUtility = minimumUtility;
      } else {
         throw new IllegalArgumentException("Minimum utility must be greater than or equal to 0 and less than 1!");
      }
   }

   public double getMinimumWeightCoefficient() {
      return this.minimumWeightCoefficient;
   }

   public void setMinimumWeightCoefficient(double minimumWeightCoefficient) {
      if (minimumWeightCoefficient < 0.0) {
         throw new IllegalArgumentException("Minimum weight coefficient must be greater than or equal to 0!");
      } else {
         this.minimumWeightCoefficient = minimumWeightCoefficient;
      }
   }

   public float getPredictability() {
      return this.predictability;
   }

   public void setPredictability(float predictability) {
      if (!(predictability > 1.0F) && !(predictability < 0.0F)) {
         this.predictability = predictability;
      } else {
         throw new IllegalArgumentException("Predictability must be a value between 0 and 1!");
      }
   }

   public long getLastUsedNanos() {
      return this.lastUsedNanos;
   }

   public void setLastUsedNanos(long lastUsedNanos) {
      this.lastUsedNanos = lastUsedNanos;
   }

   public void reset() {
      this.minimumUtility = 0.0;
      this.minimumWeightCoefficient = 0.0;
      this.predictability = 1.0F;
      this.lastUsedNanos = Evaluator.NOT_USED;
   }
}
