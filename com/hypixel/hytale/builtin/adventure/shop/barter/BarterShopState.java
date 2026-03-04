package com.hypixel.hytale.builtin.adventure.shop.barter;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.MapCodec;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.util.BsonUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.bson.BsonDocument;

public class BarterShopState {
   @Nonnull
   private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
   @Nullable
   private static BarterShopState instance;
   private static Path saveDirectory;
   @Nonnull
   public static final BuilderCodec<BarterShopState.ShopInstanceState> SHOP_INSTANCE_CODEC = BuilderCodec.builder(
         BarterShopState.ShopInstanceState.class, BarterShopState.ShopInstanceState::new
      )
      .append(new KeyedCodec<>("Stock", Codec.INT_ARRAY), (state, stock) -> state.currentStock = stock, state -> state.currentStock)
      .add()
      .append(new KeyedCodec<>("NextRefresh", Codec.INSTANT, true), (state, instant) -> state.nextRefreshTime = instant, state -> state.nextRefreshTime)
      .add()
      .append(new KeyedCodec<>("ResolveSeed", Codec.LONG, true), (state, seed) -> state.resolveSeed = seed, state -> state.resolveSeed)
      .add()
      .build();
   @Nonnull
   public static final BuilderCodec<BarterShopState> CODEC = BuilderCodec.builder(BarterShopState.class, BarterShopState::new)
      .append(
         new KeyedCodec<>("Shops", new MapCodec<>(SHOP_INSTANCE_CODEC, Object2ObjectOpenHashMap::new, false)),
         (state, shops) -> state.shopStates.putAll(shops),
         state -> new Object2ObjectOpenHashMap(state.shopStates)
      )
      .add()
      .build();
   @Nonnull
   private final Map<String, BarterShopState.ShopInstanceState> shopStates = new ConcurrentHashMap<>();

   public static void initialize(@Nonnull Path dataDirectory) {
      saveDirectory = dataDirectory;
      load();
   }

   @Nonnull
   public static BarterShopState get() {
      if (instance == null) {
         instance = new BarterShopState();
      }

      return instance;
   }

