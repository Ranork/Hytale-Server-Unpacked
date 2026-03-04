package com.hypixel.hytale.builtin.hytalegenerator.assets.delimiters;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.delimiters.RangeInt;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import javax.annotation.Nonnull;

public class RangeIntAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, RangeIntAsset>> {
   @Nonnull
   public static final AssetBuilderCodec<String, RangeIntAsset> CODEC = AssetBuilderCodec.builder(
         RangeIntAsset.class,
         RangeIntAsset::new,
         Codec.STRING,
         (asset, id) -> asset.id = id,
         config -> config.id,
         (config, data) -> config.data = data,
         config -> config.data
      )
      .append(new KeyedCodec<>("MinInclusive", Codec.INTEGER, true), (t, value) -> t.minInclusive = value, t -> t.minInclusive)
      .add()
      .append(new KeyedCodec<>("MaxExclusive", Codec.INTEGER, true), (t, value) -> t.maxExclusive = value, t -> t.maxExclusive)
      .add()
      .build();
   private String id;
   private AssetExtraInfo.Data data;
   private int minInclusive = 0;
   private int maxExclusive = 0;

   @Nonnull
   public RangeInt build() {
      return new RangeInt(this.minInclusive, this.maxExclusive);
   }

   @Nonnull
   public String getId() {
      return "";
   }
}
