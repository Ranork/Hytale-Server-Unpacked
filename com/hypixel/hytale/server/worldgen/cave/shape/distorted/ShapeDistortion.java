package com.hypixel.hytale.server.worldgen.cave.shape.distorted;

import com.hypixel.hytale.procedurallib.logic.ConstantNoise;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import com.hypixel.hytale.procedurallib.property.SingleNoiseProperty;
import javax.annotation.Nullable;

public class ShapeDistortion {
   private static final NoiseProperty DEFAULT_NOISE = new SingleNoiseProperty(new ConstantNoise(1.0));
   public static final ShapeDistortion DEFAULT = new ShapeDistortion(DEFAULT_NOISE, DEFAULT_NOISE, DEFAULT_NOISE);
   private final NoiseProperty widthNoise;
   private final NoiseProperty floorNoise;
   private final NoiseProperty ceilingNoise;

   public ShapeDistortion(NoiseProperty widthNoise, NoiseProperty floorNoise, NoiseProperty ceilingNoise) {
      this.widthNoise = widthNoise;
      this.floorNoise = floorNoise;
      this.ceilingNoise = ceilingNoise;
   }

   public double getWidthFactor(int seed, double x, double z) {
      return this.widthNoise.get(seed, x, z);
   }

   public double getFloorFactor(int seed, double x, double z) {
      return this.floorNoise.get(seed, x, z);
   }

   public double getCeilingFactor(int seed, double x, double z) {
      return this.ceilingNoise.get(seed, x, z);
   }

   public static ShapeDistortion of(@Nullable NoiseProperty widthNoise, @Nullable NoiseProperty floorNoise, @Nullable NoiseProperty ceilingNoise) {
      widthNoise = widthNoise == null ? DEFAULT_NOISE : widthNoise;
      floorNoise = floorNoise == null ? DEFAULT_NOISE : floorNoise;
      ceilingNoise = ceilingNoise == null ? DEFAULT_NOISE : ceilingNoise;
      return widthNoise == DEFAULT_NOISE && floorNoise == DEFAULT_NOISE && ceilingNoise == DEFAULT_NOISE
         ? DEFAULT
         : new ShapeDistortion(widthNoise, floorNoise, ceilingNoise);
   }
}
