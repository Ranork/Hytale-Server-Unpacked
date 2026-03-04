package com.hypixel.hytale.builtin.portals.commands.player;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.builtin.portals.resources.PortalWorld;
import com.hypixel.hytale.builtin.portals.utils.CursedItems;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class LeaveCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_LEAVE_NOT_IN_PORTAL = Message.translation("server.commands.leave.notInPortal");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_LEAVE_UNCURSED_TEMP = Message.translation("server.commands.leave.uncursedTemp").color("#d955ef");

   public LeaveCommand() {
      super("leave", "server.commands.leave.desc");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());
      if (playerComponent != null) {
         PortalWorld portalWorldResource = store.getResource(PortalWorld.getResourceType());
         if (!portalWorldResource.exists()) {
            playerRef.sendMessage(MESSAGE_COMMANDS_LEAVE_NOT_IN_PORTAL);
         } else {
            boolean uncursedAny = CursedItems.uncurseAll(playerComponent.getInventory().getCombinedEverything());
            if (uncursedAny) {
               playerRef.sendMessage(MESSAGE_COMMANDS_LEAVE_UNCURSED_TEMP);
            }

            InstancesPlugin.exitInstance(ref, store);
         }
      }
   }
}
