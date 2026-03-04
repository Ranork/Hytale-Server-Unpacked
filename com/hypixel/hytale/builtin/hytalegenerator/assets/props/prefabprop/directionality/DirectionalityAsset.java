package com.hypixel.hytale.builtin.hytalegenerator.assets.props.prefabprop.directionality;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.builtin.hytalegenerator.props.directionality.Directionality;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.builtin.hytalegenerator.threadindexer.WorkerIndexer;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public abstract class DirectionalityAsset implements Cleanable, JsonAssetWithMap<String, DefaultAssetMap<String, DirectionalityAsset>> {
   @Nonnull
   public static final AssetCodecMapCodec<String, DirectionalityAsset> CODEC = new AssetCodecMapCodec<>(
      Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data
   );
   @Nonnull
   private static final Map<String, DirectionalityAsset> exportedNodes = new HashMap<>();
   @Nonnull
   public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(DirectionalityAsset.class, CODEC);
   @Nonnull
   public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<>(CHILD_ASSET_CODEC, String[]::new);
   @Nonnull
   public static final BuilderCodec<DirectionalityAsset> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(DirectionalityAsset.class)
      .append(new KeyedCodec<>("ExportAs", Codec.STRING, false), (t, k) -> t.exportName = k, t -> t.exportName)
      .add()
      .afterDecode(asset -> {
         if (asset.exportName != null && !asset.exportName.isEmpty()) {
            exportedNodes.put(asset.exportName, asset);
            LoggerUtil.getLogger().fine("Registered imported position provider asset with name '" + asset.exportName + "' with asset id '" + asset.id);
         }
      })
      .build();
   private String id;
   private AssetExtraInfo.Data data;
   private String exportName = "";

   protected DirectionalityAsset() {
   }

   public abstract Directionality build(@Nonnull DirectionalityAsset.Argument var1);

   @Override
   public void cleanUp() {
   }

   public static DirectionalityAsset getExportedAsset(@Nonnull String name) {
      return exportedNodes.get(name);
   }

   public String getId() {
      return this.id;
   }

   @Nonnull
   public static DirectionalityAsset.Argument argumentFrom(@Nonnull PropAsset.Argument argument) {
      return new DirectionalityAsset.Argument(argument.parentSeed, argument.materialCache, argument.referenceBundle, argument.workerId);
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

      public Argument(@Nonnull DirectionalityAsset.Argument argument) {
         this.parentSeed = argument.parentSeed;
         this.materialCache = argument.materialCache;
         this.referenceBundle = argument.referenceBundle;
         this.workerId = argument.workerId;
      }
   }
}
