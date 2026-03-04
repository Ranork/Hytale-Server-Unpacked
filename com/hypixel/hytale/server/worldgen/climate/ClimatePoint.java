package com.hypixel.hytale.server.worldgen.climate;

public class ClimatePoint {
   public static final ClimatePoint[] EMPTY_ARRAY = new ClimatePoint[0];
   public double temperature;
   public double intensity;
   public double modifier;

   public ClimatePoint(double temperature, double intensity) {
      this(temperature, intensity, 1.0);
   }

   public ClimatePoint(double temperature, double intensity, double modifier) {
      this.temperature = temperature;
      this.intensity = intensity;
      this.modifier = modifier;
   }

   @Override
   public String toString() {
      return String.format("t=%.3f, i=%.3f, mod=%.3f", this.temperature, this.intensity, this.modifier);
   }
}
