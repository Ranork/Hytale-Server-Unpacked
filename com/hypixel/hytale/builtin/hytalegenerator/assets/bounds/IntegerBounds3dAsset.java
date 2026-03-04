package com.hypixel.hytale.builtin.hytalegenerator.assets.bounds;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public class IntegerBounds3dAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, IntegerBounds3dAsset>> {
   @Nonnull
   public static final AssetBuilderCodec<String, IntegerBounds3dAsset> CODEC = AssetBuilderCodec.builder(
         IntegerBounds3dAsset.class,
         IntegerBounds3dAsset::new,
         Codec.STRING,
         (asset, id) -> asset.id = id,
         config -> config.id,
         (config, data) -> config.data = data,
         config -> config.data
      )
      .append(new KeyedCodec<>("PointA", Vector3i.CODEC, true), (t, value) -> t.pointA = value, t -> t.pointA)
      .add()
      .append(new KeyedCodec<>("PointB", Vector3i.CODEC, true), (t, value) -> t.pointB = value, t -> t.pointB)
      .add()
      .build();
   private String id;
   private AssetExtraInfo.Data data;
   private Vector3i pointA = new Vector3i();
   private Vector3i pointB = new Vector3i();

   @Nonnull
   public Bounds3i build() {
      Bounds3i bounds = new Bounds3i(this.pointA, this.pointB);
      bounds.correct();
      return bounds;
   }

   public String getId() {
      return this.id;
   }
}
