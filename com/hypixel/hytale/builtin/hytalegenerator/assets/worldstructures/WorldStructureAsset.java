package com.hypixel.hytale.builtin.hytalegenerator.assets.worldstructures;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.assets.positionproviders.PositionProviderAsset;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.builtin.hytalegenerator.worldstructure.WorldStructure;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class WorldStructureAsset implements Cleanable, JsonAssetWithMap<String, DefaultAssetMap<String, WorldStructureAsset>> {
   @Nonnull
   public static final AssetCodecMapCodec<String, WorldStructureAsset> CODEC = new AssetCodecMapCodec<>(
      Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data
   );
   @Nonnull
   public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(WorldStructureAsset.class, CODEC);
   @Nonnull
   public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<>(CHILD_ASSET_CODEC, String[]::new);
   @Nonnull
   public static final BuilderCodec<WorldStructureAsset> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(WorldStructureAsset.class).build();
   private String id;
   private AssetExtraInfo.Data data;

   protected WorldStructureAsset() {
   }

   @Nullable
   public abstract WorldStructure build(@Nonnull WorldStructureAsset.Argument var1);

   @Nonnull
   public abstract PositionProviderAsset getSpawnPositionsAsset();

   public String getId() {
      return this.id;
   }

   @Override
   public void cleanUp() {
   }

   public static class Argument {
      public MaterialCache materialCache;
      public SeedBox parentSeed;
      public WorkerIndexer.Id workerId;

      public Argument(@Nonnull MaterialCache materialCache, @Nonnull SeedBox parentSeed, @Nonnull WorkerIndexer.Id workerId) {
         this.materialCache = materialCache;
         this.parentSeed = parentSeed;
         this.workerId = workerId;
      }

      public Argument(@Nonnull WorldStructureAsset.Argument argument) {
         this.materialCache = argument.materialCache;
         this.parentSeed = argument.parentSeed;
         this.workerId = argument.workerId;
      }
   }
}
