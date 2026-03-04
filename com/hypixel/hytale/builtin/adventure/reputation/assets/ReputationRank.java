package com.hypixel.hytale.builtin.adventure.reputation.assets;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import javax.annotation.Nonnull;

public class ReputationRank implements JsonAssetWithMap<String, DefaultAssetMap<String, ReputationRank>> {
   @Nonnull
   public static final AssetBuilderCodec<String, ReputationRank> CODEC = AssetBuilderCodec.builder(
         ReputationRank.class, ReputationRank::new, Codec.STRING, (t, k) -> t.id = k, t -> t.id, (t, data) -> t.data = data, t -> t.data
      )
      .addField(new KeyedCodec<>("MinValue", Codec.INTEGER), (reputationRank, s) -> reputationRank.minValue = s, reputationRank -> reputationRank.minValue)
      .addField(new KeyedCodec<>("MaxValue", Codec.INTEGER), (reputationRank, s) -> reputationRank.maxValue = s, reputationRank -> reputationRank.maxValue)
      .addField(
         new KeyedCodec<>("Attitude", Attitude.CODEC, true), (reputationRank, s) -> reputationRank.attitude = s, reputationRank -> reputationRank.attitude
      )
      .validator((asset, results) -> {
         if (asset.getMinValue() >= asset.getMaxValue()) {
            results.fail("Min value must be strictly inferior than the max value");
         }
      })
      .build();
   @Nonnull
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(ReputationRank::getAssetStore));
   private static AssetStore<String, ReputationRank, DefaultAssetMap<String, ReputationRank>> ASSET_STORE;
   protected AssetExtraInfo.Data data;
   protected String id;
   protected int minValue;
   protected int maxValue;
   protected Attitude attitude;

   @Nonnull
   public static AssetStore<String, ReputationRank, DefaultAssetMap<String, ReputationRank>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(ReputationRank.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, ReputationRank> getAssetMap() {
      return (DefaultAssetMap<String, ReputationRank>)getAssetStore().getAssetMap();
   }

   public ReputationRank(String id, int minValue, int maxValue, Attitude attitude) {
      this.id = id;
      this.minValue = minValue;
      this.maxValue = maxValue;
      this.attitude = attitude;
   }

   protected ReputationRank() {
   }

   public String getId() {
      return this.id;
   }

   public int getMinValue() {
      return this.minValue;
   }

   public int getMaxValue() {
      return this.maxValue;
   }

   public Attitude getAttitude() {
      return this.attitude;
   }

   public boolean containsValue(int value) {
      return value >= this.minValue && value < this.maxValue;
   }

   @Nonnull
   @Override
   public String toString() {
      return "ReputationRank{id='" + this.id + "', minValue=" + this.minValue + ", maxValue=" + this.maxValue + ", attitude=" + this.attitude + "}";
   }
}
