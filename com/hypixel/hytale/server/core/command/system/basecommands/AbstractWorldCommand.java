package com.hypixel.hytale.server.core.command.system.basecommands;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public abstract class AbstractWorldCommand extends AbstractAsyncCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERRORS_NO_WORLD = Message.translation("server.commands.errors.noWorld");
   @Nonnull
   private final OptionalArg<World> worldArg = this.withOptionalArg("world", "server.commands.worldthread.arg.desc", ArgTypes.WORLD);

   public AbstractWorldCommand(@Nonnull String name, @Nonnull String description) {
      super(name, description);
   }

   public AbstractWorldCommand(@Nonnull String name, @Nonnull String description, boolean requiresConfirmation) {
      super(name, description, requiresConfirmation);
   }

   public AbstractWorldCommand(@Nonnull String description) {
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
         Store<EntityStore> store = world.getEntityStore().getStore();
         return this.runAsync(context, () -> this.execute(context, world, store), world);
      }
   }

   protected abstract void execute(@Nonnull CommandContext var1, @Nonnull World var2, @Nonnull Store<EntityStore> var3);
}
