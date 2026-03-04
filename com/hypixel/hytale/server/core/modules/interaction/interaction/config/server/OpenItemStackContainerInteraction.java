package com.hypixel.hytale.server.core.modules.interaction.interaction.config.server;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.InteractionType;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemStackContainerConfig;
import com.hypixel.hytale.server.core.entity.InteractionContext;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.pages.PageManager;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ItemStackContainerWindow;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemStackItemContainer;
import com.hypixel.hytale.server.core.modules.interaction.interaction.CooldownHandler;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.SimpleInstantInteraction;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class OpenItemStackContainerInteraction extends SimpleInstantInteraction {
   @Nonnull
   public static final BuilderCodec<OpenItemStackContainerInteraction> CODEC = BuilderCodec.builder(
         OpenItemStackContainerInteraction.class, OpenItemStackContainerInteraction::new, SimpleInstantInteraction.CODEC
      )
      .documentation("Opens a container contained within the current held item.")
      .build();

   @Override
   protected void firstRun(@Nonnull InteractionType type, @Nonnull InteractionContext context, @Nonnull CooldownHandler cooldownHandler) {
      CommandBuffer<EntityStore> commandBuffer = context.getCommandBuffer();
      Ref<EntityStore> ref = context.getEntity();
      Store<EntityStore> store = ref.getStore();
      Player playerComponent = commandBuffer.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         PageManager pageManager = playerComponent.getPageManager();
         if (pageManager.getCustomPage() == null) {
            ItemStack heldItem = context.getHeldItem();
            if (!ItemStack.isEmpty(heldItem)) {
               byte heldItemSlot = context.getHeldItemSlot();
               ItemContainer itemContainer = playerComponent.getInventory().getSectionById(context.getHeldItemSectionId());
               if (itemContainer != null) {
                  ItemStack itemStack = itemContainer.getItemStack(heldItemSlot);
                  ItemStackContainerConfig config = itemStack.getItem().getItemStackContainerConfig();
                  ItemStackItemContainer itemStackItemContainer = ItemStackItemContainer.ensureConfiguredContainer(itemContainer, heldItemSlot, config);
                  if (itemStackItemContainer != null) {
                     pageManager.setPageWithWindows(ref, store, Page.Bench, true, new ItemStackContainerWindow(itemStackItemContainer));
                  }
               }
            }
         }
      }
   }
}
