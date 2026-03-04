package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.Noise3dDensity;
import com.hypixel.hytale.builtin.hytalegenerator.fields.noise.SimplexNoiseField;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class SimplexNoise3DDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<SimplexNoise3DDensityAsset> CODEC = BuilderCodec.builder(
         SimplexNoise3DDensityAsset.class, SimplexNoise3DDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Lacunarity", Codec.DOUBLE, true), (asset, lacunarity) -> asset.lacunarity = lacunarity, asset -> asset.lacunarity)
      .addValidator(Validators.greaterThan(0.0))
      .add()
      .<Double>append(new KeyedCodec<>("Persistence", Codec.DOUBLE, true), (asset, persistence) -> asset.persistence = persistence, asset -> asset.persistence)
      .addValidator(Validators.greaterThan(0.0))
      .add()
      .<Double>append(new KeyedCodec<>("ScaleXZ", Codec.DOUBLE, true), (asset, scale) -> asset.scaleXZ = scale, asset -> asset.scaleXZ)
      .addValidator(Validators.greaterThan(0.0))
      .add()
      .<Double>append(new KeyedCodec<>("ScaleY", Codec.DOUBLE, true), (asset, scale) -> asset.scaleY = scale, asset -> asset.scaleY)
      .addValidator(Validators.greaterThan(0.0))
      .add()
      .<Integer>append(new KeyedCodec<>("Octaves", Codec.INTEGER, true), (asset, octaves) -> asset.octaves = octaves, asset -> asset.octaves)
      .addValidator(Validators.greaterThan(0))
      .add()
      .append(new KeyedCodec<>("Seed", Codec.STRING, true), (asset, seed) -> asset.seedKey = seed, asset -> asset.seedKey)
      .add()
      .build();
   private double lacunarity = 1.0;
   private double persistence = 1.0;
   private double scaleXZ = 1.0;
   private double scaleY = 1.0;
   private int octaves = 1;
   private String seedKey = "A";

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      if (this.isSkipped()) {
         return new ConstantValueDensity(0.0);
      } else {
         SeedBox childSeed = argument.parentSeed.child(this.seedKey);
         SimplexNoiseField noise = SimplexNoiseField.builder()
            .withAmplitudeMultiplier(this.persistence)
            .withFrequencyMultiplier(this.lacunarity)
            .withScale(this.scaleXZ, this.scaleY, this.scaleXZ, this.scaleXZ)
            .withSeed(childSeed.createSupplier().get().intValue())
            .withNumberOfOctaves(this.octaves)
            .build();
         return new Noise3dDensity(noise);
      }
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
   }
}
