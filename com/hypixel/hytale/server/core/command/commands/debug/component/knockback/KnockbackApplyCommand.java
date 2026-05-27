package com.hypixel.hytale.server.core.command.commands.debug.component.knockback;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import com.hypixel.hytale.protocol.VelocityThresholdStyle;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.knockback.KnockbackComponent;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.modules.splitvelocity.VelocityConfig;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.util.TargetUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class KnockbackApplyCommand extends AbstractCommandCollection {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_KNOCKBACK_APPLY_SUCCESS = Message.translation("server.commands.knockback.apply.success");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_KNOCKBACK_APPLY_INVALID_DIRECTION = Message.translation("server.commands.knockback.apply.invalidDirection");
   @Nonnull
   private static final Message MESSAGE_COMMANDS_KNOCKBACK_APPLY_INVALID_PRESET = Message.translation("server.commands.knockback.apply.invalidPreset");
   @Nonnull
   private static final Message MESSAGE_GENERAL_NO_ENTITY_IN_VIEW = Message.translation("server.general.noEntityInView");

   public KnockbackApplyCommand() {
      super("apply", "server.commands.knockback.apply.desc");
      this.addSubCommand(new KnockbackApplyCommand.KnockbackApplySelfCommand());
      this.addSubCommand(new KnockbackApplyCommand.KnockbackApplyAimedCommand());
   }

   private static void applyKnockback(
      @Nonnull Ref<EntityStore> targetRef,
      @Nonnull CommandContext context,
      @Nonnull Store<EntityStore> store,
      @Nonnull Ref<EntityStore> sourceRef,
      @Nonnull String directionValue,
      float y,
      float force,
      @Nonnull String presetValue
   ) {
      KnockbackApplyCommand.HorizontalDirection direction = parseDirection(directionValue);
      if (direction == KnockbackApplyCommand.HorizontalDirection.Invalid) {
         context.sendMessage(
            MESSAGE_COMMANDS_KNOCKBACK_APPLY_INVALID_DIRECTION.param("direction", directionValue).param("values", "left|l, right|r, forward|f, backward|b")
         );
      } else {
         KnockbackApplyCommand.VelocityConfigPreset velocityConfig = velocityConfigPreset(presetValue);
         if (velocityConfig == KnockbackApplyCommand.VelocityConfigPreset.Invalid) {
            context.sendMessage(MESSAGE_COMMANDS_KNOCKBACK_APPLY_INVALID_PRESET.param("preset", presetValue).param("values", "null, linear, exp, def"));
         } else {
            Vector3d velocity = computeVelocity(direction, y, force, sourceRef, store);
            KnockbackComponent knockbackComponent = store.ensureAndGetComponent(targetRef, KnockbackComponent.getComponentType());
            knockbackComponent.setVelocity(velocity);
            knockbackComponent.setVelocityType(ChangeVelocityType.Set);
            knockbackComponent.setVelocityConfig(toVelocityConfig(velocityConfig));
            knockbackComponent.setDuration(0.0F);
            knockbackComponent.setTimer(0.0F);
            context.sendMessage(
               MESSAGE_COMMANDS_KNOCKBACK_APPLY_SUCCESS.param("force", force)
                  .param("y", y)
                  .param("direction", direction.name().toLowerCase())
                  .param("preset", velocityConfig.name().toLowerCase())
            );
         }
      }
   }

   @Nonnull
   private static Vector3d computeVelocity(
      @Nonnull KnockbackApplyCommand.HorizontalDirection direction,
      float y,
      float force,
      @Nonnull Ref<EntityStore> sourceRef,
      @Nonnull Store<EntityStore> store
   ) {
      HeadRotation headRotationComponent = store.getComponent(sourceRef, HeadRotation.getComponentType());
      float yaw = headRotationComponent == null ? Rotation3f.IDENTITY.yaw() : headRotationComponent.getRotation().yaw();
      Vector3d forward = new Vector3d(0.0, 0.0, -1.0).rotateY(yaw);

      Vector3d horizontal = switch (direction) {
         case Left -> new Vector3d(forward.z, 0.0, -forward.x);
         case Right -> new Vector3d(-forward.z, 0.0, forward.x);
         case Forward, Invalid -> forward;
         case Backward -> forward.negate();
      };
      horizontal.normalize().mul(force);
      return new Vector3d(horizontal.x, y, horizontal.z);
   }

   @Nonnull
   private static KnockbackApplyCommand.HorizontalDirection parseDirection(@Nonnull String directionValue) {
      String var1 = directionValue.toLowerCase();

      return switch (var1) {
         case "left", "l" -> KnockbackApplyCommand.HorizontalDirection.Left;
         case "right", "r" -> KnockbackApplyCommand.HorizontalDirection.Right;
         case "forward", "f" -> KnockbackApplyCommand.HorizontalDirection.Forward;
         case "backward", "b" -> KnockbackApplyCommand.HorizontalDirection.Backward;
         default -> KnockbackApplyCommand.HorizontalDirection.Invalid;
      };
   }

   @Nonnull
   private static KnockbackApplyCommand.VelocityConfigPreset velocityConfigPreset(@Nonnull String presetValue) {
      String var1 = presetValue.toLowerCase();

      return switch (var1) {
         case "null" -> KnockbackApplyCommand.VelocityConfigPreset.Null;
         case "linear" -> KnockbackApplyCommand.VelocityConfigPreset.Linear;
         case "exp" -> KnockbackApplyCommand.VelocityConfigPreset.Exp;
         case "def" -> KnockbackApplyCommand.VelocityConfigPreset.Def;
         default -> KnockbackApplyCommand.VelocityConfigPreset.Invalid;
      };
   }

   @Nullable
   private static VelocityConfig toVelocityConfig(@Nonnull KnockbackApplyCommand.VelocityConfigPreset preset) {
      return switch (preset) {
         case Null -> null;
         case Linear -> createVelocityConfig(0.99F, 0.98F, 0.94F, 0.3F, 3.0F, VelocityThresholdStyle.Linear);
         case Exp -> createVelocityConfig(0.97F, 0.96F, 0.94F, 0.3F, 3.0F, VelocityThresholdStyle.Exp);
         case Def -> createVelocityConfig(0.96F, 0.0F, 0.82F, 0.0F, 1.0F, VelocityThresholdStyle.Linear);
         case Invalid -> null;
      };
   }

   @Nonnull
   private static VelocityConfig createVelocityConfig(
      float airResistance, float airResistanceMax, float groundResistance, float groundResistanceMax, float threshold, @Nonnull VelocityThresholdStyle style
   ) {
      VelocityConfig velocityConfig = new VelocityConfig();
      velocityConfig.setAirResistance(airResistance);
      velocityConfig.setAirResistanceMax(airResistanceMax);
      velocityConfig.setGroundResistance(groundResistance);
      velocityConfig.setGroundResistanceMax(groundResistanceMax);
      velocityConfig.setThreshold(threshold);
      velocityConfig.setStyle(style);
      return velocityConfig;
   }

   private static enum HorizontalDirection {
      Left,
      Right,
      Forward,
      Backward,
      Invalid;
   }

   public static class KnockbackApplyAimedCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<String> directionArg = this.withRequiredArg("direction", "server.commands.knockback.apply.direction.desc", ArgTypes.STRING);
      @Nonnull
      private final RequiredArg<Float> yArg = this.withRequiredArg("y", "server.commands.knockback.apply.y.desc", ArgTypes.FLOAT);
      @Nonnull
      private final RequiredArg<Float> forceArg = this.withRequiredArg("force", "server.commands.knockback.apply.force.desc", ArgTypes.FLOAT);
      @Nonnull
      private final RequiredArg<String> presetArg = this.withRequiredArg("preset", "server.commands.knockback.apply.preset.desc", ArgTypes.STRING);

      public KnockbackApplyAimedCommand() {
         super("aimed", "server.commands.knockback.apply.aimed.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         Ref<EntityStore> targetRef = TargetUtil.getTargetEntity(ref, store);
         if (targetRef != null && targetRef.isValid()) {
            KnockbackApplyCommand.applyKnockback(
               targetRef, context, store, ref, this.directionArg.get(context), this.yArg.get(context), this.forceArg.get(context), this.presetArg.get(context)
            );
         } else {
            context.sendMessage(KnockbackApplyCommand.MESSAGE_GENERAL_NO_ENTITY_IN_VIEW);
         }
      }
   }

   public static class KnockbackApplySelfCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<String> directionArg = this.withRequiredArg("direction", "server.commands.knockback.apply.direction.desc", ArgTypes.STRING);
      @Nonnull
      private final RequiredArg<Float> yArg = this.withRequiredArg("y", "server.commands.knockback.apply.y.desc", ArgTypes.FLOAT);
      @Nonnull
      private final RequiredArg<Float> forceArg = this.withRequiredArg("force", "server.commands.knockback.apply.force.desc", ArgTypes.FLOAT);
      @Nonnull
      private final RequiredArg<String> presetArg = this.withRequiredArg("preset", "server.commands.knockback.apply.preset.desc", ArgTypes.STRING);

      public KnockbackApplySelfCommand() {
         super("self", "server.commands.knockback.apply.self.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         KnockbackApplyCommand.applyKnockback(
            ref, context, store, ref, this.directionArg.get(context), this.yArg.get(context), this.forceArg.get(context), this.presetArg.get(context)
         );
      }
   }

   private static enum VelocityConfigPreset {
      Null,
      Linear,
      Exp,
      Def,
      Invalid;
   }
}
