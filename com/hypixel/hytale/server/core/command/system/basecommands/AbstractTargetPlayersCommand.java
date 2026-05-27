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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class AbstractTargetPlayersCommand extends AbstractAsyncCommand {
   @Nonnull
   private final OptionalArg<PlayerRef> playerArg = this.withOptionalArg("player", "server.commands.argtype.player.desc", ArgTypes.PLAYER_REF);
   @Nonnull
   private final OptionalArg<Boolean> allArg = this.withOptionalArg("all", "server.commands.argtype.all.desc", ArgTypes.BOOLEAN);

   public AbstractTargetPlayersCommand(@Nonnull String name, @Nonnull String description) {
      super(name, description);
   }

   public AbstractTargetPlayersCommand(@Nonnull String name, @Nonnull String description, boolean requiresConfirmation) {
      super(name, description, requiresConfirmation);
   }

   public AbstractTargetPlayersCommand(@Nonnull String description) {
      super(description);
   }

   @Nonnull
   @Override
   protected final CompletableFuture<Void> executeAsync(@Nonnull CommandContext context) {
      boolean targetAll = this.allArg.provided(context) && Boolean.TRUE.equals(this.allArg.get(context));
      boolean hasPlayerArg = this.playerArg.provided(context);
      if (targetAll && hasPlayerArg) {
         context.sendMessage(Message.translation("server.commands.errors.targetConflict"));
         return CompletableFuture.completedFuture(null);
      } else if (targetAll) {
         CommandUtil.requirePermission(context.sender(), HytalePermissions.fromCommand(this.getPermission() + ".all"));
         return this.runOnWorld(context);
      } else if (hasPlayerArg) {
         CommandUtil.requirePermission(context.sender(), HytalePermissions.fromCommand(this.getPermission() + ".other"));
         return this.runOnRef(context, this.playerArg.get(context).getReference());
      } else if (!context.isPlayer()) {
         context.sendMessage(Message.translation("server.commands.errors.playerOrArg").param("option", "player"));
         return CompletableFuture.completedFuture(null);
      } else {
         return this.runOnRef(context, context.senderAsPlayerRef());
      }
   }

   @Nonnull
   private CompletableFuture<Void> runOnRef(@Nonnull CommandContext context, @Nullable Ref<EntityStore> targetRef) {
      if (targetRef != null && targetRef.isValid()) {
         Store<EntityStore> store = targetRef.getStore();
         World world = store.getExternalData().getWorld();
         return this.runAsync(context, () -> this.execute(context, world, store, List.of(targetRef)), world);
      } else {
         context.sendMessage(Message.translation("server.commands.errors.playerNotInWorld"));
         return CompletableFuture.completedFuture(null);
      }
   }

   @Nonnull
   private CompletableFuture<Void> runOnWorld(@Nonnull CommandContext context) {
      Ref<EntityStore> senderRef = context.isPlayer() ? context.senderAsPlayerRef() : null;
      if (senderRef == null) {
         context.sendMessage(Message.translation("server.commands.errors.noWorld"));
         return CompletableFuture.completedFuture(null);
      } else {
         Store<EntityStore> store = senderRef.getStore();
         World world = store.getExternalData().getWorld();
         return this.runAsync(context, () -> {
            ArrayList<Ref<EntityStore>> targets = new ArrayList<>();

            for (PlayerRef playerRef : world.getPlayerRefs()) {
               Ref<EntityStore> ref = playerRef.getReference();
               if (ref != null && ref.isValid()) {
                  targets.add(ref);
               }
            }

            this.execute(context, world, store, targets);
         }, world);
      }
   }

   protected abstract void execute(@Nonnull CommandContext var1, @Nonnull World var2, @Nonnull Store<EntityStore> var3, @Nonnull List<Ref<EntityStore>> var4);
}
