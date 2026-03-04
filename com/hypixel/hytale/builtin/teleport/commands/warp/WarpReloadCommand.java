package com.hypixel.hytale.builtin.teleport.commands.warp;

import com.hypixel.hytale.builtin.teleport.TeleportPlugin;
import com.hypixel.hytale.builtin.teleport.Warp;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import java.util.Map;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class WarpReloadCommand extends CommandBase {
   private static final HytaleLogger logger = HytaleLogger.forEnclosingClass();
   private static final Message MESSAGE_COMMANDS_TELEPORT_WARP_NOT_LOADED = Message.translation("server.commands.teleport.warp.notLoaded");
   private static final Message MESSAGE_COMMANDS_TELEPORT_WARP_FAILED_TO_RELOAD = Message.translation("server.commands.teleport.warp.failedToReload");

   public WarpReloadCommand() {
      super("reload", "server.commands.warp.reload.desc");
      this.requirePermission(HytalePermissions.fromCommand("warp.reload"));
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      if (!TeleportPlugin.get().isWarpsLoaded()) {
         context.sendMessage(MESSAGE_COMMANDS_TELEPORT_WARP_NOT_LOADED);
      } else {
         Map<String, Warp> warps = TeleportPlugin.get().getWarps();

         try {
            TeleportPlugin.get().loadWarps();
            context.sendMessage(Message.translation("server.commands.teleport.warp.reloaded").param("count", warps.size()));
         } catch (Throwable var4) {
            context.sendMessage(MESSAGE_COMMANDS_TELEPORT_WARP_FAILED_TO_RELOAD);
            ((HytaleLogger.Api)logger.at(Level.SEVERE).withCause(var4)).log("Failed to reload warps:");
         }
      }
   }
}
