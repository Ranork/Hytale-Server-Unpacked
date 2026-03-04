package com.hypixel.hytale.builtin.hytalegenerator.assets.curves;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import javax.annotation.Nonnull;

public class ConstantCurveAsset extends CurveAsset {
   @Nonnull
   public static final BuilderCodec<ConstantCurveAsset> CODEC = BuilderCodec.builder(
         ConstantCurveAsset.class, ConstantCurveAsset::new, CurveAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Value", Codec.DOUBLE, true), (asset, value) -> asset.value = value, asset -> asset.value)
      .add()
      .build();
   private double value = 0.0;

   public ConstantCurveAsset() {
   }

   public ConstantCurveAsset(double value) {
      this.value = value;
   }

   @Nonnull
   @Override
   public Double2DoubleFunction build() {
      return in -> this.value;
   }

   @Override
   public void cleanUp() {
   }
}
