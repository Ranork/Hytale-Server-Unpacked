package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.SmoothFloorDensity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class SmoothFloorDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<SmoothFloorDensityAsset> CODEC = BuilderCodec.builder(
         SmoothFloorDensityAsset.class, SmoothFloorDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Limit", Codec.DOUBLE, true), (t, k) -> t.limit = k, k -> k.limit)
      .add()
      .<Double>append(new KeyedCodec<>("SmoothRange", Codec.DOUBLE, true), (t, k) -> t.smoothRange = k, k -> k.smoothRange)
      .addValidator(Validators.greaterThanOrEqual(0.0))
      .add()
      .build();
   private double smoothRange = 1.0;
   private double limit;

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      return (Density)(this.isSkipped() ? new ConstantValueDensity(0.0) : new SmoothFloorDensity(this.limit, this.smoothRange, this.buildFirstInput(argument)));
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
   }
}
