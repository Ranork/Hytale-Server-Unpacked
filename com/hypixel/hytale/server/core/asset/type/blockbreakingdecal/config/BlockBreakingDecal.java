package com.hypixel.hytale.server.core.asset.type.blockbreakingdecal.config;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.codec.AssetCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.codec.validation.validator.ArrayValidator;
import com.hypixel.hytale.server.core.asset.common.CommonAssetValidator;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class BlockBreakingDecal
   implements JsonAssetWithMap<String, DefaultAssetMap<String, BlockBreakingDecal>>,
   NetworkSerializable<com.hypixel.hytale.protocol.BlockBreakingDecal> {
   private static final String[] DEFAULT_STAGE_TEXTURE_LIST = new String[0];
   public static final AssetCodec<String, BlockBreakingDecal> CODEC = AssetBuilderCodec.builder(
         BlockBreakingDecal.class,
         BlockBreakingDecal::new,
         Codec.STRING,
         (t, k) -> t.id = k,
         t -> t.id,
         (asset, data) -> asset.data = data,
         asset -> asset.data
      )
      .append(
         new KeyedCodec<>("StageTextures", Codec.STRING_ARRAY), (blockSet, strings) -> blockSet.stageTextures = strings, blockSet -> blockSet.stageTextures
      )
      .addValidator(new ArrayValidator<>(CommonAssetValidator.TEXTURE_ITEM))
      .add()
      .build();
   private static AssetStore<String, BlockBreakingDecal, DefaultAssetMap<String, BlockBreakingDecal>> ASSET_STORE;
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(BlockBreakingDecal::getAssetStore));
   private String id;
   private AssetExtraInfo.Data data;
   private String[] stageTextures = DEFAULT_STAGE_TEXTURE_LIST;

   public static AssetStore<String, BlockBreakingDecal, DefaultAssetMap<String, BlockBreakingDecal>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(BlockBreakingDecal.class);
      }

      return ASSET_STORE;
   }

   protected BlockBreakingDecal() {
   }

   @Nonnull
   public com.hypixel.hytale.protocol.BlockBreakingDecal toPacket() {
      com.hypixel.hytale.protocol.BlockBreakingDecal packet = new com.hypixel.hytale.protocol.BlockBreakingDecal();
      packet.stageTextures = this.stageTextures;
      return packet;
   }

   public String getId() {
      return this.id;
   }

   @Nonnull
   @Override
   public String toString() {
      return "BlockBreakingDecal{id='" + this.id + "', data=" + this.data + ", stageTextures=" + Arrays.toString((Object[])this.stageTextures) + "}";
   }
}
