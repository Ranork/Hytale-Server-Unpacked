package com.hypixel.hytale.server.core.command.system.basecommands;

import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public abstract class AbstractAsyncWorldCommand extends AbstractAsyncCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERRORS_NO_WORLD = Message.translation("server.commands.errors.noWorld");
   @Nonnull
   private final OptionalArg<World> worldArg = this.withOptionalArg("world", "server.commands.worldthread.arg.desc", ArgTypes.WORLD);

   public AbstractAsyncWorldCommand(@Nonnull String name, @Nonnull String description) {
      super(name, description);
   }

   public AbstractAsyncWorldCommand(@Nonnull String name, @Nonnull String description, boolean requiresConfirmation) {
      super(name, description, requiresConfirmation);
   }

   public AbstractAsyncWorldCommand(@Nonnull String description) {
      super(description);
   }

   @Nonnull
   @Override
   protected final CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
      World world = this.worldArg.getProcessed(context);
      if (world == null) {
         context.sendMessage(MESSAGE_COMMANDS_ERRORS_NO_WORLD);
         return CompletableFuture.completedFuture(null);
      } else {
         return this.executeAsync(context, world);
      }
   }

   @Nonnull
   protected abstract CompletableFuture<Void> executeAsync(@Nonnull CommandContext var1, @Nonnull World var2);
}
