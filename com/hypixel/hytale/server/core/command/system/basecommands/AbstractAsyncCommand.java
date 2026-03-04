package com.hypixel.hytale.server.core.command.system.basecommands;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import java.awt.Color;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public abstract class AbstractAsyncCommand extends AbstractCommand {
   @Nonnull
   private static final Message MESSAGE_MODULES_COMMAND_RUNTIME_ERROR = Message.translation("server.modules.command.runtimeError").color(Color.RED);

   public AbstractAsyncCommand(@Nonnull String name, @Nonnull String description) {
      super(name, description);
   }

   public AbstractAsyncCommand(@Nonnull String name, @Nonnull String description, boolean requiresConfirmation) {
      super(name, description, requiresConfirmation);
   }

   public AbstractAsyncCommand(@Nonnull String description) {
      super(description);
   }

   @Override
   protected final CompletableFuture<Void> execute(@Nonnull CommandContext context) {
      return this.executeAsync(context);
   }

   @Nonnull
   protected abstract CompletableFuture<Void> executeAsync(@Nonnull CommandContext var1);

   @Nonnull
   public CompletableFuture<Void> runAsync(@Nonnull CommandContext context, @Nonnull Runnable runnable, @Nonnull Executor executor) {
      return CompletableFuture.runAsync(() -> {
         try {
            runnable.run();
         } catch (Exception var3x) {
            context.sendMessage(MESSAGE_MODULES_COMMAND_RUNTIME_ERROR);
            ((HytaleLogger.Api)AbstractCommand.LOGGER.at(Level.SEVERE).withCause(var3x)).log("Exception while running that command:");
         }
      }, executor);
   }
}
