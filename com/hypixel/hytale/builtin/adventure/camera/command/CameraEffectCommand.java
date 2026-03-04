package com.hypixel.hytale.builtin.adventure.camera.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.camera.CameraEffect;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgumentType;
import com.hypixel.hytale.server.core.command.system.arguments.types.AssetArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractTargetPlayerCommand;
import com.hypixel.hytale.server.core.modules.entity.damage.Damage;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageSystems;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class CameraEffectCommand extends AbstractCommandCollection {
   @Nonnull
   protected static final ArgumentType<CameraEffect> CAMERA_EFFECT_ARGUMENT_TYPE = new AssetArgumentType("CameraEffect", CameraEffect.class, "");

   public CameraEffectCommand() {
      super("camshake", "server.commands.camshake.desc");
      this.addSubCommand(new CameraEffectCommand.DamageCommand());
      this.addSubCommand(new CameraEffectCommand.DebugCommand());
   }

   protected static class DamageCommand extends AbstractTargetPlayerCommand {
      @Nonnull
      protected static final ArgumentType<DamageCause> DAMAGE_CAUSE_ARGUMENT_TYPE = new AssetArgumentType("DamageCause", DamageCause.class, "");
      @Nonnull
      protected final OptionalArg<CameraEffect> effectArg = this.withOptionalArg(
         "effect", "server.commands.camshake.effect.desc", CameraEffectCommand.CAMERA_EFFECT_ARGUMENT_TYPE
      );
      @Nonnull
      protected final RequiredArg<DamageCause> causeArg = this.withRequiredArg(
         "cause", "server.commands.camshake.damage.cause.desc", DAMAGE_CAUSE_ARGUMENT_TYPE
      );
      @Nonnull
      protected final RequiredArg<Float> damageArg = this.withRequiredArg("amount", "server.commands.camshake.damage.amount.desc", ArgTypes.FLOAT);

      public DamageCommand() {
         super("damage", "server.commands.camshake.damage.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context,
         @Nullable Ref<EntityStore> sourceRef,
         @Nonnull Ref<EntityStore> ref,
         @Nonnull PlayerRef playerRef,
         @Nonnull World world,
         @Nonnull Store<EntityStore> store
      ) {
         DamageCause damageCause = context.get(this.causeArg);
         float damageAmount = context.get(this.damageArg);
         Damage.CommandSource damageSource = new Damage.CommandSource(context.sender(), this.getName());
         Damage damageEvent = new Damage(damageSource, damageCause, damageAmount);
         String cameraEffectId = "Default";
         if (this.effectArg.provided(context)) {
            cameraEffectId = context.get(this.effectArg).getId();
            Damage.CameraEffect damageEffect = new Damage.CameraEffect(CameraEffect.getAssetMap().getIndex(cameraEffectId));
            damageEvent.getMetaStore().putMetaObject(Damage.CAMERA_EFFECT, damageEffect);
         }

         DamageSystems.executeDamage(ref, store, damageEvent);
         context.sendMessage(
            Message.translation("server.commands.camshake.damage.success")
               .param("effect", cameraEffectId)
               .param("cause", damageCause.getId())
               .param("amount", damageAmount)
         );
      }
   }

   protected static class DebugCommand extends AbstractTargetPlayerCommand {
      @Nonnull
      protected final RequiredArg<CameraEffect> effectArg = this.withRequiredArg(
         "effect", "server.commands.camshake.effect.desc", CameraEffectCommand.CAMERA_EFFECT_ARGUMENT_TYPE
      );
      @Nonnull
      protected final RequiredArg<Float> intensityArg = this.withRequiredArg("intensity", "server.commands.camshake.debug.intensity.desc", ArgTypes.FLOAT);

      public DebugCommand() {
         super("debug", "server.commands.camshake.debug.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context,
         @Nullable Ref<EntityStore> sourceRef,
         @Nonnull Ref<EntityStore> ref,
         @Nonnull PlayerRef playerRef,
         @Nonnull World world,
         @Nonnull Store<EntityStore> store
      ) {
         CameraEffect cameraEffect = context.get(this.effectArg);
         float intensity = context.get(this.intensityArg);
         PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());

         assert playerRefComponent != null;

         playerRefComponent.getPacketHandler().writeNoCache(cameraEffect.createCameraShakePacket(intensity));
         context.sendMessage(Message.translation("server.commands.camshake.debug.success").param("effect", cameraEffect.getId()).param("intensity", intensity));
      }
   }
}
