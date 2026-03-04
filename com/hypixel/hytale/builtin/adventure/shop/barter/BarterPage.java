package com.hypixel.hytale.builtin.adventure.shop.barter;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.ItemUtils;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.InteractiveCustomUIPage;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.transaction.ItemStackTransaction;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BarterPage extends InteractiveCustomUIPage<BarterPage.BarterEventData> {
   @Nullable
   private final BarterShopAsset shopAsset;

   public BarterPage(@Nonnull PlayerRef playerRef, @Nonnull String shopId) {
      super(playerRef, CustomPageLifetime.CanDismissOrCloseThroughInteraction, BarterPage.BarterEventData.CODEC);
      this.shopAsset = BarterShopAsset.getAssetMap().getAsset(shopId);
   }

   private boolean isTradeValid(@Nonnull BarterTrade trade) {
      if (!ItemModule.exists(trade.getOutput().getItemId())) {
         return false;
      } else {
         for (BarterItemStack input : trade.getInput()) {
            if (!ItemModule.exists(input.getItemId())) {
               return false;
            }
         }

         return true;
      }
   }

   @Nonnull
   private String getSafeItemId(@Nonnull String itemId) {
      return ItemModule.exists(itemId) ? itemId : "Unknown";
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      if (this.shopAsset != null) {
         commandBuilder.append("Pages/BarterPage.ui");
         String titleKey = this.shopAsset.getDisplayNameKey() != null ? this.shopAsset.getDisplayNameKey() : this.shopAsset.getId();
         commandBuilder.set("#ShopTitle.Text", Message.translation(titleKey));
         WorldTimeResource timeResource = store.getResource(WorldTimeResource.getResourceType());
         Instant gameTime = timeResource.getGameTime();
         BarterShopState barterState = BarterShopState.get();
         int[] stockArray = barterState.getStockArray(this.shopAsset, gameTime);
         Message refreshMessage = this.getRefreshTimerText(barterState, gameTime);
         if (refreshMessage != null) {
            commandBuilder.set("#RefreshTimer.Text", refreshMessage);
         }

         commandBuilder.clear("#TradeGrid");
         Ref<EntityStore> playerEntityRef = this.playerRef.getReference();
         Player playerComponent = playerEntityRef != null ? store.getComponent(playerEntityRef, Player.getComponentType()) : null;
         ItemContainer playerInventory = null;
         if (playerComponent != null) {
            playerInventory = playerComponent.getInventory().getCombinedHotbarFirst();
         }

         BarterTrade[] trades = barterState.getResolvedTrades(this.shopAsset, gameTime);

         for (int i = 0; i < trades.length; i++) {
            BarterTrade trade = trades[i];
            String selector = "#TradeGrid[" + i + "]";
            int stock = i < stockArray.length ? stockArray[i] : 0;
            boolean tradeValid = this.isTradeValid(trade);
            commandBuilder.append("#TradeGrid", "Pages/BarterTradeRow.ui");
            commandBuilder.set(selector + " #OutputSlot.ItemId", this.getSafeItemId(trade.getOutput().getItemId()));
            int outputQty = trade.getOutput().getQuantity();
            commandBuilder.set(selector + " #OutputQuantity.Text", outputQty > 1 ? String.valueOf(outputQty) : "");
            boolean canAfford = true;
            int playerHas = 0;
            if (trade.getInput().length > 0) {
               BarterItemStack firstInput = trade.getInput()[0];
               String inputItemId = firstInput.getItemId();
               int inputQty = firstInput.getQuantity();
               commandBuilder.set(selector + " #InputSlot.ItemId", this.getSafeItemId(inputItemId));
               commandBuilder.set(selector + " #InputQuantity.Text", inputQty > 1 ? String.valueOf(inputQty) : "");
               if (ItemModule.exists(inputItemId)) {
                  playerHas = playerInventory != null ? this.countItemsInContainer(playerInventory, inputItemId) : 0;
                  canAfford = playerHas >= inputQty;
               } else {
                  canAfford = false;
               }

               commandBuilder.set(selector + " #InputSlotBorder.Background", canAfford ? "#2a5a3a" : "#5a2a2a");
               commandBuilder.set(
                  selector + " #HaveNeedLabel.Text", Message.translation("server.barter.customUI.barterPage.quantityStock").param("count", playerHas)
               );
               commandBuilder.set(selector + " #HaveNeedLabel.Style.TextColor", canAfford ? "#3d913f" : "#962f2f");
            }

            if (!tradeValid) {
               commandBuilder.set(selector + " #Stock.Visible", false);
               commandBuilder.set(selector + " #OutOfStockOverlay.Visible", true);
               commandBuilder.set(selector + " #OutOfStockLabel.Text", Message.translation("server.barter.customUI.barterPage.invalidItem"));
               commandBuilder.set(selector + " #OutOfStockLabel.Style.TextColor", "#cc8844");
               commandBuilder.set(selector + " #TradeButton.Disabled", true);
               commandBuilder.set(selector + " #TradeButton.Style.Disabled.Background", "#4a3020");
            } else if (stock <= 0) {
               commandBuilder.set(selector + " #Stock.Visible", false);
               commandBuilder.set(selector + " #OutOfStockOverlay.Visible", true);
               commandBuilder.set(selector + " #OutOfStockLabel.Text", Message.translation("server.barter.customUI.barterPage.noStock"));
               commandBuilder.set(selector + " #OutOfStockLabel.Style.TextColor", "#cc4444");
               commandBuilder.set(selector + " #TradeButton.Disabled", true);
               commandBuilder.set(selector + " #TradeButton.Style.Disabled.Background", "#4a2020");
            } else {
               commandBuilder.set(selector + " #Stock.TextSpans", Message.translation("server.barter.customUI.barterPage.inStock").param("count", stock));
            }

            eventBuilder.addEventBinding(
               CustomUIEventBindingType.Activating, selector + " #TradeButton", EventData.of("TradeIndex", String.valueOf(i)).append("Quantity", "1"), false
            );
            eventBuilder.addEventBinding(
               CustomUIEventBindingType.RightClicking, selector + " #TradeButton", EventData.of("TradeIndex", String.valueOf(i)).append("Quantity", "1"), false
            );
         }

         int cardsPerRow = 3;
         int remainder = trades.length % 3;
         if (remainder > 0) {
            int spacersNeeded = 3 - remainder;

            for (int s = 0; s < spacersNeeded; s++) {
               commandBuilder.append("#TradeGrid", "Pages/BarterGridSpacer.ui");
            }
         }
      }
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull BarterPage.BarterEventData data) {
      if (this.shopAsset != null) {
         int tradeIndex = data.getTradeIndex();
         int requestedQuantity = data.getQuantity();
         if (requestedQuantity > 0) {
            WorldTimeResource timeResource = store.getResource(WorldTimeResource.getResourceType());
            Instant gameTime = timeResource.getGameTime();
            BarterShopState barterState = BarterShopState.get();
            BarterTrade[] trades = barterState.getResolvedTrades(this.shopAsset, gameTime);
            if (tradeIndex >= 0 && tradeIndex < trades.length) {
               BarterTrade trade = trades[tradeIndex];
               if (this.isTradeValid(trade)) {
                  BarterShopState.ShopInstanceState shopState = barterState.getOrCreateShopState(this.shopAsset, gameTime);
                  int currentStock = shopState.getCurrentStock()[tradeIndex];
                  if (currentStock > 0) {
                     Ref<EntityStore> playerEntityRef = this.playerRef.getReference();
                     if (playerEntityRef != null) {
                        Player playerComponent = store.getComponent(playerEntityRef, Player.getComponentType());
                        if (playerComponent != null) {
                           Inventory inventory = playerComponent.getInventory();
                           CombinedItemContainer container = inventory.getCombinedHotbarFirst();
                           int maxQuantity = Math.min(requestedQuantity, currentStock);

                           for (BarterItemStack inputStack : trade.getInput()) {
                              int has = this.countItemsInContainer(container, inputStack.getItemId());
                              int canAfford = has / inputStack.getQuantity();
                              maxQuantity = Math.min(maxQuantity, canAfford);
                           }

                           if (maxQuantity > 0) {
                              int quantity = maxQuantity;

                              for (BarterItemStack inputStack : trade.getInput()) {
                                 int toRemove = inputStack.getQuantity() * quantity;
                                 this.removeItemsFromContainer(container, inputStack.getItemId(), toRemove);
                              }

                              BarterItemStack output = trade.getOutput();
                              ItemStack outputStack = new ItemStack(output.getItemId(), output.getQuantity() * quantity);
                              ItemStackTransaction transaction = container.addItemStack(outputStack);
                              ItemStack remainder = transaction.getRemainder();
                              if (remainder != null && !remainder.isEmpty()) {
                                 int addedQty = outputStack.getQuantity() - remainder.getQuantity();
                                 if (addedQty > 0) {
                                    playerComponent.notifyPickupItem(playerEntityRef, outputStack.withQuantity(addedQty), null, store);
                                 }

                                 ItemUtils.dropItem(playerEntityRef, remainder, store);
                              } else {
                                 playerComponent.notifyPickupItem(playerEntityRef, outputStack, null, store);
                              }

                              barterState.executeTrade(this.shopAsset, tradeIndex, quantity, gameTime);
                              this.updateAfterTrade(ref, store, tradeIndex);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private void updateAfterTrade(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, int tradedIndex) {
      UICommandBuilder commandBuilder = new UICommandBuilder();
      WorldTimeResource timeResource = store.getResource(WorldTimeResource.getResourceType());
      Instant gameTime = timeResource.getGameTime();
      BarterShopState barterState = BarterShopState.get();
      int[] stockArray = barterState.getStockArray(this.shopAsset, gameTime);
      BarterTrade[] trades = barterState.getResolvedTrades(this.shopAsset, gameTime);
      Ref<EntityStore> playerEntityRef = this.playerRef.getReference();
      Player playerComponent = playerEntityRef != null ? store.getComponent(playerEntityRef, Player.getComponentType()) : null;
      ItemContainer playerInventory = null;
      if (playerComponent != null) {
         playerInventory = playerComponent.getInventory().getCombinedHotbarFirst();
      }

      for (int i = 0; i < trades.length; i++) {
         BarterTrade trade = trades[i];
         String selector = "#TradeGrid[" + i + "]";
         int stock = i < stockArray.length ? stockArray[i] : 0;
         boolean tradeValid = this.isTradeValid(trade);
         if (trade.getInput().length > 0) {
            BarterItemStack firstInput = trade.getInput()[0];
            int playerHas = 0;
            boolean canAfford = false;
            if (ItemModule.exists(firstInput.getItemId())) {
               playerHas = playerInventory != null ? this.countItemsInContainer(playerInventory, firstInput.getItemId()) : 0;
               canAfford = playerHas >= firstInput.getQuantity();
            }

            commandBuilder.set(selector + " #InputSlotBorder.Background", canAfford ? "#2a5a3a" : "#5a2a2a");
            commandBuilder.set(
               selector + " #HaveNeedLabel.Text", Message.translation("server.barter.customUI.barterPage.quantityStock").param("count", playerHas)
            );
            commandBuilder.set(selector + " #HaveNeedLabel.Style.TextColor", canAfford ? "#3d913f" : "#962f2f");
         }

         if (!tradeValid) {
            commandBuilder.set(selector + " #Stock.Visible", false);
            commandBuilder.set(selector + " #OutOfStockOverlay.Visible", true);
            commandBuilder.set(selector + " #TradeButton.Disabled", true);
         } else if (stock <= 0) {
            commandBuilder.set(selector + " #Stock.Visible", false);
            commandBuilder.set(selector + " #OutOfStockOverlay.Visible", true);
            commandBuilder.set(selector + " #OutOfStockLabel.Text", Message.translation("server.barter.customUI.barterPage.noStock"));
            commandBuilder.set(selector + " #OutOfStockLabel.Style.TextColor", "#cc4444");
            commandBuilder.set(selector + " #TradeButton.Disabled", true);
            commandBuilder.set(selector + " #TradeButton.Style.Disabled.Background", "#4a2020");
         } else {
            commandBuilder.set(selector + " #Stock.Visible", true);
            commandBuilder.set(selector + " #Stock.TextSpans", Message.translation("server.barter.customUI.barterPage.inStock").param("count", stock));
            commandBuilder.set(selector + " #OutOfStockOverlay.Visible", false);
            commandBuilder.set(selector + " #TradeButton.Disabled", false);
            commandBuilder.set(selector + " #TradeButton.Style.Default.Background", "#1e2a3a");
         }
      }

      this.sendUpdate(commandBuilder, new UIEventBuilder(), false);
   }

   private int countItemsInContainer(@Nonnull ItemContainer container, @Nonnull String itemId) {
      return container.countItemStacks(stack -> itemId.equals(stack.getItemId()));
   }

   private void removeItemsFromContainer(@Nonnull ItemContainer container, @Nonnull String itemId, int amount) {
      container.removeItemStack(new ItemStack(itemId, amount));
   }

   private void refreshUI(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      UICommandBuilder commandBuilder = new UICommandBuilder();
      UIEventBuilder eventBuilder = new UIEventBuilder();
      this.build(ref, commandBuilder, eventBuilder, store);
      this.sendUpdate(commandBuilder, eventBuilder, true);
   }

   @Nullable
   private Message getRefreshTimerText(@Nonnull BarterShopState barterState, @Nonnull Instant gameTime) {
      if (this.shopAsset == null) {
         return null;
      } else {
         RefreshInterval interval = this.shopAsset.getRefreshInterval();
         if (interval == null) {
            return null;
         } else {
            BarterShopState.ShopInstanceState shopState = barterState.getOrCreateShopState(this.shopAsset, gameTime);
            Instant nextRefresh = shopState.getNextRefreshTime();
            if (nextRefresh == null) {
               return null;
            } else {
               Duration remaining = Duration.between(gameTime, nextRefresh);
               if (!remaining.isNegative() && !remaining.isZero()) {
                  long currentDayNumber = gameTime.getEpochSecond() / WorldTimeResource.SECONDS_PER_DAY;
                  long refreshDayNumber = nextRefresh.getEpochSecond() / WorldTimeResource.SECONDS_PER_DAY;
                  long daysUntilRefresh = refreshDayNumber - currentDayNumber;
                  LocalTime restockTime = LocalTime.of(this.shopAsset.getRestockHour(), 0);
                  String timeString = restockTime.toString();
                  if (daysUntilRefresh <= 0L) {
                     return Message.translation("server.barter.customUI.barterPage.restocksToday").param("restockTime", timeString);
                  } else {
                     return daysUntilRefresh == 1L
                        ? Message.translation("server.barter.customUI.barterPage.restocksTomorrow").param("restockTime", timeString)
                        : Message.translation("server.barter.customUI.barterPage.restocksInDays").param("days", (int)daysUntilRefresh);
                  }
               } else {
                  return null;
               }
            }
         }
      }
   }

   public static class BarterEventData {
      @Nonnull
      static final String TRADE_INDEX = "TradeIndex";
      @Nonnull
      static final String QUANTITY = "Quantity";
      @Nonnull
      static final String SHIFT_HELD = "ShiftHeld";
      @Nonnull
      public static final BuilderCodec<BarterPage.BarterEventData> CODEC = BuilderCodec.builder(
            BarterPage.BarterEventData.class, BarterPage.BarterEventData::new
         )
         .append(new KeyedCodec<>("TradeIndex", Codec.STRING), (data, s) -> data.tradeIndex = Integer.parseInt(s), data -> String.valueOf(data.tradeIndex))
         .add()
         .append(new KeyedCodec<>("Quantity", Codec.STRING), (data, s) -> data.quantity = Integer.parseInt(s), data -> String.valueOf(data.quantity))
         .add()
         .append(new KeyedCodec<>("ShiftHeld", Codec.BOOLEAN), (data, b) -> {
            if (b != null) {
               data.shiftHeld = b;
            }
         }, data -> data.shiftHeld)
         .add()
         .build();
      private int tradeIndex;
      private int quantity = 1;
      private boolean shiftHeld = false;

      public int getTradeIndex() {
         return this.tradeIndex;
      }

      public int getQuantity() {
         return this.shiftHeld ? 10 : this.quantity;
      }

      public boolean isShiftHeld() {
         return this.shiftHeld;
      }
   }
}
