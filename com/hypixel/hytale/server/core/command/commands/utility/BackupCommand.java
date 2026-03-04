package com.hypixel.hytale.server.core.command.commands.utility;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.universe.Universe;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class BackupCommand extends AbstractAsyncCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERRORS_WAIT_FOR_BOOT = Message.translation("server.commands.errors.waitForBoot");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_BACKUP_NOT_CONFIGURED = Message.translation("server.commands.backup.notConfigured");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_BACKUP_STARTING = Message.translation("server.commands.backup.starting");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_BACKUP_COMPLETE = Message.translation("server.commands.backup.complete");

   public BackupCommand() {
      super("backup", "server.commands.backup.desc");
   }

   @Nonnull
   @Override
   protected CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
      if (!HytaleServer.get().isBooted()) {
         context.sendMessage(MESSAGE_COMMANDS_ERRORS_WAIT_FOR_BOOT);
         return CompletableFuture.completedFuture(null);
      } else if (HytaleServer.get().getConfig().getBackupConfig().getDirectory() == null) {
         context.sendMessage(MESSAGE_COMMANDS_BACKUP_NOT_CONFIGURED);
         return CompletableFuture.completedFuture(null);
      } else {
         context.sendMessage(MESSAGE_COMMANDS_BACKUP_STARTING);
         return Universe.get().runBackup().thenAccept(aVoid -> context.sendMessage(MESSAGE_COMMANDS_BACKUP_COMPLETE));
      }
   }
}
