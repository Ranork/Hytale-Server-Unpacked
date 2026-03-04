package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.RotatorDensity;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class RotatorDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<RotatorDensityAsset> CODEC = BuilderCodec.builder(
         RotatorDensityAsset.class, RotatorDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("NewYAxis", Vector3d.CODEC, true), (t, k) -> t.newYAxis = k, t -> t.newYAxis)
      .add()
      .append(new KeyedCodec<>("SpinAngle", Codec.DOUBLE, true), (t, k) -> t.spinAngle = k, t -> t.spinAngle)
      .add()
      .build();
   private Vector3d newYAxis = new Vector3d();
   private double spinAngle;

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      return (Density)(this.isSkipped() ? new ConstantValueDensity(0.0) : new RotatorDensity(this.buildFirstInput(argument), this.newYAxis, this.spinAngle));
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
   }
}
