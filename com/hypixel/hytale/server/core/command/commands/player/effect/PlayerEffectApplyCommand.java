package com.hypixel.hytale.server.core.command.commands.player.effect;

import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.OverlapBehavior;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
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

public class PlayerEffectApplyCommand extends AbstractPlayerCommand {
   private static final float DEFAULT_DURATION = 100.0F;
   @Nonnull
   private final RequiredArg<EntityEffect> effectArg = this.withRequiredArg("effect", "server.commands.player.effect.apply.effect.desc", ArgTypes.EFFECT_ASSET);
   @Nonnull
   private final DefaultArg<Float> durationArg = this.withDefaultArg(
         "duration", "server.commands.player.effect.apply.duration.desc", ArgTypes.FLOAT, 100.0F, "server.commands.entity.effect.duration"
      )
      .addValidator(Validators.greaterThan(0.0F));

   public PlayerEffectApplyCommand() {
      super("apply", "server.commands.player.effect.apply.desc");
      this.requirePermission(HytalePermissions.fromCommand("player.effect.apply.self"));
      this.addUsageVariant(new PlayerEffectApplyCommand.PlayerEffectApplyOtherCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      EffectControllerComponent effectControllerComponent = store.getComponent(ref, EffectControllerComponent.getComponentType());

      assert effectControllerComponent != null;

      EntityEffect effect = this.effectArg.get(context);
      Float duration = this.durationArg.get(context);
      effectControllerComponent.addEffect(ref, effect, duration, OverlapBehavior.OVERWRITE, store);
      context.sendMessage(Message.translation("server.commands.player.effect.apply.success.self").param("effect", effect.getId()).param("duration", duration));
   }

   private static class PlayerEffectApplyOtherCommand extends CommandBase {
      private static final Message MESSAGE_COMMANDS_ERRORS_PLAYER_NOT_IN_WORLD = Message.translation("server.commands.errors.playerNotInWorld");
      @Nonnull
      private final RequiredArg<PlayerRef> playerArg = this.withRequiredArg("player", "server.commands.argtype.player.desc", ArgTypes.PLAYER_REF);
      @Nonnull
      private final RequiredArg<EntityEffect> effectArg = this.withRequiredArg(
         "effect", "server.commands.player.effect.apply.effect.desc", ArgTypes.EFFECT_ASSET
      );
      @Nonnull
      private final DefaultArg<Float> durationArg = this.withDefaultArg(
            "duration", "server.commands.player.effect.apply.duration.desc", ArgTypes.FLOAT, 100.0F, "server.commands.entity.effect.duration"
         )
         .addValidator(Validators.greaterThan(0.0F));

      PlayerEffectApplyOtherCommand() {
         super("server.commands.player.effect.apply.other.desc");
         this.requirePermission(HytalePermissions.fromCommand("player.effect.apply.other"));
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

                     EntityEffect effect = this.effectArg.get(context);
                     Float duration = this.durationArg.get(context);
                     effectControllerComponent.addEffect(ref, effect, duration, OverlapBehavior.OVERWRITE, store);
                     context.sendMessage(
                        Message.translation("server.commands.player.effect.apply.success.other")
                           .param("username", playerRefComponent.getUsername())
                           .param("effect", effect.getId())
                           .param("duration", duration)
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
