package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.assets.curves.legacy.NodeFunctionYOutAsset;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.OffsetDensity;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class OffsetDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<OffsetDensityAsset> CODEC = BuilderCodec.builder(
         OffsetDensityAsset.class, OffsetDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("FunctionForY", NodeFunctionYOutAsset.CODEC, true), (t, k) -> t.nodeFunctionYOutAsset = k, k -> k.nodeFunctionYOutAsset)
      .add()
      .build();
   @Nonnull
   private NodeFunctionYOutAsset nodeFunctionYOutAsset = new NodeFunctionYOutAsset();

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      return (Density)(this.isSkipped() ? new ConstantValueDensity(0.0) : new OffsetDensity(this.nodeFunctionYOutAsset.build(), this.buildFirstInput(argument)));
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
      this.nodeFunctionYOutAsset.cleanUp();
   }
}
