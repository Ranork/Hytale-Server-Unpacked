package com.hypixel.hytale.builtin.adventure.objectives.completion;

import com.hypixel.hytale.builtin.adventure.objectives.Objective;
import com.hypixel.hytale.builtin.adventure.objectives.config.completion.GiveItemsCompletionAsset;
import com.hypixel.hytale.builtin.adventure.objectives.historydata.ItemObjectiveRewardHistoryData;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.SimpleItemContainer;
import com.hypixel.hytale.server.core.modules.item.ItemModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.NotificationUtil;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nonnull;

public class GiveItemsCompletion extends ObjectiveCompletion {
   public GiveItemsCompletion(@Nonnull GiveItemsCompletionAsset asset) {
      super(asset);
   }

   @Nonnull
   public GiveItemsCompletionAsset getAsset() {
      return (GiveItemsCompletionAsset)super.getAsset();
   }

   @Override
   public void handle(@Nonnull Objective objective, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      World world = Universe.get().getWorld(objective.getWorldUUID());
      if (world != null) {
         Store<EntityStore> store = world.getEntityStore().getStore();
         boolean showItemNotification = world.getGameplayConfig().getShowItemPickupNotifications();
         objective.forEachParticipant(
            (participantReference, asset, objectiveHistoryData) -> {
               CombinedItemContainer hotbarFirstCombinedItemContainer = InventoryComponent.getCombined(
                  componentAccessor, participantReference, InventoryComponent.HOTBAR_FIRST
               );
               List<ItemStack> itemStacks = ItemModule.get().getRandomItemDrops(asset.getDropListId());
               SimpleItemContainer.addOrDropItemStacks(store, participantReference, hotbarFirstCombinedItemContainer, itemStacks);
               Player playerComponent = componentAccessor.getComponent(participantReference, Player.getComponentType());
               if (playerComponent != null) {
                  PlayerRef playerRefComponent = componentAccessor.getComponent(participantReference, PlayerRef.getComponentType());

                  assert playerRefComponent != null;

                  UUIDComponent uuidComponent = store.getComponent(participantReference, UUIDComponent.getComponentType());

                  assert uuidComponent != null;

                  UUID uuid = uuidComponent.getUuid();

                  for (ItemStack itemStack : itemStacks) {
                     objectiveHistoryData.addRewardForPlayerUUID(uuid, new ItemObjectiveRewardHistoryData(itemStack.getItemId(), itemStack.getQuantity()));
                     if (showItemNotification) {
                        Message itemNameMessage = Message.translation(itemStack.getItem().getTranslationKey());
                        NotificationUtil.sendNotification(
                           playerRefComponent.getPacketHandler(),
                           Message.translation("server.objectives.itemObjectiveCompletion").param("item", itemNameMessage),
                           null,
                           itemStack.toPacket()
                        );
                     }
                  }
               }
            },
            this.getAsset(),
            objective.getObjectiveHistoryData()
         );
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "GiveItemsCompletion{} " + super.toString();
   }
}