   public static void load() {
      if (saveDirectory == null) {
         LOGGER.at(Level.WARNING).log("Cannot load barter shop state: save directory not set");
         instance = new BarterShopState();
      } else {
         Path file = saveDirectory.resolve("barter_shop_state.json");
         if (!Files.exists(file)) {
            LOGGER.at(Level.INFO).log("No saved barter shop state found, starting fresh");
            instance = new BarterShopState();
         } else {
            try {
               BsonDocument document = BsonUtil.readDocumentNow(file);
               if (document != null) {
                  ExtraInfo extraInfo = ExtraInfo.THREAD_LOCAL.get();
                  instance = CODEC.decode(document, extraInfo);
                  extraInfo.getValidationResults().logOrThrowValidatorExceptions(LOGGER);
                  LOGGER.at(Level.INFO).log("Loaded barter shop state with %d shops", instance.shopStates.size());
               } else {
                  instance = new BarterShopState();
               }
            } catch (Exception var3) {
               ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var3)).log("Failed to load barter shop state, starting fresh");
               instance = new BarterShopState();
            }
         }
      }
   }

   public static void save() {
      if (saveDirectory != null && instance != null) {
         try {
            if (!Files.exists(saveDirectory)) {
               Files.createDirectories(saveDirectory);
            }

            Path file = saveDirectory.resolve("barter_shop_state.json");
            BsonUtil.writeSync(file, CODEC, instance, LOGGER);
            LOGGER.at(Level.FINE).log("Saved barter shop state");
         } catch (IOException var1) {
            ((HytaleLogger.Api)LOGGER.at(Level.WARNING).withCause(var1)).log("Failed to save barter shop state");
         }
      }
   }

   public static void shutdown() {
      save();
      instance = null;
   }

   @Nonnull
   private static Instant calculateNextScheduledRestock(@Nonnull Instant gameTime, int intervalDays, int restockHour) {
      LocalDateTime dateTime = LocalDateTime.ofInstant(gameTime, ZoneOffset.UTC);
      long daysSinceEpoch = Duration.between(WorldTimeResource.ZERO_YEAR, gameTime).toDays();
      long currentCycle = daysSinceEpoch / intervalDays;
      long nextRestockDaySinceEpoch = (currentCycle + 1L) * intervalDays;
      boolean isTodayRestockDay = daysSinceEpoch % intervalDays == 0L;
      if (isTodayRestockDay && dateTime.getHour() < restockHour) {
         nextRestockDaySinceEpoch = daysSinceEpoch;
      }

      return WorldTimeResource.ZERO_YEAR.plus(Duration.ofDays(nextRestockDaySinceEpoch)).plus(Duration.ofHours(restockHour));
   }

   @Nonnull
   public BarterShopState.ShopInstanceState getOrCreateShopState(@Nonnull BarterShopAsset asset, @Nonnull Instant gameTime) {
      return this.shopStates.computeIfAbsent(asset.getId(), id -> {
         BarterShopState.ShopInstanceState state = new BarterShopState.ShopInstanceState();
         state.resetStockAndResolve(asset);
         RefreshInterval interval = asset.getRefreshInterval();
         if (interval != null) {
            state.setNextRefreshTime(calculateNextScheduledRestock(gameTime, interval.getDays(), asset.getRestockHour()));
         }

         return state;
      });
   }

   public void checkRefresh(@Nonnull BarterShopAsset asset, @Nonnull Instant gameTime) {
      RefreshInterval interval = asset.getRefreshInterval();
      if (interval != null) {
         BarterShopState.ShopInstanceState state = this.getOrCreateShopState(asset, gameTime);
         Instant nextRefresh = state.getNextRefreshTime();
         if (nextRefresh == null) {
            state.setNextRefreshTime(calculateNextScheduledRestock(gameTime, interval.getDays(), asset.getRestockHour()));
            save();
         } else {
            if (!gameTime.isBefore(nextRefresh)) {
               state.resetStockAndResolve(asset);
               state.setNextRefreshTime(calculateNextScheduledRestock(gameTime, interval.getDays(), asset.getRestockHour()));
               save();
            }
         }
      }
   }

   public int[] getStockArray(@Nonnull BarterShopAsset asset, @Nonnull Instant gameTime) {
      this.checkRefresh(asset, gameTime);
      BarterShopState.ShopInstanceState state = this.getOrCreateShopState(asset, gameTime);
      if (state.expandStockIfNeeded(asset)) {
         save();
      }

      return (int[])state.getCurrentStock().clone();
   }

   @Nonnull
   public BarterTrade[] getResolvedTrades(@Nonnull BarterShopAsset asset, @Nonnull Instant gameTime) {
      this.checkRefresh(asset, gameTime);
      BarterShopState.ShopInstanceState state = this.getOrCreateShopState(asset, gameTime);
      return state.getResolvedTrades(asset);
   }

   public boolean executeTrade(@Nonnull BarterShopAsset asset, int tradeIndex, int quantity, @Nonnull Instant gameTime) {
      this.checkRefresh(asset, gameTime);
      BarterShopState.ShopInstanceState state = this.getOrCreateShopState(asset, gameTime);
      boolean success = state.decrementStock(tradeIndex, quantity);
      if (success) {
         save();
      }

      return success;
   }

   public static class ShopInstanceState {
      @Nonnull
      public static final BarterTrade[] BARTER_TRADES = new BarterTrade[0];
      @Nonnull
      public static final int[] INTS = new int[0];
      private int[] currentStock = INTS;
      @Nullable
      private Instant nextRefreshTime;
      @Nullable
      private Long resolveSeed;
      @Nullable
      private transient BarterTrade[] resolvedTrades;

      public ShopInstanceState() {
      }

      public ShopInstanceState(int tradeCount) {
         this.currentStock = new int[tradeCount];
         this.nextRefreshTime = null;
      }

      public int[] getCurrentStock() {
         return this.currentStock;
      }

      @Nullable
      public Instant getNextRefreshTime() {
         return this.nextRefreshTime;
      }

      public void setNextRefreshTime(@Nonnull Instant time) {
         this.nextRefreshTime = time;
      }

      @Nullable
      public Long getResolveSeed() {
         return this.resolveSeed;
      }

      public void setResolveSeed(Long seed) {
         this.resolveSeed = seed;
      }

      @Nonnull
      public BarterTrade[] getResolvedTrades(@Nonnull BarterShopAsset asset) {
         if (!asset.hasTradeSlots()) {
            return asset.getTrades() != null ? asset.getTrades() : BARTER_TRADES;
         } else if (this.resolvedTrades != null) {
            return this.resolvedTrades;
         } else {
            if (this.resolveSeed == null) {
               this.resolveSeed = ThreadLocalRandom.current().nextLong();
            }

            this.resolvedTrades = resolveTradeSlots(asset, this.resolveSeed);
            return this.resolvedTrades;
         }
      }

      @Nonnull
      private static BarterTrade[] resolveTradeSlots(@Nonnull BarterShopAsset asset, long seed) {
         TradeSlot[] slots = asset.getTradeSlots();
         if (slots != null && slots.length != 0) {
            Random random = new Random(seed);
            List<BarterTrade> result = new ObjectArrayList();

            for (TradeSlot slot : slots) {
               result.addAll(slot.resolve(random));
            }

            return result.toArray(BARTER_TRADES);
         } else {
            return BARTER_TRADES;
         }
      }

      public void resetStockAndResolve(@Nonnull BarterShopAsset asset) {
         if (asset.hasTradeSlots()) {
            this.resolveSeed = ThreadLocalRandom.current().nextLong();
            this.resolvedTrades = resolveTradeSlots(asset, this.resolveSeed);
         } else {
            this.resolvedTrades = null;
         }

         BarterTrade[] trades = this.getResolvedTrades(asset);
         this.currentStock = new int[trades.length];

         for (int i = 0; i < trades.length; i++) {
            this.currentStock[i] = trades[i].getMaxStock();
         }
      }

      /** @deprecated */
      public void resetStock(@Nonnull BarterShopAsset asset) {
         BarterTrade[] trades = this.getResolvedTrades(asset);
         if (this.currentStock.length != trades.length) {
            this.currentStock = new int[trades.length];
         }

         for (int i = 0; i < trades.length; i++) {
            this.currentStock[i] = trades[i].getMaxStock();
         }
      }

      public boolean expandStockIfNeeded(@Nonnull BarterShopAsset asset) {
         BarterTrade[] trades = this.getResolvedTrades(asset);
         if (this.currentStock.length >= trades.length) {
            return false;
         } else {
            int[] newStock = new int[trades.length];
            System.arraycopy(this.currentStock, 0, newStock, 0, this.currentStock.length);

            for (int i = this.currentStock.length; i < trades.length; i++) {
               newStock[i] = trades[i].getMaxStock();
            }

            this.currentStock = newStock;
            return true;
         }
      }

      public boolean hasStock(int tradeIndex, int quantity) {
         return tradeIndex >= 0 && tradeIndex < this.currentStock.length ? this.currentStock[tradeIndex] >= quantity : false;
      }

      public boolean decrementStock(int tradeIndex, int quantity) {
         if (!this.hasStock(tradeIndex, quantity)) {
            return false;
         } else {
            this.currentStock[tradeIndex] = this.currentStock[tradeIndex] - quantity;
            return true;
         }
      }

      public int getStock(int tradeIndex) {
         return tradeIndex >= 0 && tradeIndex < this.currentStock.length ? this.currentStock[tradeIndex] : 0;
      }
   }
}
