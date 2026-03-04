package com.hypixel.hytale.builtin.portals.commands.utils;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.item.config.metadata.AdventureMetadata;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
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
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         Inventory inventory = playerComponent.getInventory();
         if (!inventory.usingToolsItem()) {
            ItemStack inHandItemStack = inventory.getActiveHotbarItem();
            if (inHandItemStack != null && !inHandItemStack.isEmpty()) {
               AdventureMetadata adventureMeta = inHandItemStack.getFromMetadataOrDefault("Adventure", AdventureMetadata.CODEC);
               adventureMeta.setCursed(!adventureMeta.isCursed());
               ItemStack edited = inHandItemStack.withMetadata(AdventureMetadata.KEYED_CODEC, adventureMeta);
               inventory.getHotbar().replaceItemStackInSlot(inventory.getActiveHotbarSlot(), inHandItemStack, edited);
               playerRef.sendMessage(Message.translation("server.commands.cursethis.done").param("state", adventureMeta.isCursed()));
            } else {
               playerRef.sendMessage(MESSAGE_COMMANDS_CURSE_THIS_NOT_HOLDING_ITEM);
            }
         }
      }
   }
}
