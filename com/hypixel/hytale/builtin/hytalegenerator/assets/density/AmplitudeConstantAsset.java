package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.AmplitudeConstantDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class AmplitudeConstantAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<AmplitudeConstantAsset> CODEC = BuilderCodec.builder(
         AmplitudeConstantAsset.class, AmplitudeConstantAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Value", Codec.DOUBLE, true), (t, k) -> t.value = k, t -> t.value)
      .add()
      .build();
   private double value = 0.0;

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      return (Density)(this.isSkipped() ? new ConstantValueDensity(0.0) : new AmplitudeConstantDensity(this.value, this.buildFirstInput(argument)));
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
   }
}
