package com.hypixel.hytale.builtin.hytalegenerator.assets.framework;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.assets.worldstructures.WorldStructureAsset;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import javax.annotation.Nonnull;

public abstract class FrameworkAsset implements Cleanable, JsonAssetWithMap<String, DefaultAssetMap<String, FrameworkAsset>> {
   @Nonnull
   public static final AssetCodecMapCodec<String, FrameworkAsset> CODEC = new AssetCodecMapCodec<>(
      Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data
   );
   @Nonnull
   public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(FrameworkAsset.class, CODEC);
   @Nonnull
   public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<>(CHILD_ASSET_CODEC, String[]::new);
   @Nonnull
   public static final BuilderCodec<FrameworkAsset> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(FrameworkAsset.class).build();
   private String id;
   private AssetExtraInfo.Data data;

   protected FrameworkAsset() {
   }

   public String getId() {
      return this.id;
   }

   @Override
   public void cleanUp() {
   }

   public abstract void build(@Nonnull WorldStructureAsset.Argument var1, @Nonnull ReferenceBundle var2);
}
