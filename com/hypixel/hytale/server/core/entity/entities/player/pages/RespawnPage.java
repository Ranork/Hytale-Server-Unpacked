package com.hypixel.hytale.server.core.entity.entities.player.pages;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.common.util.CompletableFutureUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.gameplay.DeathConfig;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathComponent;
import com.hypixel.hytale.server.core.modules.entity.damage.DeathItemLoss;
import com.hypixel.hytale.server.core.ui.builder.EventData;
import com.hypixel.hytale.server.core.ui.builder.UICommandBuilder;
import com.hypixel.hytale.server.core.ui.builder.UIEventBuilder;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RespawnPage extends InteractiveCustomUIPage<RespawnPage.RespawnPageEventData> {
   @Nonnull
   private static final String UI_RESPAWN_PAGE = "Pages/RespawnPage.ui";
   @Nonnull
   private static final String ELEMENT_BUTTON_RESPAWN = "#RespawnButton";
   @Nonnull
   private static final String ELEMENT_LABEL_DEATH_REASON_TEXT_SPANS = "#DeathReason.TextSpans";
   @Nullable
   private final Message deathReason;
   private final boolean displayDataOnDeathScreen;
   private final DeathItemLoss deathItemLoss;
   private final ItemStack[] itemsLostOnDeath;

   public RespawnPage(@Nonnull PlayerRef playerRef, @Nullable Message deathReason, boolean displayDataOnDeathScreen, DeathItemLoss deathItemLoss) {
      super(playerRef, CustomPageLifetime.CantClose, RespawnPage.RespawnPageEventData.CODEC);
      this.deathReason = deathReason;
      this.displayDataOnDeathScreen = displayDataOnDeathScreen;
      this.deathItemLoss = deathItemLoss;
      this.itemsLostOnDeath = combineSimilarItemStacks(deathItemLoss.getItemsLost());
   }

   @Nullable
   private static ItemStack[] combineSimilarItemStacks(@Nullable ItemStack[] itemsLostOnDeath) {
      if (itemsLostOnDeath == null) {
         return null;
      } else {
         ArrayList<ItemStack> singleItemStacks = new ArrayList<>();
         HashMap<String, ItemStack> combinedItemStacks = new HashMap<>();

         for (ItemStack itemStack : itemsLostOnDeath) {
            if (itemStack.getItem().getMaxStack() <= 1) {
               singleItemStacks.add(itemStack);
            } else {
               String itemId = itemStack.getItemId();
               int quantity = itemStack.getQuantity();
               ItemStack combinedItemStack = combinedItemStacks.get(itemId);
               if (combinedItemStack != null) {
                  quantity += combinedItemStack.getQuantity();
               }

               combinedItemStacks.put(itemId, itemStack.withQuantity(quantity));
            }
         }

         singleItemStacks.addAll(combinedItemStacks.values());
         return singleItemStacks.toArray(ItemStack[]::new);
      }
   }

   @Override
   public void build(
      @Nonnull Ref<EntityStore> ref, @Nonnull UICommandBuilder commandBuilder, @Nonnull UIEventBuilder eventBuilder, @Nonnull Store<EntityStore> store
   ) {
      commandBuilder.append("Pages/RespawnPage.ui");
      commandBuilder.set("#DeathReason.TextSpans", this.deathReason != null ? this.deathReason : Message.empty());
      if (!this.displayDataOnDeathScreen) {
         commandBuilder.set("#DeathData.Visible", false);
      } else {
         DecimalFormat decimalFormat = new DecimalFormat("#.##");
         decimalFormat.setDecimalSeparatorAlwaysShown(false);
         if (this.deathItemLoss.getLossMode() == DeathConfig.ItemsLossMode.NONE) {
            commandBuilder.set("#ItemsLossStatus.TextSpans", Message.translation("server.general.itemsLossPrevented"));
         } else {
            commandBuilder.set(
               "#ItemsLossStatus.TextSpans",
               Message.translation("server.general.itemsDurabilityLoss")
                  .param("percentage", decimalFormat.format(this.deathItemLoss.getDurabilityLossPercentage()))
            );
         }

         if (this.itemsLostOnDeath != null && this.itemsLostOnDeath.length != 0) {
            commandBuilder.set("#ItemsAmountLoss.TextSpans", Message.translation("server.general.itemsAmountLoss"));
            commandBuilder.set("#DroppedItemsContainer.Visible", true);
            commandBuilder.clear("#DroppedItemsContainer");

            for (int i = 0; i < this.itemsLostOnDeath.length; i++) {
               ItemStack itemStack = this.itemsLostOnDeath[i];
               String itemSelector = "#DroppedItemsContainer[" + i + "] ";
               commandBuilder.append("#DroppedItemsContainer", "Pages/DroppedItemSlot.ui");
               commandBuilder.set(itemSelector + "#ItemIcon.ItemId", itemStack.getItemId());
               commandBuilder.set(itemSelector + "#ItemIcon.Quantity", itemStack.getQuantity());
               if (itemStack.getQuantity() > 1) {
                  commandBuilder.set(itemSelector + "#QuantityLabel.Text", String.valueOf(itemStack.getQuantity()));
               } else {
                  commandBuilder.set(itemSelector + "#QuantityLabel.Visible", false);
               }
            }
         } else {
            commandBuilder.set("#ItemsAmountLoss.Visible", false);
            commandBuilder.set("#DroppedItemsContainer.Visible", false);
         }
      }

      eventBuilder.addEventBinding(CustomUIEventBindingType.Activating, "#RespawnButton", EventData.of("Action", "Respawn"));
   }

   public void handleDataEvent(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store, @Nonnull RespawnPage.RespawnPageEventData data) {
      if ("Respawn".equals(data.action)) {
         PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

         assert playerRef != null;

         UICommandBuilder commandBuilder = new UICommandBuilder();
         commandBuilder.set("#RespawnButton.Disabled", true);
         this.sendUpdate(commandBuilder);
         CompletableFutureUtil._catch(DeathComponent.respawn(store, ref).thenCompose(v -> {
            Ref<EntityStore> currentRef = playerRef.getReference();
            return currentRef != null && currentRef.isValid() ? CompletableFuture.runAsync(() -> {
               if (currentRef.isValid()) {
                  Player playerComponent = currentRef.getStore().getComponent(currentRef, Player.getComponentType());

                  assert playerComponent != null;

                  if (playerComponent.getPageManager().getCustomPage() == this) {
                     playerComponent.getPageManager().setPage(currentRef, currentRef.getStore(), Page.None);
                  }
               }
            }, currentRef.getStore().getExternalData().getWorld()) : CompletableFuture.completedFuture(null);
         }));
      }
   }

   @Override
   public void onDismiss(@Nonnull Ref<EntityStore> ref, @Nonnull Store<EntityStore> store) {
      super.onDismiss(ref, store);
      boolean isDead = store.getArchetype(ref).contains(DeathComponent.getComponentType());
      if (isDead) {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         DeathComponent.respawn(store, ref);
      }
   }

   public static class RespawnPageEventData {
      static final String KEY_ACTION = "Action";
      static final String ACTION_RESPAWN = "Respawn";
      public static final BuilderCodec<RespawnPage.RespawnPageEventData> CODEC = BuilderCodec.builder(
            RespawnPage.RespawnPageEventData.class, RespawnPage.RespawnPageEventData::new
         )
         .addField(new KeyedCodec<>("Action", Codec.STRING), (entry, s) -> entry.action = s, entry -> entry.action)
         .build();
      private String action;
   }
}
