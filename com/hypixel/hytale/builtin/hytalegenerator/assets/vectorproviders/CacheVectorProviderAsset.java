package com.hypixel.hytale.builtin.hytalegenerator.assets.vectorproviders;

import com.hypixel.hytale.builtin.hytalegenerator.vectorproviders.CacheVectorProvider;
import com.hypixel.hytale.builtin.hytalegenerator.vectorproviders.ConstantVectorProvider;
import com.hypixel.hytale.builtin.hytalegenerator.vectorproviders.VectorProvider;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class CacheVectorProviderAsset extends VectorProviderAsset {
   @Nonnull
   public static final BuilderCodec<CacheVectorProviderAsset> CODEC = BuilderCodec.builder(
         CacheVectorProviderAsset.class, CacheVectorProviderAsset::new, ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>("VectorProvider", VectorProviderAsset.CODEC, true),
         (asset, value) -> asset.vectorProviderAsset = value,
         value -> value.vectorProviderAsset
      )
      .add()
      .build();
   private VectorProviderAsset vectorProviderAsset = new ConstantVectorProviderAsset();

   private CacheVectorProviderAsset() {
   }

   public CacheVectorProviderAsset(@Nonnull VectorProviderAsset vectorProviderAsset) {
      this.vectorProviderAsset = vectorProviderAsset;
   }

   @Nonnull
   @Override
   public VectorProvider build(@Nonnull VectorProviderAsset.Argument argument) {
      if (this.isSkipped()) {
         return new ConstantVectorProvider(new Vector3d());
      } else {
         VectorProvider vectorProvider = this.vectorProviderAsset.build(argument);
         return new CacheVectorProvider(vectorProvider);
      }
   }
}
