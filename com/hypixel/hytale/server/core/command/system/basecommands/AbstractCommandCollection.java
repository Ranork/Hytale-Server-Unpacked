package com.hypixel.hytale.server.core.command.system.basecommands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public abstract class AbstractCommandCollection extends AbstractAsyncCommand {
   public AbstractCommandCollection(@Nonnull String name, @Nonnull String description) {
      super(name, description);
   }

   @Nonnull
   public Message getFullUsage(@Nonnull CommandSender sender) {
      return super.getUsageString(sender);
   }

   @Nonnull
   @Override
   protected final CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
      Message message = Message.translation("server.commands.help.usage")
         .insert(":")
         .insert("  (")
         .insert(Message.translation("server.commands.help.useHelpOnAnySubCommand"))
         .insert(")")
         .insert("\n")
         .insert(this.getUsageString(context.sender()));
      context.sender().sendMessage(message);
      return CompletableFuture.completedFuture(null);
   }

   @Nonnull
   @Override
   public Message getUsageString(@Nonnull CommandSender sender) {
      return this.getUsageShort(sender, false);
   }
}
