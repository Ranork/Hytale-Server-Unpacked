package com.hypixel.hytale.builtin.teleport.commands.warp;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class WarpGoCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<String> warpNameArg = this.withRequiredArg("warpName", "server.commands.warp.warpName.desc", ArgTypes.STRING);

   public WarpGoCommand() {
      super("go", "server.commands.warp.go.desc");
      this.requirePermission(HytalePermissions.fromCommand("warp.go"));
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      String warpName = this.warpNameArg.get(context);
      WarpCommand.tryGo(context, warpName, ref, store);
   }
}
