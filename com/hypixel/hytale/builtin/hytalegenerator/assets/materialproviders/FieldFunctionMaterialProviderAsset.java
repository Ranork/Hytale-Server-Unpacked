package com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.ConstantDensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.DensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.FieldFunctionMaterialProvider;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.ArrayList;
import javax.annotation.Nonnull;

public class FieldFunctionMaterialProviderAsset extends MaterialProviderAsset {
   @Nonnull
   public static final BuilderCodec<FieldFunctionMaterialProviderAsset> CODEC = BuilderCodec.builder(
         FieldFunctionMaterialProviderAsset.class, FieldFunctionMaterialProviderAsset::new, MaterialProviderAsset.ABSTRACT_CODEC
      )
      .append(new KeyedCodec<>("FieldFunction", DensityAsset.CODEC, true), (t, k) -> t.densityAsset = k, t -> t.densityAsset)
      .add()
      .append(
         new KeyedCodec<>(
            "Delimiters",
            new ArrayCodec<>(FieldFunctionMaterialProviderAsset.DelimiterAsset.CODEC, FieldFunctionMaterialProviderAsset.DelimiterAsset[]::new),
            true
         ),
         (t, k) -> t.delimiterAssets = k,
         k -> k.delimiterAssets
      )
      .add()
      .build();
   private DensityAsset densityAsset = new ConstantDensityAsset();
   private FieldFunctionMaterialProviderAsset.DelimiterAsset[] delimiterAssets = new FieldFunctionMaterialProviderAsset.DelimiterAsset[0];

   @Nonnull
   @Override
   public MaterialProvider<Material> build(@Nonnull MaterialProviderAsset.Argument argument) {
      if (super.skip()) {
         return MaterialProvider.noMaterialProvider();
      } else {
         Density functionTree = this.densityAsset.build(DensityAsset.from(argument));
         ArrayList<FieldFunctionMaterialProvider.FieldDelimiter<Material>> delimitersList = new ArrayList<>(this.delimiterAssets.length);

         for (FieldFunctionMaterialProviderAsset.DelimiterAsset delimiterAsset : this.delimiterAssets) {
            MaterialProvider<Material> materialProvider = delimiterAsset.materialProviderAsset.build(argument);
            FieldFunctionMaterialProvider.FieldDelimiter<Material> delimiter = new FieldFunctionMaterialProvider.FieldDelimiter<>(
               materialProvider, delimiterAsset.from, delimiterAsset.to
            );
            delimitersList.add(delimiter);
         }

         return new FieldFunctionMaterialProvider<>(functionTree, delimitersList);
      }
   }

   @Override
   public void cleanUp() {
      this.densityAsset.cleanUp();

      for (FieldFunctionMaterialProviderAsset.DelimiterAsset delimiterAsset : this.delimiterAssets) {
         delimiterAsset.cleanUp();
      }
   }

   public static class DelimiterAsset
      implements Cleanable,
      JsonAssetWithMap<String, DefaultAssetMap<String, FieldFunctionMaterialProviderAsset.DelimiterAsset>> {
      @Nonnull
      public static final AssetBuilderCodec<String, FieldFunctionMaterialProviderAsset.DelimiterAsset> CODEC = AssetBuilderCodec.builder(
            FieldFunctionMaterialProviderAsset.DelimiterAsset.class,
            FieldFunctionMaterialProviderAsset.DelimiterAsset::new,
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
