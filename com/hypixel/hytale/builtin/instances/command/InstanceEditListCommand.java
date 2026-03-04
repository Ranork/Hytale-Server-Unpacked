package com.hypixel.hytale.builtin.instances.command;

import com.hypixel.hytale.builtin.instances.InstancesPlugin;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.util.message.MessageFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class InstanceEditListCommand extends AbstractAsyncCommand {
   public InstanceEditListCommand() {
      super("list", "server.commands.instances.edit.list.desc");
   }

   @Nonnull
   @Override
   public CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
      List<String> instanceAssets = InstancesPlugin.get().getInstanceAssets();
      context.sendMessage(
         MessageFormat.list(Message.translation("server.commands.instances.edit.list.header"), instanceAssets.stream().map(Message::raw).toList())
      );
      return CompletableFuture.completedFuture(null);
   }
}
