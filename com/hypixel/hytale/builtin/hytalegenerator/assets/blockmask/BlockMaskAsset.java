package com.hypixel.hytale.builtin.hytalegenerator.assets.blockmask;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.builtin.hytalegenerator.BlockMask;
import com.hypixel.hytale.builtin.hytalegenerator.LoggerUtil;
import com.hypixel.hytale.builtin.hytalegenerator.MaterialSet;
import com.hypixel.hytale.builtin.hytalegenerator.assets.Cleanable;
import com.hypixel.hytale.builtin.hytalegenerator.assets.blockset.MaterialSetAsset;
import com.hypixel.hytale.builtin.hytalegenerator.material.MaterialCache;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class BlockMaskAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, BlockMaskAsset>>, Cleanable {
   @Nonnull
   private static final Map<String, BlockMaskAsset.Exported> exportedNodes = new HashMap<>();
   @Nonnull
   public static final AssetBuilderCodec<String, BlockMaskAsset> CODEC = AssetBuilderCodec.builder(
         BlockMaskAsset.class,
         BlockMaskAsset::new,
         Codec.STRING,
         (asset, id) -> asset.id = id,
         config -> config.id,
         (config, data) -> config.data = data,
         config -> config.data
      )
      .append(new KeyedCodec<>("DontPlace", MaterialSetAsset.CODEC, false), (t, k) -> t.dontPlaceMaterialSetAsset = k, t -> t.dontPlaceMaterialSetAsset)
      .add()
      .append(new KeyedCodec<>("DontReplace", MaterialSetAsset.CODEC, false), (t, k) -> t.dontReplaceMaterialSetAsset = k, t -> t.dontReplaceMaterialSetAsset)
      .add()
      .append(
         new KeyedCodec<>("Advanced", new ArrayCodec<>(BlockMaskEntryAsset.CODEC, BlockMaskEntryAsset[]::new), false),
         (t, k) -> t.blockMaskEntries = k,
         t -> t.blockMaskEntries
      )
      .add()
      .append(new KeyedCodec<>("ExportAs", Codec.STRING, false), (t, k) -> t.exportName = k, t -> t.exportName)
      .add()
      .append(new KeyedCodec<>("Import", Codec.STRING, false), (t, k) -> t.importName = k, t -> t.importName)
      .add()
      .afterDecode(asset -> {
         if (asset.exportName != null && !asset.exportName.isEmpty()) {
            if (exportedNodes.containsKey(asset.exportName)) {
               LoggerUtil.getLogger().warning("Duplicate export name for asset: " + asset.exportName);
            }

            BlockMaskAsset.Exported exported = new BlockMaskAsset.Exported();
            exported.asset = asset;
            exportedNodes.put(asset.exportName, exported);
            LoggerUtil.getLogger().fine("Registered imported node asset with name '" + asset.exportName + "' with asset id '" + asset.id);
         }
      })
      .build();
   private String id;
   private AssetExtraInfo.Data data;
   protected String exportName = "";
   protected String importName = "";
   private MaterialSetAsset dontPlaceMaterialSetAsset = new MaterialSetAsset();
   private MaterialSetAsset dontReplaceMaterialSetAsset = new MaterialSetAsset();
   private BlockMaskEntryAsset[] blockMaskEntries = new BlockMaskEntryAsset[0];

   public BlockMask build(@Nonnull MaterialCache materialCache) {
      if (this.importName != null && !this.importName.isEmpty()) {
         BlockMaskAsset.Exported importedAssetEntry = exportedNodes.get(this.importName);
         if (importedAssetEntry != null && importedAssetEntry.asset != null) {
            return importedAssetEntry.asset.build(materialCache);
         } else {
            LoggerUtil.getLogger().warning("Imported BlockMask asset with name '" + this.importName + "' not found");
            return new BlockMask();
         }
      } else {
         MaterialSet dontPlaceBlockSet = this.dontPlaceMaterialSetAsset == null ? new MaterialSet() : this.dontPlaceMaterialSetAsset.build(materialCache);
         MaterialSet dontReplaceBlockSet = this.dontReplaceMaterialSetAsset == null ? new MaterialSet() : this.dontReplaceMaterialSetAsset.build(materialCache);
         BlockMask blockMask = new BlockMask();
         blockMask.setSkippedBlocks(dontPlaceBlockSet);
         blockMask.setDefaultMask(dontReplaceBlockSet);

         for (BlockMaskEntryAsset entry : this.blockMaskEntries) {
            blockMask.putBlockMaskEntry(entry.getPropBlockSet(materialCache), entry.getReplacesBlockSet(materialCache));
         }

         return blockMask;
      }
   }

   public String getId() {
      return this.id;
   }

   @Override
   public void cleanUp() {
      this.dontPlaceMaterialSetAsset.cleanUp();
      this.dontReplaceMaterialSetAsset.cleanUp();

      for (BlockMaskEntryAsset blockMaskEntryAsset : this.blockMaskEntries) {
         blockMaskEntryAsset.cleanUp();
      }
   }

   public static class Exported {
      public BlockMaskAsset asset;
   }
}
