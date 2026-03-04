package com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth;

import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.MaterialProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.conditionassets.AlwaysTrueConditionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.conditionassets.ConditionAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders.spaceanddepth.layerassets.LayerAsset;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.spaceanddepth.SpaceAndDepthMaterialProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import java.util.ArrayList;
import javax.annotation.Nonnull;

public class SpaceAndDepthMaterialProviderAsset extends MaterialProviderAsset {
   @Nonnull
   public static final BuilderCodec<SpaceAndDepthMaterialProviderAsset> CODEC = BuilderCodec.builder(
         SpaceAndDepthMaterialProviderAsset.class, SpaceAndDepthMaterialProviderAsset::new, MaterialProviderAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("LayerContext", SpaceAndDepthMaterialProvider.LayerContextType.CODEC, true), (t, k) -> t.layerContext = k, k -> k.layerContext)
      .add()
      .<Integer>append(new KeyedCodec<>("MaxExpectedDepth", Codec.INTEGER, true), (t, k) -> t.maxDistance = k, k -> k.maxDistance)
      .addValidator(Validators.greaterThanOrEqual(0))
      .add()
      .append(new KeyedCodec<>("Condition", ConditionAsset.CODEC, false), (t, k) -> t.conditionAsset = k, k -> k.conditionAsset)
      .add()
      .<LayerAsset[]>append(
         new KeyedCodec<>("Layers", new ArrayCodec<>(LayerAsset.CODEC, LayerAsset[]::new), false), (t, k) -> t.layerAssets = k, k -> k.layerAssets
      )
      .addValidator(Validators.nonNullArrayElements())
      .add()
      .build();
   private SpaceAndDepthMaterialProvider.LayerContextType layerContext = SpaceAndDepthMaterialProvider.LayerContextType.DEPTH_INTO_FLOOR;
   private int maxDistance = 16;
   private ConditionAsset conditionAsset = new AlwaysTrueConditionAsset();
   private LayerAsset[] layerAssets = new LayerAsset[0];

   @Nonnull
   @Override
   public MaterialProvider<Material> build(@Nonnull MaterialProviderAsset.Argument argument) {
      if (super.skip()) {
         return MaterialProvider.noMaterialProvider();
      } else {
         SpaceAndDepthMaterialProvider.Condition condition = this.conditionAsset.build();
         ArrayList<SpaceAndDepthMaterialProvider.Layer<Material>> layerList = new ArrayList<>(this.layerAssets.length);

         for (LayerAsset asset : this.layerAssets) {
            layerList.add(asset.build(argument));
         }

         return new SpaceAndDepthMaterialProvider<>(this.layerContext, layerList, condition, this.maxDistance);
      }
   }

   @Override
   public void cleanUp() {
      for (LayerAsset layerAsset : this.layerAssets) {
         layerAsset.cleanUp();
      }
   }
}
