package com.hypixel.hytale.builtin.hytalegenerator.assets.terrains;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import javax.annotation.Nonnull;

public abstract class TerrainAsset implements Cleanable, JsonAssetWithMap<String, DefaultAssetMap<String, TerrainAsset>> {
   @Nonnull
   private static final TerrainAsset[] EMPTY_INPUTS = new TerrainAsset[0];
   @Nonnull
   public static final AssetCodecMapCodec<String, TerrainAsset> CODEC = new AssetCodecMapCodec<>(
      Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data
   );
   @Nonnull
   public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(TerrainAsset.class, CODEC);
   @Nonnull
   public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<>(CHILD_ASSET_CODEC, String[]::new);
   @Nonnull
   public static final BuilderCodec<TerrainAsset> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(TerrainAsset.class).build();
   private String id;
   private AssetExtraInfo.Data data;
   @Nonnull
   private TerrainAsset[] inputs = EMPTY_INPUTS;
   private boolean skip = false;

   protected TerrainAsset() {
   }

   public abstract Density buildDensity(@Nonnull SeedBox var1, @Nonnull ReferenceBundle var2, @Nonnull WorkerIndexer.Id var3);

   public String getId() {
      return this.id;
   }

   @Override
   public void cleanUp() {
      for (TerrainAsset asset : this.inputs) {
         asset.cleanUp();
      }
   }
}
