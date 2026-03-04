package com.hypixel.hytale.builtin.hytalegenerator.assets.interpolationasset;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import javax.annotation.Nonnull;

public class BiomeFrontierAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, BiomeFrontierAsset>> {
   @Nonnull
   public static final AssetBuilderCodec<String, BiomeFrontierAsset> CODEC = AssetBuilderCodec.builder(
         BiomeFrontierAsset.class,
         BiomeFrontierAsset::new,
         Codec.STRING,
         (asset, id) -> asset.id = id,
         config -> config.id,
         (config, data) -> config.data = data,
         config -> config.data
      )
      .append(new KeyedCodec<>("InterpolationRadius", Codec.INTEGER, true), (t, k) -> t.interpolationRadius = k, t -> t.interpolationRadius)
      .add()
      .build();
   private String id;
   private AssetExtraInfo.Data data;
   private int interpolationRadius = 1;

   private BiomeFrontierAsset() {
   }

   public int getInterpolationRadius() {
      return this.interpolationRadius;
   }

   public String getId() {
      return this.id;
   }
}
