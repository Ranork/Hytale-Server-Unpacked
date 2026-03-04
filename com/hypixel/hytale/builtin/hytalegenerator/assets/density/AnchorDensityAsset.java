package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.AnchorDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class AnchorDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<AnchorDensityAsset> CODEC = BuilderCodec.builder(
         AnchorDensityAsset.class, AnchorDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Reversed", Codec.BOOLEAN, false), (t, k) -> t.isReversed = k, k -> k.isReversed)
      .add()
      .build();
   private boolean isReversed = false;

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      return (Density)(this.isSkipped() ? new ConstantValueDensity(0.0) : new AnchorDensity(this.buildFirstInput(argument), this.isReversed));
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
   }
}
