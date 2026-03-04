package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.CacheDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.MultiCacheDensity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class CacheDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<CacheDensityAsset> CODEC = BuilderCodec.builder(
         CacheDensityAsset.class, CacheDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Capacity", Codec.INTEGER, true), (asset, value) -> asset.capacity = value, asset -> asset.capacity)
      .addValidator(Validators.greaterThanOrEqual(0))
      .add()
      .build();
   public static int DEFAULT_CAPACITY = 3;
   private int capacity = DEFAULT_CAPACITY;

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      if (this.capacity <= 0) {
         return this.build(argument);
      } else {
         return (Density)(this.capacity == 1
            ? new CacheDensity(this.buildFirstInput(argument))
            : new MultiCacheDensity(this.buildFirstInput(argument), this.capacity));
      }
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
   }
}
