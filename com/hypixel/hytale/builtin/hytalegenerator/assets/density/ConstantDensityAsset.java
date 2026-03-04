package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class ConstantDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<ConstantDensityAsset> CODEC = BuilderCodec.builder(
         ConstantDensityAsset.class, ConstantDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Value", Codec.DOUBLE, true), (t, k) -> t.value = k, k -> k.value)
      .add()
      .build();
   private double value;

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      return this.isSkipped() ? new ConstantValueDensity(0.0) : new ConstantValueDensity(this.value);
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
   }
}
