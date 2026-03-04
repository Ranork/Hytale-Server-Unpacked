package com.hypixel.hytale.server.core.command.system.basecommands;

import com.hypixel.hytale.server.core.command.system.AbstractCommand;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class CommandBase extends AbstractCommand {
   public CommandBase(@Nonnull String name, @Nonnull String description) {
      super(name, description);
   }

   public CommandBase(@Nonnull String name, @Nonnull String description, boolean requiresConfirmation) {
      super(name, description, requiresConfirmation);
   }

   public CommandBase(@Nonnull String description) {
      super(description);
   }

   @Nullable
   @Override
   protected final CompletableFuture<Void> execute(@Nonnull CommandContext context) {
      this.executeSync(context);
      return null;
   }

   protected abstract void executeSync(@Nonnull CommandContext var1);
}
