package com.hypixel.hytale.builtin.hytalegenerator.assets.materialproviders;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.assets.density.DensityAsset;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.material.Material;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.materialproviders.MaterialProvider;
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

public abstract class MaterialProviderAsset implements Cleanable, JsonAssetWithMap<String, DefaultAssetMap<String, MaterialProviderAsset>> {
   @Nonnull
   private static final MaterialProviderAsset[] EMPTY_INPUTS = new MaterialProviderAsset[0];
   @Nonnull
   public static final AssetCodecMapCodec<String, MaterialProviderAsset> CODEC = new AssetCodecMapCodec<>(
      Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data
   );
   @Nonnull
   private static final Map<String, MaterialProviderAsset> exportedNodes = new ConcurrentHashMap<>();
   @Nonnull
   public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(MaterialProviderAsset.class, CODEC);
   @Nonnull
   public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<>(CHILD_ASSET_CODEC, String[]::new);
   @Nonnull
   public static final BuilderCodec<MaterialProviderAsset> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(MaterialProviderAsset.class)
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
   private boolean skip;
   private String exportName = "";

   protected MaterialProviderAsset() {
   }

   public abstract MaterialProvider<Material> build(@Nonnull MaterialProviderAsset.Argument var1);

   public boolean skip() {
      return this.skip;
   }

   public static MaterialProviderAsset getExportedAsset(@Nonnull String name) {
      return exportedNodes.get(name);
   }

   public String getId() {
      return this.id;
   }

   @Nonnull
   public static MaterialProviderAsset.Argument argumentFrom(@Nonnull DensityAsset.Argument argument, @Nonnull MaterialCache materialCache) {
      return new MaterialProviderAsset.Argument(argument.parentSeed, materialCache, argument.referenceBundle, argument.workerId);
   }

   @Nonnull
   public static MaterialProviderAsset.Argument argumentFrom(@Nonnull PropAsset.Argument argument) {
      return new MaterialProviderAsset.Argument(argument.parentSeed, argument.materialCache, argument.referenceBundle, argument.workerId);
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

      public Argument(@Nonnull MaterialProviderAsset.Argument argument) {
         this.parentSeed = argument.parentSeed;
         this.materialCache = argument.materialCache;
         this.workerId = argument.workerId;
      }
   }
}
