package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ClampDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class ClampDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<ClampDensityAsset> CODEC = BuilderCodec.builder(
         ClampDensityAsset.class, ClampDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("WallA", Codec.DOUBLE, true), (t, k) -> t.wallA = k, k -> k.wallA)
      .add()
      .append(new KeyedCodec<>("WallB", Codec.DOUBLE, true), (t, k) -> t.wallB = k, k -> k.wallB)
      .add()
      .build();
   private double wallA;
   private double wallB;

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      return (Density)(this.isSkipped() ? new ConstantValueDensity(0.0) : new ClampDensity(this.wallA, this.wallB, this.buildFirstInput(argument)));
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
   }
}
