package com.hypixel.hytale.server.core.command.system.basecommands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public abstract class AbstractPlayerCommand extends AbstractAsyncCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_OR_ARG = Message.translation("server.commands.errors.playerOrArg").param("option", "player");

   public AbstractPlayerCommand(@Nonnull String name, @Nonnull String description) {
      super(name, description);
   }

   public AbstractPlayerCommand(@Nonnull String name, @Nonnull String description, boolean requiresConfirmation) {
      super(name, description, requiresConfirmation);
   }

   public AbstractPlayerCommand(@Nonnull String description) {
      super(description);
   }

   @Nonnull
   @Override
   protected final CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
      Ref<EntityStore> ref = context.senderAsPlayerRef();
      if (ref == null) {
         context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_OR_ARG);
         return CompletableFuture.completedFuture(null);
      } else if (!ref.isValid()) {
         context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
         return CompletableFuture.completedFuture(null);
      } else {
         Store<EntityStore> store = ref.getStore();
         World world = store.getExternalData().getWorld();
         return this.runAsync(context, () -> {
            PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());
            if (playerRefComponent == null) {
               context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
            } else {
               this.execute(context, store, ref, playerRefComponent, world);
            }
         }, world);
      }
   }

   protected abstract void execute(
      @Nonnull CommandContext var1, @Nonnull Store<EntityStore> var2, @Nonnull Ref<EntityStore> var3, @Nonnull PlayerRef var4, @Nonnull World var5
   );
}
