package com.hypixel.hytale.builtin.portals.commands.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.metadata.AdventureMetadata;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.InventoryComponent;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class CursedHeldItemCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_CURSE_THIS_NOT_HOLDING_ITEM = Message.translation("server.commands.cursethis.notHoldingItem");

   public CursedHeldItemCommand() {
      super("cursethis", "server.commands.cursethis.desc");
      this.setPermissionGroups("hytale:Builder");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         InventoryComponent.Tool toolComponent = store.getComponent(ref, InventoryComponent.Tool.getComponentType());
         if (toolComponent == null || !toolComponent.isUsingToolsItem()) {
            ItemStack itemInHand = InventoryComponent.getItemInHand(store, ref);
            if (itemInHand != null && !itemInHand.isEmpty()) {
               AdventureMetadata adventureMeta = itemInHand.getFromMetadataOrDefault("Adventure", AdventureMetadata.CODEC);
               adventureMeta.setCursed(!adventureMeta.isCursed());
               ItemStack edited = itemInHand.withMetadata(AdventureMetadata.KEYED_CODEC, adventureMeta);
               InventoryComponent.Hotbar hotbarComponent = store.getComponent(ref, InventoryComponent.Hotbar.getComponentType());
               if (hotbarComponent != null) {
                  byte activeSlot = hotbarComponent.getActiveSlot();
                  hotbarComponent.getInventory().replaceItemStackInSlot(activeSlot, itemInHand, edited);
                  playerRef.sendMessage(Message.translation("server.commands.cursethis.done").param("state", adventureMeta.isCursed()));
               }
            } else {
               playerRef.sendMessage(MESSAGE_COMMANDS_CURSE_THIS_NOT_HOLDING_ITEM);
            }
         }
      }
   }
}
