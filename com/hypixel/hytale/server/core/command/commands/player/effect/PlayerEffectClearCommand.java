package com.hypixel.hytale.server.core.command.commands.player.effect;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class PlayerEffectClearCommand extends AbstractPlayerCommand {
   private static final Message MESSAGE_EFFECTS_CLEARED_SELF = Message.translation("server.commands.player.effect.clear.success.self");

   public PlayerEffectClearCommand() {
      super("clear", "server.commands.player.effect.clear.desc");
      this.requirePermission(HytalePermissions.fromCommand("player.effect.clear.self"));
      this.addUsageVariant(new PlayerEffectClearCommand.PlayerEffectClearOtherCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      EffectControllerComponent effectControllerComponent = store.getComponent(ref, EffectControllerComponent.getComponentType());

      assert effectControllerComponent != null;

      effectControllerComponent.clearEffects(ref, store);
      context.sendMessage(MESSAGE_EFFECTS_CLEARED_SELF);
   }

   private static class PlayerEffectClearOtherCommand extends CommandBase {
      private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
      @Nonnull
      private final RequiredArg<PlayerRef> playerArg = this.withRequiredArg("player", "server.commands.argtype.player.desc", ArgTypes.PLAYER_REF);

      PlayerEffectClearOtherCommand() {
         super("server.commands.player.effect.clear.other.desc");
         this.requirePermission(HytalePermissions.fromCommand("player.effect.clear.other"));
      }

      @Override
      protected void executeSync(@Nonnull CommandContext context) {
         PlayerRef targetPlayerRef = this.playerArg.get(context);
         Ref<EntityStore> ref = targetPlayerRef.getReference();
         if (ref != null && ref.isValid()) {
            Store<EntityStore> store = ref.getStore();
            World world = store.getExternalData().getWorld();
            world.execute(
               () -> {
                  Player playerComponent = store.getComponent(ref, Player.getComponentType());
                  if (playerComponent == null) {
                     context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
                  } else {
                     PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());

                     assert playerRefComponent != null;

                     EffectControllerComponent effectControllerComponent = store.getComponent(ref, EffectControllerComponent.getComponentType());

                     assert effectControllerComponent != null;

                     effectControllerComponent.clearEffects(ref, store);
                     context.sendMessage(
                        Message.translation("server.commands.player.effect.clear.success.other").param("username", playerRefComponent.getUsername())
                     );
                  }
               }
            );
         } else {
            context.sendMessage(MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD);
         }
      }
   }
}
