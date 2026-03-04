package com.hypixel.hytale.builtin.hytalegenerator.assets.vectorproviders;

import com.hypixel.hytale.builtin.hytalegenerator.vectorproviders.ConstantVectorProvider;
import com.hypixel.hytale.builtin.hytalegenerator.vectorproviders.VectorProvider;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class ConstantVectorProviderAsset extends VectorProviderAsset {
   @Nonnull
   public static final BuilderCodec<ConstantVectorProviderAsset> CODEC = BuilderCodec.builder(
         ConstantVectorProviderAsset.class, ConstantVectorProviderAsset::new, ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Value", Vector3d.CODEC, true), (asset, value) -> asset.value = value, asset -> asset.value)
      .add()
      .build();
   private Vector3d value = new Vector3d();

   public ConstantVectorProviderAsset() {
   }

   public ConstantVectorProviderAsset(@Nonnull Vector3d vector) {
      this.value.assign(vector);
   }

   @Nonnull
   @Override
   public VectorProvider build(@Nonnull VectorProviderAsset.Argument argument) {
      return this.isSkipped() ? new ConstantVectorProvider(new Vector3d()) : new ConstantVectorProvider(this.value);
   }
}
