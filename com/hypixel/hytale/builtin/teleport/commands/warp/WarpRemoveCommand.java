package com.hypixel.hytale.builtin.teleport.commands.warp;

import com.hypixel.hytale.builtin.teleport.TeleportPlugin;
import com.hypixel.hytale.builtin.teleport.Warp;
import com.hypixel.hytale.builtin.teleport.commands.event.RemoveWarpEvent;
import com.hypixel.hytale.event.IEventDispatcher;
import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import javax.annotation.Nonnull;

public class WarpRemoveCommand extends CommandBase {
   private static final Message MESSAGE_COMMANDS_TELEPORT_WARP_NOT_LOADED = Message.translation("server.commands.teleport.warp.notLoaded");
   @Nonnull
   private final RequiredArg<String> nameArg = this.withRequiredArg("name", "server.commands.warp.remove.name.desc", ArgTypes.STRING);
   @Nonnull
   private final FlagArg forceArg = this.withFlagArg("force", "server.commands.warp.remove.force.desc");

   public WarpRemoveCommand() {
      super("remove", "server.commands.warp.remove.desc");
      this.requirePermission(HytalePermissions.fromCommand("warp.remove"));
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      TeleportPlugin plugin = TeleportPlugin.get();
      if (!plugin.isWarpsLoaded()) {
         context.sendMessage(MESSAGE_COMMANDS_TELEPORT_WARP_NOT_LOADED);
      } else {
         String warpName = this.nameArg.get(context).toLowerCase();
         Warp warp = plugin.getWarps().get(warpName);
         if (warp == null) {
            context.sendMessage(Message.translation("server.commands.teleport.warp.unknownWarp").param("name", warpName));
         } else if (context.provided(this.forceArg) || !cancel(context, warp)) {
            TeleportPlugin.get().removeWarp(warpName);
            context.sendMessage(Message.translation("server.commands.teleport.warp.removedWarp").param("name", warpName));
         }
      }
   }

   private static boolean cancel(@Nonnull CommandContext context, @Nonnull Warp warp) {
      IEventDispatcher<RemoveWarpEvent, RemoveWarpEvent> dispatch = HytaleServer.get().getEventBus().dispatchFor(RemoveWarpEvent.class);
      if (!dispatch.hasListener()) {
         return false;
      } else {
         RemoveWarpEvent result = dispatch.dispatch(new RemoveWarpEvent(warp));
         if (!result.isCancelled()) {
            return false;
         } else {
            if (result.getCancelReason() != null) {
               context.sendMessage(result.getCancelReason());
            }

            context.sendMessage(WarpCommand.MESSAGE_COMMANDS_MODIFICATION_CANCELLED.param("name", warp.getId()));
            return true;
         }
      }
   }
}
