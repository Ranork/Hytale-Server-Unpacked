package com.hypixel.hytale.procedurallib.property;

import javax.annotation.Nonnull;

public class ScaleNoiseProperty implements NoiseProperty {
   protected final NoiseProperty noiseProperty;
   protected final double scaleX;
   protected final double scaleY;
   protected final double scaleZ;

   public ScaleNoiseProperty(NoiseProperty noiseProperty, double scale) {
      this(noiseProperty, scale, scale, scale);
   }

   public ScaleNoiseProperty(NoiseProperty noiseProperty, double scaleX, double scaleY, double scaleZ) {
      this.noiseProperty = noiseProperty;
      this.scaleX = scaleX;
      this.scaleY = scaleY;
      this.scaleZ = scaleZ;
   }

   public NoiseProperty getNoiseProperty() {
      return this.noiseProperty;
   }

   public double getScaleX() {
      return this.scaleX;
   }

   public double getScaleY() {
      return this.scaleY;
   }

   public double getScaleZ() {
      return this.scaleZ;
   }

   @Override
   public double get(int seed, double x, double y) {
      return this.noiseProperty.get(seed, x * this.scaleX, y * this.scaleY);
   }

   @Override
   public double get(int seed, double x, double y, double z) {
      return this.noiseProperty.get(seed, x * this.scaleX, y * this.scaleY, z * this.scaleZ);
   }

   @Nonnull
   @Override
   public String toString() {
      return "ScaleNoiseProperty{noiseProperty=" + this.noiseProperty + ", scaleX=" + this.scaleX + ", scaleY=" + this.scaleY + ", scaleZ=" + this.scaleZ + "}";
   }
}
