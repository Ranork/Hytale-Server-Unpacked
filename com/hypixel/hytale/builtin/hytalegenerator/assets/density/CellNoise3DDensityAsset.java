package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.Noise3dDensity;
import com.hypixel.hytale.builtin.hytalegenerator.fields.FastNoiseLite;
import com.hypixel.hytale.builtin.hytalegenerator.fields.noise.CellNoiseField;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class CellNoise3DDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<CellNoise3DDensityAsset> CODEC = BuilderCodec.builder(
         CellNoise3DDensityAsset.class, CellNoise3DDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("ScaleX", Codec.DOUBLE, true), (asset, scale) -> asset.scaleX = scale, asset -> asset.scaleX)
      .addValidator(Validators.greaterThan(0.0))
      .add()
      .<Double>append(new KeyedCodec<>("ScaleY", Codec.DOUBLE, true), (asset, scale) -> asset.scaleY = scale, asset -> asset.scaleY)
      .addValidator(Validators.greaterThan(0.0))
      .add()
      .<Double>append(new KeyedCodec<>("ScaleZ", Codec.DOUBLE, true), (asset, scale) -> asset.scaleZ = scale, asset -> asset.scaleZ)
      .addValidator(Validators.greaterThan(0.0))
      .add()
      .append(new KeyedCodec<>("Jitter", Codec.DOUBLE, true), (asset, v) -> asset.jitter = v, asset -> asset.scaleZ)
      .add()
      .<Integer>append(new KeyedCodec<>("Octaves", Codec.INTEGER, true), (asset, octaves) -> asset.octaves = octaves, asset -> asset.octaves)
      .addValidator(Validators.greaterThan(0))
      .add()
      .append(new KeyedCodec<>("Seed", Codec.STRING, true), (asset, seed) -> asset.seedKey = seed, asset -> asset.seedKey)
      .add()
      .append(
         new KeyedCodec<>("CellType", FastNoiseLite.CellularReturnType.CODEC, true), (asset, cellType) -> asset.cellType = cellType, asset -> asset.cellType
      )
      .add()
      .build();
   private double scaleX = 1.0;
   private double scaleY = 1.0;
   private double scaleZ = 1.0;
   private double jitter = 0.5;
   private int octaves = 1;
   private String seedKey = "A";
   @Nonnull
   private FastNoiseLite.CellularReturnType cellType = FastNoiseLite.CellularReturnType.CellValue;

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      if (this.isSkipped()) {
         return new ConstantValueDensity(0.0);
      } else {
         SeedBox childSeed = argument.parentSeed.child(this.seedKey);
         CellNoiseField noise = new CellNoiseField(
            childSeed.createSupplier().get(), this.scaleX, this.scaleY, this.scaleZ, this.jitter, this.octaves, this.cellType
         );
         return new Noise3dDensity(noise);
      }
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
   }
}
