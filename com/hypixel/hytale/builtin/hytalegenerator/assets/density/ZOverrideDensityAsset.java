package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ZOverrideDensity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class ZOverrideDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<ZOverrideDensityAsset> CODEC = BuilderCodec.builder(
         ZOverrideDensityAsset.class, ZOverrideDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Value", Codec.DOUBLE, true), (t, k) -> t.value = k, t -> t.value)
      .add()
      .build();
   private double value;

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      if (this.isSkipped()) {
         return new ConstantValueDensity(0.0);
      } else {
         Density input = this.buildFirstInput(argument);
         return (Density)(input == null ? new ConstantValueDensity(0.0) : new ZOverrideDensity(input, this.value));
      }
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
   }
}
