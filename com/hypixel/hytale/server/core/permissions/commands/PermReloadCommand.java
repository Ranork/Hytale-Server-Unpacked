package com.hypixel.hytale.server.core.permissions.commands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import javax.annotation.Nonnull;

public class PermReloadCommand extends CommandBase {
   private static final Message MESSAGE_RELOADED = Message.translation("server.commands.perm.reload.success");

   public PermReloadCommand() {
      super("reload", "server.commands.perm.reload.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      PermissionsModule.get().reload();
      context.sendMessage(MESSAGE_RELOADED);
   }
}
