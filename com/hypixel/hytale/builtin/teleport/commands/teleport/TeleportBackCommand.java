package com.hypixel.hytale.builtin.teleport.commands.teleport;

import com.hypixel.hytale.builtin.teleport.components.TeleportHistory;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class TeleportBackCommand extends AbstractPlayerCommand {
   @Nonnull
   private final OptionalArg<Integer> countArg = this.withOptionalArg("count", "server.commands.teleport.back.count.desc", ArgTypes.INTEGER);

   public TeleportBackCommand() {
      super("back", "server.commands.teleport.recent.desc");
      this.requirePermission(HytalePermissions.fromCommand("teleport.back"));
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      int counter = this.countArg.provided(context) ? this.countArg.get(context) : 1;
      TeleportHistory history = store.ensureAndGetComponent(ref, TeleportHistory.getComponentType());
      history.back(ref, counter);
   }
}
