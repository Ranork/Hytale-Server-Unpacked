package com.hypixel.hytale.builtin.hytalegenerator.assets.density;

import com.hypixel.hytale.builtin.hytalegenerator.assets.vectorproviders.ConstantVectorProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.vectorproviders.VectorProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.AngleDensity;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.ConstantValueDensity;
import com.hypixel.hytale.builtin.hytalegenerator.vectorproviders.VectorProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class AngleDensityAsset extends DensityAsset {
   @Nonnull
   public static final BuilderCodec<AngleDensityAsset> CODEC = BuilderCodec.builder(
         AngleDensityAsset.class, AngleDensityAsset::new, DensityAsset.ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>("VectorProvider", VectorProviderAsset.CODEC, true),
         (asset, value) -> asset.vectorProviderAsset = value,
         value -> value.vectorProviderAsset
      )
      .add()
      .append(new KeyedCodec<>("Vector", Vector3d.CODEC, true), (asset, value) -> asset.vector = value, asset -> asset.vector)
      .add()
      .append(new KeyedCodec<>("IsAxis", Codec.BOOLEAN, true), (asset, value) -> asset.isAxis = value, asset -> asset.isAxis)
      .add()
      .build();
   private VectorProviderAsset vectorProviderAsset = new ConstantVectorProviderAsset();
   private Vector3d vector = new Vector3d();
   private boolean isAxis;

   @Nonnull
   @Override
   public Density build(@Nonnull DensityAsset.Argument argument) {
      if (this.isSkipped()) {
         return new ConstantValueDensity(0.0);
      } else {
         VectorProvider vectorProvider = this.vectorProviderAsset.build(new VectorProviderAsset.Argument(argument));
         return new AngleDensity(vectorProvider, this.vector, this.isAxis);
      }
   }

   @Override
   public void cleanUp() {
      this.cleanUpInputs();
      this.vectorProviderAsset.cleanUp();
   }
}
