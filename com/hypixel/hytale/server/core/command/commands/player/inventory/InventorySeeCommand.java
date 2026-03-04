package com.hypixel.hytale.server.core.command.commands.player.inventory;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.packets.interface_.Page;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.windows.ContainerWindow;
import com.hypixel.hytale.server.core.inventory.container.CombinedItemContainer;
import com.hypixel.hytale.server.core.inventory.container.DelegateItemContainer;
import com.hypixel.hytale.server.core.inventory.container.ItemContainer;
import com.hypixel.hytale.server.core.inventory.container.filter.FilterType;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class InventorySeeCommand extends AbstractPlayerCommand {
   public static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
   @Nonnull
   private final RequiredArg<PlayerRef> targetPlayerArg = this.withRequiredArg("player", "server.commands.inventorysee.player.desc", ArgTypes.PLAYER_REF);

   public InventorySeeCommand() {
      super("see", "server.commands.inventorysee.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      PlayerRef targetPlayerRef = this.targetPlayerArg.get(context);
      Ref<EntityStore> targetRef = targetPlayerRef.getReference();
      if (targetRef != null && targetRef.isValid()) {
         Store<EntityStore> targetStore = targetRef.getStore();
         World targetWorld = targetStore.getExternalData().getWorld();
         targetWorld.execute(() -> {
            Player targetPlayerComponent = targetStore.getComponent(targetRef, Player.getComponentType());
            if (targetPlayerComponent == null) {
               context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
            } else {
               CombinedItemContainer targetInventory = targetPlayerComponent.getInventory().getCombinedHotbarFirst();
               ItemContainer targetItemContainer = targetInventory;
               if (!context.sender().hasPermission(HytalePermissions.fromCommand("invsee", "modify"))) {
                  DelegateItemContainer<CombinedItemContainer> delegateItemContainer = new DelegateItemContainer<>(targetInventory);
                  delegateItemContainer.setGlobalFilter(FilterType.DENY_ALL);
                  targetItemContainer = delegateItemContainer;
               }

               playerComponent.getPageManager().setPageWithWindows(ref, store, Page.Bench, true, new ContainerWindow(targetItemContainer));
            }
         });
      } else {
         context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
      }
   }
}
