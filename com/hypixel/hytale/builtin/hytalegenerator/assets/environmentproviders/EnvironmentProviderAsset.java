package com.hypixel.hytale.builtin.hytalegenerator.assets.environmentproviders;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.environmentproviders.EnvironmentProvider;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;

public abstract class EnvironmentProviderAsset implements Cleanable, JsonAssetWithMap<String, DefaultAssetMap<String, EnvironmentProviderAsset>> {
   @Nonnull
   public static final AssetCodecMapCodec<String, EnvironmentProviderAsset> CODEC = new AssetCodecMapCodec<>(
      Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data
   );
   @Nonnull
   private static final Map<String, EnvironmentProviderAsset> exportedNodes = new ConcurrentHashMap<>();
   @Nonnull
   public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(EnvironmentProviderAsset.class, CODEC);
   @Nonnull
   public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<>(CHILD_ASSET_CODEC, String[]::new);
   @Nonnull
   public static final BuilderCodec<EnvironmentProviderAsset> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(EnvironmentProviderAsset.class)
      .append(new KeyedCodec<>("Skip", Codec.BOOLEAN, false), (t, k) -> t.skip = k, t -> t.skip)
      .add()
      .append(new KeyedCodec<>("ExportAs", Codec.STRING, false), (t, k) -> t.exportName = k, t -> t.exportName)
      .add()
      .afterDecode(asset -> {
         if (asset.exportName != null && !asset.exportName.isEmpty()) {
            if (exportedNodes.containsKey(asset.exportName)) {
               LoggerUtil.getLogger().warning("Duplicate export name for asset: " + asset.exportName);
            }

            exportedNodes.put(asset.exportName, asset);
            LoggerUtil.getLogger().fine("Registered imported node asset with name '" + asset.exportName + "' with asset id '" + asset.id);
         }
      })
      .build();
   private String id;
   private AssetExtraInfo.Data data;
   private boolean skip = false;
   private String exportName = "";

   protected EnvironmentProviderAsset() {
   }

   public abstract EnvironmentProvider build(@Nonnull EnvironmentProviderAsset.Argument var1);

   @Nonnull
   public static EnvironmentProviderAsset getFallbackAsset() {
      return new ConstantEnvironmentProviderAsset();
   }

   public boolean isSkipped() {
      return this.skip;
   }

   public static EnvironmentProviderAsset getExportedAsset(@Nonnull String name) {
      return exportedNodes.get(name);
   }

   public String getId() {
      return this.id;
   }

   @Override
   public void cleanUp() {
   }

   public static class Argument {
      public SeedBox parentSeed;
      public MaterialCache materialCache;
      public ReferenceBundle referenceBundle;
      public WorkerIndexer.Id workerId;

      public Argument(
         @Nonnull SeedBox parentSeed, @Nonnull MaterialCache materialCache, @Nonnull ReferenceBundle referenceBundle, @Nonnull WorkerIndexer.Id workerId
      ) {
         this.parentSeed = parentSeed;
         this.materialCache = materialCache;
         this.referenceBundle = referenceBundle;
         this.workerId = workerId;
      }

      public Argument(@Nonnull EnvironmentProviderAsset.Argument argument) {
         this.parentSeed = argument.parentSeed;
         this.materialCache = argument.materialCache;
         this.referenceBundle = argument.referenceBundle;
         this.workerId = argument.workerId;
      }
   }
}
