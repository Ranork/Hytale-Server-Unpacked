package com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.datastructures.WeightedMap;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.WeightedMaterialProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.Validators;
import javax.annotation.Nonnull;

public class WeightedMaterialProviderAsset extends MaterialProviderAsset {
   @Nonnull
   public static final BuilderCodec<WeightedMaterialProviderAsset> CODEC = BuilderCodec.builder(
         WeightedMaterialProviderAsset.class, WeightedMaterialProviderAsset::new, MaterialProviderAsset.ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>(
            "WeightedMaterials",
            new ArrayCodec<>(WeightedMaterialProviderAsset.WeightedMaterialAsset.CODEC, WeightedMaterialProviderAsset.WeightedMaterialAsset[]::new),
            true
         ),
         (t, k) -> t.weighedMapEntries = k,
         k -> k.weighedMapEntries
      )
      .add()
      .append(new KeyedCodec<>("SkipChance", Codec.DOUBLE, true), (t, k) -> t.skipChance = k, k -> k.skipChance)
      .add()
      .append(new KeyedCodec<>("Seed", Codec.STRING, true), (t, k) -> t.seed = k, k -> k.seed)
      .add()
      .build();
   private WeightedMaterialProviderAsset.WeightedMaterialAsset[] weighedMapEntries = new WeightedMaterialProviderAsset.WeightedMaterialAsset[0];
   private double skipChance;
   private String seed = "";

   @Nonnull
   @Override
   public MaterialProvider<Material> build(@Nonnull MaterialProviderAsset.Argument argument) {
      if (super.skip()) {
         return MaterialProvider.noMaterialProvider();
      } else {
         WeightedMap<MaterialProvider<Material>> weightMap = new WeightedMap<>();

         for (WeightedMaterialProviderAsset.WeightedMaterialAsset entry : this.weighedMapEntries) {
            weightMap.add(entry.materialProviderAsset.build(argument), entry.weight);
         }

         return new WeightedMaterialProvider<>(weightMap, argument.parentSeed.child(this.seed), this.skipChance);
      }
   }

   @Override
   public void cleanUp() {
      for (WeightedMaterialProviderAsset.WeightedMaterialAsset weightedMaterialAsset : this.weighedMapEntries) {
         weightedMaterialAsset.cleanUp();
      }
   }

   public static class WeightedMaterialAsset
      implements Cleanable,
      JsonAssetWithMap<String, DefaultAssetMap<String, WeightedMaterialProviderAsset.WeightedMaterialAsset>> {
      @Nonnull
      public static final AssetBuilderCodec<String, WeightedMaterialProviderAsset.WeightedMaterialAsset> CODEC = AssetBuilderCodec.builder(
            WeightedMaterialProviderAsset.WeightedMaterialAsset.class,
            WeightedMaterialProviderAsset.WeightedMaterialAsset::new,
            Codec.STRING,
            (asset, id) -> asset.id = id,
            config -> config.id,
            (config, data) -> config.data = data,
            config -> config.data
         )
         .append(new KeyedCodec<>("Weight", Codec.DOUBLE, true), (t, y) -> t.weight = y, t -> t.weight)
         .addValidator(Validators.greaterThanOrEqual(0.0))
         .add()
         .append(new KeyedCodec<>("Material", MaterialProviderAsset.CODEC, true), (t, out) -> t.materialProviderAsset = out, t -> t.materialProviderAsset)
         .add()
         .build();
      private String id;
      private AssetExtraInfo.Data data;
      private double weight = 1.0;
      private MaterialProviderAsset materialProviderAsset;

      public String getId() {
         return this.id;
      }

      @Override
      public void cleanUp() {
         this.materialProviderAsset.cleanUp();
      }
   }
}
