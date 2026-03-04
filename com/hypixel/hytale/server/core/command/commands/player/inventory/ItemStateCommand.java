package com.hypixel.hytale.server.core.command.commands.player.inventory;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.inventory.Inventory;
import com.hypixel.hytale.server.core.inventory.ItemStack;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ItemStateCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ITEMSTATE_NO_ITEM = Message.translation("server.commands.itemstate.noItem");
   @Nonnull
   private final RequiredArg<String> stateArg = this.withRequiredArg("state", "server.commands.itemstate.state.desc", ArgTypes.STRING);

   public ItemStateCommand() {
      super("itemstate", "server.commands.itemstate.desc");
      this.setPermissionGroup(GameMode.Creative);
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      Inventory inventory = playerComponent.getInventory();
      byte activeHotbarSlot = inventory.getActiveHotbarSlot();
      if (activeHotbarSlot == -1) {
         context.sendMessage(MESSAGE_COMMANDS_ITEMSTATE_NO_ITEM);
      } else {
         ItemContainer hotbar = inventory.getHotbar();
         ItemStack item = hotbar.getItemStack(activeHotbarSlot);
         if (item == null) {
            context.sendMessage(MESSAGE_COMMANDS_ITEMSTATE_NO_ITEM);
         } else {
            String state = this.stateArg.get(context);
            hotbar.setItemStackForSlot(activeHotbarSlot, item.withState(state));
         }
      }
   }
}
