package com.hypixel.hytale.server.core.asset.type.buildertool.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.common.map.WeightedMap;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import java.util.Collections;
import java.util.HashSet;
import javax.annotation.Nonnull;

public class BlockTypeListAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, BlockTypeListAsset>> {
   public static final AssetBuilderCodec<String, BlockTypeListAsset> CODEC = AssetBuilderCodec.builder(
         BlockTypeListAsset.class,
         BlockTypeListAsset::new,
         Codec.STRING,
         (builder, id) -> builder.id = id,
         builder -> builder.id,
         (builder, data) -> builder.data = data,
         builder -> builder.data
      )
      .append(new KeyedCodec<>("Blocks", new ArrayCodec<>(Codec.STRING, String[]::new), true), (builder, blockTypeKeys) -> {
         if (blockTypeKeys != null) {
            Collections.addAll(builder.blockTypeKeys, blockTypeKeys);
         }
      }, builder -> builder.blockTypeKeys.toArray(String[]::new))
      .add()
      .afterDecode(blockTypeListAsset -> {
         if (blockTypeListAsset.blockTypeKeys != null) {
            WeightedMap.Builder<String> weightedMapBuilder = WeightedMap.builder(ArrayUtil.EMPTY_STRING_ARRAY);

            for (String blockTypeKey : blockTypeListAsset.blockTypeKeys) {
               weightedMapBuilder.put(blockTypeKey, 1.0);
            }

            blockTypeListAsset.blockPattern = new BlockPattern(weightedMapBuilder.build());
         }
      })
      .build();
   private static AssetStore<String, BlockTypeListAsset, DefaultAssetMap<String, BlockTypeListAsset>> ASSET_STORE;
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(BlockTypeListAsset::getAssetStore));
   private String id;
   private final HashSet<String> blockTypeKeys = new HashSet<>();
   private BlockPattern blockPattern;
   private AssetExtraInfo.Data data;

   public static AssetStore<String, BlockTypeListAsset, DefaultAssetMap<String, BlockTypeListAsset>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(BlockTypeListAsset.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, BlockTypeListAsset> getAssetMap() {
      return (DefaultAssetMap<String, BlockTypeListAsset>)getAssetStore().getAssetMap();
   }

   public BlockPattern getBlockPattern() {
      return this.blockPattern;
   }

   @Nonnull
   public HashSet<String> getBlockTypeKeys() {
      return this.blockTypeKeys;
   }

   public String getId() {
      return this.id;
   }
}
