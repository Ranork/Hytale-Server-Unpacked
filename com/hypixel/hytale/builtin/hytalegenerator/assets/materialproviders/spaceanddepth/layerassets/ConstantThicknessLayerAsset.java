package com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.layerassets;

import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.ConstantMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.MaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.SpaceAndDepthMaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.layers.ConstantThicknessLayer;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class ConstantThicknessLayerAsset extends LayerAsset {
   @Nonnull
   public static final BuilderCodec<ConstantThicknessLayerAsset> CODEC = BuilderCodec.builder(
         ConstantThicknessLayerAsset.class, ConstantThicknessLayerAsset::new, LayerAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("Thickness", Codec.INTEGER, true), (t, k) -> t.thickness = k, k -> k.thickness)
      .addValidator(Validators.greaterThanOrEqual(0))
      .add()
      .append(new KeyedCodec<>("Material", MaterialProviderAsset.CODEC, true), (t, k) -> t.materialProviderAsset = k, k -> k.materialProviderAsset)
      .add()
      .build();
   private int thickness;
   private MaterialProviderAsset materialProviderAsset = new ConstantMaterialProviderAsset();

   @Nonnull
   @Override
   public SpaceAndDepthMaterialProvider.Layer<Material> build(@Nonnull MaterialProviderAsset.Argument argument) {
      return new ConstantThicknessLayer<>(this.thickness, this.materialProviderAsset.build(argument));
   }

   @Override
   public void cleanUp() {
      this.materialProviderAsset.cleanUp();
   }
}
