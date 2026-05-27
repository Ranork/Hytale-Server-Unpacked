package com.hypixel.hytale.builtin.worldgen.modifier.content.common;

import com.hypixel.hytale.builtin.worldgen.modifier.content.Codecs;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.range.FloatRange;
import com.hypixel.hytale.procedurallib.condition.DoubleThreshold;
import com.hypixel.hytale.procedurallib.condition.DoubleThresholdCondition;
import com.hypixel.hytale.procedurallib.condition.ICoordinateCondition;
import com.hypixel.hytale.procedurallib.condition.NoiseMaskCondition;
import com.hypixel.hytale.procedurallib.json.SeedString;
import com.hypixel.hytale.procedurallib.logic.SimplexNoise;
import com.hypixel.hytale.procedurallib.property.FractalNoiseProperty;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import com.hypixel.hytale.procedurallib.property.ScaleNoiseProperty;
import com.hypixel.hytale.server.worldgen.SeedStringResource;
import com.hypixel.hytale.server.worldgen.util.ConstantNoiseProperty;

public class NoiseMask {
   public static final BuilderCodec<NoiseMask> CODEC = BuilderCodec.builder(NoiseMask.class, NoiseMask::new)
      .documentation("Defines a noise mask that uses fractal simplex noise to control placement density within a configurable threshold range")
      .<String>append(new KeyedCodec<>("Seed", Codec.STRING), (t, v) -> t.seed = v, t -> t.seed)
      .documentation("Seed for the mask noise")
      .add()
      .<Double>append(new KeyedCodec<>("Scale", Codec.DOUBLE), (t, v) -> t.scale = v, t -> t.scale)
      .addValidator(Validators.greaterThan(0.0))
      .documentation("The scale of the mask noise")
      .add()
      .<Integer>append(new KeyedCodec<>("Octaves", Codec.INTEGER), (t, v) -> t.octaves = v, t -> t.octaves)
      .addValidator(Validators.range(0, 5))
      .documentation("The number of octaves of the noise to use")
      .add()
      .<Double>append(new KeyedCodec<>("Lacunarity", Codec.DOUBLE), (t, v) -> t.lacunarity = v, t -> t.lacunarity)
      .addValidator(Validators.range(1.0, 10.0))
      .documentation("The cumulative frequency multiplier for each noise octave")
      .add()
      .<Double>append(new KeyedCodec<>("Persistence", Codec.DOUBLE), (t, v) -> t.persistence = v, t -> t.persistence)
      .addValidator(Validators.range(0.0, 1.0))
      .documentation("The cumulative gain multiplier for each noise octave")
      .add()
      .<FloatRange>append(new KeyedCodec<>("Density", Codecs.DENSITY_RANGE), (t, v) -> t.density = v, t -> t.density)
      .documentation("Threshold range for the mask. Noise values outside the range result in the prefab position being occluded")
      .add()
      .build();
   public static final NoiseMask DEFAULT = new NoiseMask();
   public static final NoiseMask ZERO = new NoiseMask() {
      @Override
      public NoiseProperty buildNoise(SeedString<SeedStringResource> seed) {
         return ConstantNoiseProperty.DEFAULT_ZERO;
      }
   };
   protected String seed = "Default_Mask";
   protected double scale = 128.0;
   protected int octaves = 2;
   protected double lacunarity = 4.0;
   protected double persistence = 0.6;
   protected FloatRange density = new FloatRange(0.0F, 1.0F);

   public NoiseProperty buildNoise(SeedString<SeedStringResource> seed) {
      return new ScaleNoiseProperty(
         new FractalNoiseProperty(
            seed.append(this.seed).hashCode(),
            SimplexNoise.INSTANCE,
            FractalNoiseProperty.FractalMode.FBM.getFunction(),
            this.octaves,
            this.lacunarity,
            this.persistence
         ),
         this.scale == 0.0 ? 0.0 : 1.0 / this.scale
      );
   }

   public ICoordinateCondition build(SeedString<SeedStringResource> seed) {
      return new NoiseMaskCondition(
         this.buildNoise(seed), new DoubleThresholdCondition(new DoubleThreshold.Single(this.density.getInclusiveMin(), this.density.getInclusiveMax()))
      );
   }
}
