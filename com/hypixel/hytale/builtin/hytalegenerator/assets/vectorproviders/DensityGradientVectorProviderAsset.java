package com.hypixel.hytale.builtin.hytalegenerator.assets.vectorproviders;

import com.hypixel.hytale.builtin.hytalegenerator.assets.density.ConstantDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.DensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.vectorproviders.ConstantVectorProvider;
import com.hypixel.hytale.builtin.hytalegenerator.vectorproviders.DensityGradientVectorProvider;
import com.hypixel.hytale.builtin.hytalegenerator.vectorproviders.VectorProvider;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class DensityGradientVectorProviderAsset extends VectorProviderAsset {
   @Nonnull
   public static final BuilderCodec<DensityGradientVectorProviderAsset> CODEC = BuilderCodec.builder(
         DensityGradientVectorProviderAsset.class, DensityGradientVectorProviderAsset::new, VectorProviderAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Density", DensityAsset.CODEC, true), (asset, value) -> asset.densityAsset = value, asset -> asset.densityAsset)
      .add()
      .<Double>append(
         new KeyedCodec<>("SampleDistance", BuilderCodec.DOUBLE, true), (asset, value) -> asset.sampleDistance = value, asset -> asset.sampleDistance
      )
      .addValidator(Validators.greaterThanOrEqual(0.0))
      .add()
      .build();
   private DensityAsset densityAsset = new ConstantDensityAsset();
   private double sampleDistance = 1.0;

   @Nonnull
   @Override
   public VectorProvider build(@Nonnull VectorProviderAsset.Argument argument) {
      if (this.isSkipped()) {
         return new ConstantVectorProvider(new Vector3d());
      } else {
         Density density = this.densityAsset.build(DensityAsset.from(argument));
         return new DensityGradientVectorProvider(density, this.sampleDistance);
      }
   }
}
