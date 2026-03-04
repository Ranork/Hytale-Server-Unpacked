package com.hypixel.hytale.builtin.hytalegenerator.assets.delimiters;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.delimiters.RangeDouble;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import javax.annotation.Nonnull;

public class RangeDoubleAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, RangeDoubleAsset>> {
   @Nonnull
   public static final AssetBuilderCodec<String, RangeDoubleAsset> CODEC = AssetBuilderCodec.builder(
         RangeDoubleAsset.class,
         RangeDoubleAsset::new,
         Codec.STRING,
         (asset, id) -> asset.id = id,
         config -> config.id,
         (config, data) -> config.data = data,
         config -> config.data
      )
      .append(new KeyedCodec<>("MinInclusive", Codec.DOUBLE, true), (t, value) -> t.minInclusive = value, t -> t.minInclusive)
      .add()
      .append(new KeyedCodec<>("MaxExclusive", Codec.DOUBLE, true), (t, value) -> t.maxExclusive = value, t -> t.maxExclusive)
      .add()
      .build();
   private String id;
   private AssetExtraInfo.Data data;
   private double minInclusive = 0.0;
   private double maxExclusive = 0.0;

   @Nonnull
   public RangeDouble build() {
      return new RangeDouble(this.minInclusive, this.maxExclusive);
   }

   @Nonnull
   public String getId() {
      return "";
   }
}
