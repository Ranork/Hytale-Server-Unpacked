package com.hypixel.hytale.builtin.hytalegenerator.assets.density.positions.distancefunctions;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.density.nodes.positions.distancefunctions.DistanceFunction;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import javax.annotation.Nonnull;

public abstract class DistanceFunctionAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, DistanceFunctionAsset>> {
   @Nonnull
   public static final AssetCodecMapCodec<String, DistanceFunctionAsset> CODEC = new AssetCodecMapCodec<>(
      Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data
   );
   @Nonnull
   public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(DistanceFunctionAsset.class, CODEC);
   @Nonnull
   public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<>(CHILD_ASSET_CODEC, String[]::new);
   @Nonnull
   public static final BuilderCodec<DistanceFunctionAsset> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(DistanceFunctionAsset.class).build();
   private String id;
   private AssetExtraInfo.Data data;

   protected DistanceFunctionAsset() {
   }

   public abstract DistanceFunction build(@Nonnull SeedBox var1, double var2);

   public String getId() {
      return this.id;
   }
}
