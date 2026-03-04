package com.hypixel.hytale.server.core.command.commands.player.inventory;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerWindow;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemStackItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class InventoryItemCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_INVENTORY_ITEM_NO_ITEM_IN_HAND = Message.translation("server.commands.inventory.item.noItemInHand");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_INVENTORY_ITEM_NO_CONTAINER_ON_ITEM = Message.translation("server.commands.inventory.item.noContainerOnItem");

   public InventoryItemCommand() {
      super("item", "server.commands.inventoryitem.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      Inventory inventory = playerComponent.getInventory();
      ItemContainer hotbar = inventory.getHotbar();
      byte activeHotbarSlot = inventory.getActiveHotbarSlot();
      ItemStack activeHotbarItem = inventory.getActiveHotbarItem();
      if (ItemStack.isEmpty(activeHotbarItem)) {
         context.sendMessage(MESSAGE_COMMANDS_INVENTORY_ITEM_NO_ITEM_IN_HAND);
      } else {
         ItemStackItemContainer backpackInventory = ItemStackItemContainer.getContainer(hotbar, activeHotbarSlot);
         if (backpackInventory != null && backpackInventory.getCapacity() != 0) {
            playerComponent.getPageManager().setPageWithWindows(ref, store, Page.Bench, true, new ContainerWindow(backpackInventory));
         } else {
            context.sendMessage(MESSAGE_COMMANDS_INVENTORY_ITEM_NO_CONTAINER_ON_ITEM);
         }
      }
   }
}
