package com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders;

import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.UpwardSpaceMaterialProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class UpwardSpaceMaterialProviderAsset extends MaterialProviderAsset {
   @Nonnull
   public static final BuilderCodec<UpwardSpaceMaterialProviderAsset> CODEC = BuilderCodec.builder(
         UpwardSpaceMaterialProviderAsset.class, UpwardSpaceMaterialProviderAsset::new, MaterialProviderAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Space", Codec.INTEGER, true), (t, k) -> t.space = k, k -> k.space)
      .add()
      .append(new KeyedCodec<>("Material", MaterialProviderAsset.CODEC, true), (t, k) -> t.materialProviderAsset = k, k -> k.materialProviderAsset)
      .add()
      .build();
   private int space;
   private MaterialProviderAsset materialProviderAsset = new ConstantMaterialProviderAsset();

   @Nonnull
   @Override
   public MaterialProvider<Material> build(@Nonnull MaterialProviderAsset.Argument argument) {
      return new UpwardSpaceMaterialProvider<>(this.materialProviderAsset.build(argument), this.space);
   }

   @Override
   public void cleanUp() {
      this.materialProviderAsset.cleanUp();
   }
}
