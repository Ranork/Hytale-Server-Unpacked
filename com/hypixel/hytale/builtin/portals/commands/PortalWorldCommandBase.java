package com.hypixel.hytale.builtin.portals.commands;

import com.hypixel.hytale.builtin.portals.resources.PortalWorld;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public abstract class PortalWorldCommandBase extends AbstractWorldCommand {
   @Nonnull
   private static final Message MESSAGE_SERVER_COMMANDS_PORTALS_NOT_IN_PORTAL = Message.translation("server.commands.portals.notInPortal");

   public PortalWorldCommandBase(@Nonnull String name, @Nonnull String description) {
      super(name, description);
   }

   @Override
   protected final void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      PortalWorld portalWorldResource = store.getResource(PortalWorld.getResourceType());
      if (!portalWorldResource.exists()) {
         context.sendMessage(MESSAGE_SERVER_COMMANDS_PORTALS_NOT_IN_PORTAL);
      } else {
         this.execute(context, world, portalWorldResource, store);
      }
   }

   protected abstract void execute(@Nonnull CommandContext var1, @Nonnull World var2, @Nonnull PortalWorld var3, @Nonnull Store<EntityStore> var4);
}
