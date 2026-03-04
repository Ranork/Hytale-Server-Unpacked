package com.hypixel.hytale.server.core.update.command;

import com.hypixel.hytale.server.core.HytaleServer;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.config.UpdateConfig;
import com.hypixel.hytale.server.core.update.UpdateService;
import javax.annotation.Nonnull;

public class UpdatePatchlineCommand extends CommandBase {
   public UpdatePatchlineCommand() {
      super("patchline", "server.commands.update.patchline.desc");
      this.addUsageVariant(new UpdatePatchlineCommand.SetPatchlineVariant());
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      String current = UpdateService.getEffectivePatchline();
      context.sendMessage(Message.translation("server.commands.update.patchline.current").param("patchline", current));
   }

   private static class SetPatchlineVariant extends CommandBase {
      @Nonnull
      private final RequiredArg<String> patchlineArg = this.withRequiredArg("patchline", "server.commands.update.patchline.arg.desc", ArgTypes.STRING);

      SetPatchlineVariant() {
         super("server.commands.update.patchline.set.desc");
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         String newPatchline = this.patchlineArg.get(context);
         UpdateConfig config = HytaleServer.get().getConfig().getUpdateConfig();
         config.setPatchline(newPatchline);
         context.sendMessage(Message.translation("server.commands.update.patchline.changed").param("patchline", newPatchline));
      }
   }
}
