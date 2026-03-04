package com.hypixel.hytale.builtin.hytalegenerator.fields.noise;

import javax.annotation.Nonnull;

public abstract class NoiseField {
   protected double scaleX = 1.0;
   protected double scaleY = 1.0;
   protected double scaleZ = 1.0;
   protected double scaleW = 1.0;

   public abstract double valueAt(double var1, double var3, double var5, double var7);

   public abstract double valueAt(double var1, double var3, double var5);

   public abstract double valueAt(double var1, double var3);

   public abstract double valueAt(double var1);

   @Nonnull
   public NoiseField setScale(double scaleX, double scaleY, double scaleZ, double scaleW) {
      this.scaleX = scaleX;
      this.scaleY = scaleY;
      this.scaleZ = scaleZ;
      this.scaleW = scaleW;
      return this;
   }

   @Nonnull
   public NoiseField setScale(double scale) {
      this.scaleX = scale;
      this.scaleY = scale;
      this.scaleZ = scale;
      this.scaleW = scale;
      return this;
   }
}
