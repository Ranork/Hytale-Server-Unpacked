package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.CeilingDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class CeilingDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<CeilingDensityAsset> CODEC = BuilderCodec.builder(
         CeilingDensityAsset.class, CeilingDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Limit", Codec.DOUBLE, true), (t, k) -> t.limit = k, k -> k.limit)
      .add()
      .build();
   private double limit;

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      return (Density)(this.isSkipped() ? new ConstantValueDensity(0.0) : new CeilingDensity(this.limit, this.buildFirstInput(argument)));
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
   }
}
