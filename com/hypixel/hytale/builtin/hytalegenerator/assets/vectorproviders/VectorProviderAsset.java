package com.hypixel.hytale.builtin.hytalegenerator.assets.vectorproviders;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.DensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.builtin.hytalegenerator.vectorproviders.VectorProvider;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;

public abstract class VectorProviderAsset implements Cleanable, JsonAssetWithMap<String, DefaultAssetMap<String, VectorProviderAsset>> {
   @Nonnull
   public static final AssetCodecMapCodec<String, VectorProviderAsset> CODEC = new AssetCodecMapCodec<>(
      Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data
   );
   @Nonnull
   private static final Map<String, VectorProviderAsset.Exported> exportedNodes = new ConcurrentHashMap<>();
   @Nonnull
   public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(VectorProviderAsset.class, CODEC);
   @Nonnull
   public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<>(CHILD_ASSET_CODEC, String[]::new);
   @Nonnull
   public static final BuilderCodec<VectorProviderAsset> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(VectorProviderAsset.class)
      .append(new KeyedCodec<>("Skip", Codec.BOOLEAN, false), (t, k) -> t.skip = k, t -> t.skip)
      .add()
      .append(new KeyedCodec<>("ExportAs", Codec.STRING, false), (t, k) -> t.exportName = k, t -> t.exportName)
      .add()
      .afterDecode(asset -> {
         if (asset.exportName != null && !asset.exportName.isEmpty()) {
            if (exportedNodes.containsKey(asset.exportName)) {
               LoggerUtil.getLogger().warning("Duplicate export name for asset: " + asset.exportName);
            }

            boolean isSingleInstance;
            if (asset instanceof ExportedVectorProviderAsset exportedAsset) {
               isSingleInstance = exportedAsset.isSingleInstance();
            } else {
               isSingleInstance = false;
            }

            VectorProviderAsset.Exported exported = new VectorProviderAsset.Exported(isSingleInstance, asset);
            exportedNodes.put(asset.exportName, exported);
            LoggerUtil.getLogger().fine("Registered imported node asset with name '" + asset.exportName + "' with asset id '" + asset.id);
         }
      })
      .build();
   private String id;
   private AssetExtraInfo.Data data;
   protected boolean skip = false;
   protected String exportName = "";

   protected VectorProviderAsset() {
   }

   public abstract VectorProvider build(@Nonnull VectorProviderAsset.Argument var1);

   public boolean isSkipped() {
      return this.skip;
   }

   public static VectorProviderAsset.Exported getExportedAsset(@Nonnull String name) {
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
      public ReferenceBundle referenceBundle;
      public WorkerIndexer.Id workerId;

      public Argument(@Nonnull SeedBox parentSeed, @Nonnull ReferenceBundle referenceBundle, @Nonnull WorkerIndexer.Id workerId) {
         this.parentSeed = parentSeed;
         this.referenceBundle = referenceBundle;
         this.workerId = workerId;
      }

      public Argument(@Nonnull VectorProviderAsset.Argument argument) {
         this.parentSeed = argument.parentSeed;
         this.referenceBundle = argument.referenceBundle;
         this.workerId = argument.workerId;
      }

      public Argument(@Nonnull DensityAsset.Argument argument) {
         this.parentSeed = argument.parentSeed;
         this.referenceBundle = argument.referenceBundle;
         this.workerId = argument.workerId;
      }
   }

   public static class Exported {
      public boolean isSingleInstance;
      @Nonnull
      public VectorProviderAsset asset;
      @Nonnull
      public Map<WorkerIndexer.Id, VectorProvider> threadInstances;

      public Exported(boolean isSingleInstance, @Nonnull VectorProviderAsset asset) {
         this.isSingleInstance = isSingleInstance;
         this.asset = asset;
         this.threadInstances = new ConcurrentHashMap<>();
      }
   }
}
