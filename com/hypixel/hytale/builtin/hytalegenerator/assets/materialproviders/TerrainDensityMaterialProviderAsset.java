package com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.TerrainDensityMaterialProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.ArrayList;
import javax.annotation.Nonnull;

public class TerrainDensityMaterialProviderAsset extends MaterialProviderAsset {
   @Nonnull
   public static final BuilderCodec<TerrainDensityMaterialProviderAsset> CODEC = BuilderCodec.builder(
         TerrainDensityMaterialProviderAsset.class, TerrainDensityMaterialProviderAsset::new, MaterialProviderAsset.ABSTRACT_CODEC
      )
      .append(
         new KeyedCodec<>(
            "Delimiters",
            new ArrayCodec<>(TerrainDensityMaterialProviderAsset.DelimiterAsset.CODEC, TerrainDensityMaterialProviderAsset.DelimiterAsset[]::new),
            true
         ),
         (t, k) -> t.delimiterAssets = k,
         k -> k.delimiterAssets
      )
      .add()
      .build();
   private TerrainDensityMaterialProviderAsset.DelimiterAsset[] delimiterAssets = new TerrainDensityMaterialProviderAsset.DelimiterAsset[0];

   @Nonnull
   @Override
   public MaterialProvider<Material> build(@Nonnull MaterialProviderAsset.Argument argument) {
      if (super.skip()) {
         return MaterialProvider.noMaterialProvider();
      } else {
         ArrayList<TerrainDensityMaterialProvider.FieldDelimiter<Material>> delimitersList = new ArrayList<>(this.delimiterAssets.length);

         for (TerrainDensityMaterialProviderAsset.DelimiterAsset delimiterAsset : this.delimiterAssets) {
            MaterialProvider<Material> materialProvider = delimiterAsset.materialProviderAsset.build(argument);
            TerrainDensityMaterialProvider.FieldDelimiter<Material> delimiter = new TerrainDensityMaterialProvider.FieldDelimiter<>(
               materialProvider, delimiterAsset.from, delimiterAsset.to
            );
            delimitersList.add(delimiter);
         }

         return new TerrainDensityMaterialProvider<>(delimitersList);
      }
   }

   @Override
   public void cleanUp() {
      for (TerrainDensityMaterialProviderAsset.DelimiterAsset delimiterAsset : this.delimiterAssets) {
         delimiterAsset.cleanUp();
      }
   }

   public static class DelimiterAsset
      implements Cleanable,
      JsonAssetWithMap<String, DefaultAssetMap<String, TerrainDensityMaterialProviderAsset.DelimiterAsset>> {
      @Nonnull
      public static final AssetBuilderCodec<String, TerrainDensityMaterialProviderAsset.DelimiterAsset> CODEC = AssetBuilderCodec.builder(
            TerrainDensityMaterialProviderAsset.DelimiterAsset.class,
            TerrainDensityMaterialProviderAsset.DelimiterAsset::new,
            Codec.STRING,
            (asset, id) -> asset.id = id,
            config -> config.id,
            (config, data) -> config.data = data,
            config -> config.data
         )
         .append(new KeyedCodec<>("From", Codec.DOUBLE, true), (t, y) -> t.from = y, t -> t.from)
         .add()
         .append(new KeyedCodec<>("To", Codec.DOUBLE, true), (t, out) -> t.to = out, t -> t.to)
         .add()
         .append(new KeyedCodec<>("Material", MaterialProviderAsset.CODEC, true), (t, out) -> t.materialProviderAsset = out, t -> t.materialProviderAsset)
         .add()
         .build();
      private String id;
      private AssetExtraInfo.Data data;
      private double from;
      private double to;
      private MaterialProviderAsset materialProviderAsset = new ConstantMaterialProviderAsset();

      public String getId() {
         return this.id;
      }

      @Override
      public void cleanUp() {
         this.materialProviderAsset.cleanUp();
      }
   }
}
