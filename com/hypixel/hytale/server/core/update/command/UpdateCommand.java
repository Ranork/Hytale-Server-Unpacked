package com.hypixel.hytale.server.core.update.command;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.ParseResult;
import com.hypixel.hytale.server.core.command.system.ParserContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.update.UpdateModule;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateCommand extends AbstractCommandCollection {
   private static final Message MSG_DISABLED = Message.translation("server.commands.update.disabled");

   public UpdateCommand() {
      super("update", "server.commands.update.desc");
      this.addSubCommand(new UpdateCheckCommand());
      this.addSubCommand(new UpdateDownloadCommand());
      this.addSubCommand(new UpdateApplyCommand());
      this.addSubCommand(new UpdateCancelCommand());
      this.addSubCommand(new UpdateStatusCommand());
      this.addSubCommand(new UpdatePatchlineCommand());
   }

   @Nullable
   @Override
   public CompletableFuture<Void> acceptCall(@Nonnull CommandSender sender, @Nonnull ParserContext parserContext, @Nonnull ParseResult parseResult) {
      if (UpdateModule.KILL_SWITCH_ENABLED) {
         sender.sendMessage(MSG_DISABLED);
         return CompletableFuture.completedFuture(null);
      } else {
         return super.acceptCall(sender, parserContext, parseResult);
      }
   }
}
