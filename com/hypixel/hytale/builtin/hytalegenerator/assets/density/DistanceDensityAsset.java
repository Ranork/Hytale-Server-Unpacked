package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.ConstantCurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.CurveAsset;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.DistanceDensity;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class DistanceDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<DistanceDensityAsset> CODEC = BuilderCodec.builder(
         DistanceDensityAsset.class, DistanceDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Curve", CurveAsset.CODEC, false), (t, k) -> t.densityCurveAsset = k, k -> k.densityCurveAsset)
      .add()
      .build();
   private CurveAsset densityCurveAsset = new ConstantCurveAsset();

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      return (Density)(!this.isSkipped() && this.densityCurveAsset != null
         ? new DistanceDensity(this.densityCurveAsset.build())
         : new ConstantValueDensity(0.0));
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
      this.densityCurveAsset.cleanUp();
   }
}
