package com.hypixel.hytale.builtin.hytalegenerator.assets.blockmask;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.MaterialSet;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.assets.blockset.MaterialSetAsset;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import javax.annotation.Nonnull;

public class BlockMaskEntryAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, BlockMaskEntryAsset>>, Cleanable {
   @Nonnull
   public static final AssetBuilderCodec<String, BlockMaskEntryAsset> CODEC = AssetBuilderCodec.builder(
         BlockMaskEntryAsset.class,
         BlockMaskEntryAsset::new,
         Codec.STRING,
         (asset, id) -> asset.id = id,
         config -> config.id,
         (config, data) -> config.data = data,
         config -> config.data
      )
      .append(new KeyedCodec<>("Source", MaterialSetAsset.CODEC, true), (t, k) -> t.propBlockSet = k, t -> t.propBlockSet)
      .add()
      .append(new KeyedCodec<>("CanReplace", MaterialSetAsset.CODEC, true), (t, k) -> t.replacesBlockSet = k, t -> t.replacesBlockSet)
      .add()
      .build();
   private String id;
   private AssetExtraInfo.Data data;
   private MaterialSetAsset propBlockSet = new MaterialSetAsset();
   private MaterialSetAsset replacesBlockSet = new MaterialSetAsset();

   protected BlockMaskEntryAsset() {
   }

   @Nonnull
   public MaterialSet getPropBlockSet(@Nonnull MaterialCache materialCache) {
      return this.propBlockSet.build(materialCache);
   }

   @Nonnull
   public MaterialSet getReplacesBlockSet(@Nonnull MaterialCache materialCache) {
      return this.replacesBlockSet.build(materialCache);
   }

   public String getId() {
      return this.id;
   }

   @Override
   public void cleanUp() {
      this.propBlockSet.cleanUp();
      this.replacesBlockSet.cleanUp();
   }
}
