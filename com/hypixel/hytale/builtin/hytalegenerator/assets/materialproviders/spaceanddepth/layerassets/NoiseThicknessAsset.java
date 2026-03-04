package com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.layerassets;

import com.hypixel.hytale.builtin.hytalegenerator.assets.density.ConstantDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.DensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.ConstantMaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.MaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.SpaceAndDepthMaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.layers.NoiseThickness;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import javax.annotation.Nonnull;

public class NoiseThicknessAsset extends LayerAsset {
   @Nonnull
   public static final BuilderCodec<NoiseThicknessAsset> CODEC = BuilderCodec.builder(
         NoiseThicknessAsset.class, NoiseThicknessAsset::new, LayerAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("ThicknessFunctionXZ", DensityAsset.CODEC, true), (asset, k) -> asset.densityAsset = k, asset -> asset.densityAsset)
      .add()
      .append(new KeyedCodec<>("Material", MaterialProviderAsset.CODEC, true), (t, k) -> t.materialProviderAsset = k, k -> k.materialProviderAsset)
      .add()
      .build();
   private DensityAsset densityAsset = new ConstantDensityAsset();
   private MaterialProviderAsset materialProviderAsset = new ConstantMaterialProviderAsset();

   @Nonnull
   @Override
   public SpaceAndDepthMaterialProvider.Layer<Material> build(@Nonnull MaterialProviderAsset.Argument argument) {
      MaterialProvider<Material> materialProvider = this.materialProviderAsset.build(argument);
      Density functionTree = this.densityAsset.build(DensityAsset.from(argument));
      return new NoiseThickness<>(functionTree, materialProvider);
   }

   @Override
   public void cleanUp() {
      this.densityAsset.cleanUp();
      this.materialProviderAsset.cleanUp();
   }
}
