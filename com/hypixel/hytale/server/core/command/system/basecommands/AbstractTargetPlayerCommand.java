package com.hypixel.hytale.server.core.command.system.basecommands;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandUtil;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractTargetPlayerCommand extends AbstractAsyncCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
   @Nonnull
   private final OptionalArg<PlayerRef> playerArg = this.withOptionalArg("player", "server.commands.argtype.player.desc", ArgTypes.PLAYER_REF);

   public AbstractTargetPlayerCommand(@Nonnull String name, @Nonnull String description) {
      super(name, description);
   }

   public AbstractTargetPlayerCommand(@Nonnull String name, @Nonnull String description, boolean requiresConfirmation) {
      super(name, description, requiresConfirmation);
   }

   public AbstractTargetPlayerCommand(@Nonnull String description) {
      super(description);
   }

   @Nonnull
   @Override
   protected final CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
      Ref<EntityStore> sourceRef;
      if (context.isPlayer()) {
         sourceRef = context.senderAsPlayerRef();
      } else {
         sourceRef = null;
      }

      Ref<EntityStore> targetRef;
      if (this.playerArg.provided(context)) {
         targetRef = this.playerArg.get(context).getReference();
         CommandUtil.requirePermission(context.sender(), HytalePermissions.fromCommand(this.getPermission() + ".other"));
      } else {
         if (!context.isPlayer()) {
            context.sendMessage(Message.translation("server.commands.errors.playerOrArg").param("option", "player"));
            return CompletableFuture.completedFuture(null);
         }

         targetRef = context.senderAsPlayerRef();
      }

      if (targetRef != null && targetRef.isValid()) {
         Store<EntityStore> targetStore = targetRef.getStore();
         World targetWorld = targetStore.getExternalData().getWorld();
         return this.runAsync(context, () -> {
            PlayerRef playerRefComponent = targetStore.getComponent(targetRef, PlayerRef.getComponentType());

            assert playerRefComponent != null;

            this.execute(context, sourceRef, targetRef, playerRefComponent, targetWorld, targetStore);
         }, targetWorld);
      } else {
         context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
         return CompletableFuture.completedFuture(null);
      }
   }

   protected abstract void execute(
      @Nonnull CommandContext var1,
      @Nullable Ref<EntityStore> var2,
      @Nonnull Ref<EntityStore> var3,
      @Nonnull PlayerRef var4,
      @Nonnull World var5,
      @Nonnull Store<EntityStore> var6
   );
}
