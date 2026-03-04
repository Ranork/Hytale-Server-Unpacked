package com.hypixel.hytale.builtin.hytalegenerator.assets.scanners;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetCodecMapCodec;
import com.hypixel.hytale.assetstore.codec.ContainedAssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.assets.props.PropAsset;
import com.hypixel.hytale.builtin.hytalegenerator.referencebundle.ReferenceBundle;
import com.hypixel.hytale.builtin.hytalegenerator.scanners.Scanner;
import com.hypixel.hytale.builtin.hytalegenerator.seed.SeedBox;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;

public abstract class ScannerAsset implements Cleanable, JsonAssetWithMap<String, DefaultAssetMap<String, ScannerAsset>> {
   @Nonnull
   public static final AssetCodecMapCodec<String, ScannerAsset> CODEC = new AssetCodecMapCodec<>(
      Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data
   );
   @Nonnull
   private static final Map<String, ScannerAsset> exportedNodes = new ConcurrentHashMap<>();
   @Nonnull
   public static final Codec<String> CHILD_ASSET_CODEC = new ContainedAssetCodec<>(ScannerAsset.class, CODEC);
   @Nonnull
   public static final Codec<String[]> CHILD_ASSET_CODEC_ARRAY = new ArrayCodec<>(CHILD_ASSET_CODEC, String[]::new);
   @Nonnull
   public static final BuilderCodec<ScannerAsset> ABSTRACT_CODEC = BuilderCodec.abstractBuilder(ScannerAsset.class)
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
            LoggerUtil.getLogger().info("Exported Scanner asset with name '" + asset.exportName + "' with asset id '" + asset.id);
         }
      })
      .build();
   private String id;
   private AssetExtraInfo.Data data;
   private boolean skip = false;
   private String exportName = "";

   protected ScannerAsset() {
   }

   public abstract Scanner build(@Nonnull ScannerAsset.Argument var1);

   public boolean skip() {
      return this.skip;
   }

   public static ScannerAsset getExportedAsset(@Nonnull String name) {
      return exportedNodes.get(name);
   }

   public String getId() {
      return this.id;
   }

   @Nonnull
   public static ScannerAsset.Argument argumentFrom(@Nonnull PropAsset.Argument argument) {
      return new ScannerAsset.Argument(argument.parentSeed, argument.referenceBundle);
   }

   @Override
   public void cleanUp() {
   }

   public static class Argument {
      public SeedBox parentSeed;
      public ReferenceBundle referenceBundle;

      public Argument(@Nonnull SeedBox parentSeed, @Nonnull ReferenceBundle referenceBundle) {
         this.parentSeed = parentSeed;
         this.referenceBundle = referenceBundle;
      }

      public Argument(@Nonnull ScannerAsset.Argument argument) {
         this.parentSeed = argument.parentSeed;
         this.referenceBundle = argument.referenceBundle;
      }
   }
}
