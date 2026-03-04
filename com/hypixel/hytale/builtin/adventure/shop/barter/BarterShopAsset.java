package com.hypixel.hytale.builtin.adventure.shop.barter;

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
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BarterShopAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, BarterShopAsset>> {
   @Nonnull
   public static final AssetBuilderCodec<String, BarterShopAsset> CODEC = AssetBuilderCodec.builder(
         BarterShopAsset.class,
         BarterShopAsset::new,
         Codec.STRING,
         (asset, s) -> asset.id = s,
         asset -> asset.id,
         (asset, data) -> asset.extraData = data,
         asset -> asset.extraData
      )
      .addField(new KeyedCodec<>("DisplayNameKey", Codec.STRING), (asset, s) -> asset.displayNameKey = s, asset -> asset.displayNameKey)
      .addField(
         new KeyedCodec<>("RefreshInterval", RefreshInterval.CODEC), (asset, interval) -> asset.refreshInterval = interval, asset -> asset.refreshInterval
      )
      .addField(
         new KeyedCodec<>("Trades", new ArrayCodec<>(BarterTrade.CODEC, BarterTrade[]::new)), (asset, trades) -> asset.trades = trades, asset -> asset.trades
      )
      .addField(
         new KeyedCodec<>("TradeSlots", new ArrayCodec<>(TradeSlot.CODEC, TradeSlot[]::new)),
         (asset, slots) -> asset.tradeSlots = slots,
         asset -> asset.tradeSlots
      )
      .addField(new KeyedCodec<>("RestockHour", Codec.INTEGER, true), (asset, hour) -> asset.restockHour = hour, asset -> asset.restockHour)
      .build();
   @Nonnull
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(BarterShopAsset::getAssetStore));
   private static AssetStore<String, BarterShopAsset, DefaultAssetMap<String, BarterShopAsset>> ASSET_STORE;
   protected AssetExtraInfo.Data extraData;
   public static final int DEFAULT_RESTOCK_HOUR = 7;
   protected String id;
   protected String displayNameKey;
   protected RefreshInterval refreshInterval;
   protected BarterTrade[] trades;
   protected TradeSlot[] tradeSlots;
   @Nullable
   protected Integer restockHour;

   @Nonnull
   public static AssetStore<String, BarterShopAsset, DefaultAssetMap<String, BarterShopAsset>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(BarterShopAsset.class);
      }

      return ASSET_STORE;
   }

   public static DefaultAssetMap<String, BarterShopAsset> getAssetMap() {
      return (DefaultAssetMap<String, BarterShopAsset>)getAssetStore().getAssetMap();
   }

   public BarterShopAsset(
      String id, String displayNameKey, RefreshInterval refreshInterval, BarterTrade[] trades, TradeSlot[] tradeSlots, @Nullable Integer restockHour
   ) {
      this.id = id;
      this.displayNameKey = displayNameKey;
      this.refreshInterval = refreshInterval;
      this.trades = trades;
      this.tradeSlots = tradeSlots;
      this.restockHour = restockHour;
   }

   protected BarterShopAsset() {
   }

   public String getId() {
      return this.id;
   }

   public String getDisplayNameKey() {
      return this.displayNameKey;
   }

   public RefreshInterval getRefreshInterval() {
      return this.refreshInterval;
   }

   public BarterTrade[] getTrades() {
      return this.trades;
   }

   @Nullable
   public TradeSlot[] getTradeSlots() {
      return this.tradeSlots;
   }

   public boolean hasTradeSlots() {
      return this.tradeSlots != null && this.tradeSlots.length > 0;
   }

   public int getRestockHour() {
      return this.restockHour != null ? this.restockHour : 7;
   }

   @Nonnull
   @Override
   public String toString() {
      return "BarterShopAsset{id='"
         + this.id
         + "', displayNameKey='"
         + this.displayNameKey
         + "', refreshInterval="
         + this.refreshInterval
         + ", restockHour="
         + this.getRestockHour()
         + ", trades="
         + Arrays.toString((Object[])this.trades)
         + ", tradeSlots="
         + Arrays.toString((Object[])this.tradeSlots)
         + "}";
   }
}
