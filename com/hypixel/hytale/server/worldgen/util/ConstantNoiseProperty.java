package com.hypixel.hytale.server.worldgen.util;

import com.hypixel.hytale.procedurallib.logic.ConstantNoise;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import com.hypixel.hytale.procedurallib.property.SingleNoiseProperty;

public final class ConstantNoiseProperty {
   private static final ConstantNoise DEFAULT_ZERO_NOISE = new ConstantNoise(0.0);
   public static final NoiseProperty DEFAULT_ZERO = new SingleNoiseProperty(0, DEFAULT_ZERO_NOISE);
   private static final ConstantNoise DEFAULT_ONE_NOISE = new ConstantNoise(1.0);
   public static final NoiseProperty DEFAULT_ONE = new SingleNoiseProperty(0, DEFAULT_ONE_NOISE);

   private ConstantNoiseProperty() {
      throw new UnsupportedOperationException();
   }
}
