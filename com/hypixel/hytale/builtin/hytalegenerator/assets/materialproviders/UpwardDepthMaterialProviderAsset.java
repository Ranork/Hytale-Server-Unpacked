package com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders;

import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.UpwardDepthMaterialProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class UpwardDepthMaterialProviderAsset extends MaterialProviderAsset {
   @Nonnull
   public static final BuilderCodec<UpwardDepthMaterialProviderAsset> CODEC = BuilderCodec.builder(
         UpwardDepthMaterialProviderAsset.class, UpwardDepthMaterialProviderAsset::new, MaterialProviderAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Depth", Codec.INTEGER, true), (t, k) -> t.depth = k, k -> k.depth)
      .add()
      .append(new KeyedCodec<>("Material", MaterialProviderAsset.CODEC, true), (t, k) -> t.materialProviderAsset = k, k -> k.materialProviderAsset)
      .add()
      .build();
   private int depth;
   private MaterialProviderAsset materialProviderAsset = new ConstantMaterialProviderAsset();

   @Nonnull
   @Override
   public MaterialProvider<Material> build(@Nonnull MaterialProviderAsset.Argument argument) {
      return (MaterialProvider<Material>)(super.skip()
         ? MaterialProvider.noMaterialProvider()
         : new UpwardDepthMaterialProvider<>(this.materialProviderAsset.build(argument), this.depth));
   }

   @Override
   public void cleanUp() {
      this.materialProviderAsset.cleanUp();
   }
}
