package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EnumArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class RotateCommand extends AbstractCommandCollection {
   public RotateCommand() {
      super("rotate", "server.commands.rotate.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.addUsageVariant(new RotateCommand.RotateArbitraryVariant());
      this.addUsageVariant(new RotateCommand.RotateAxisVariant());
   }

   private static class RotateArbitraryVariant extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<Float> yawArg = this.withRequiredArg("yaw", "server.commands.rotate.yaw.desc", ArgTypes.FLOAT);
      @Nonnull
      private final RequiredArg<Float> pitchArg = this.withRequiredArg("pitch", "server.commands.rotate.pitch.desc", ArgTypes.FLOAT);
      @Nonnull
      private final RequiredArg<Float> rollArg = this.withRequiredArg("roll", "server.commands.rotate.roll.desc", ArgTypes.FLOAT);

      RotateArbitraryVariant() {
         super("server.commands.rotate.arbitrary.variant.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
            float yaw = this.yawArg.get(context);
            float pitch = this.pitchArg.get(context);
            float roll = this.rollArg.get(context);
            boolean isSimple90Degree = yaw % 90.0F == 0.0F && pitch % 90.0F == 0.0F && roll % 90.0F == 0.0F;
            if (isSimple90Degree && pitch == 0.0F && roll == 0.0F) {
               BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.rotate(r, Axis.Y, (int)yaw, componentAccessor));
            } else {
               BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.rotateArbitrary(r, yaw, pitch, roll, componentAccessor));
            }
         }
      }
   }

   private static class RotateAxisVariant extends AbstractPlayerCommand {
      @Nonnull
      private static final Message MESSAGE_BUILDER_TOOLS_ROTATE_ANGLE_USAGE = Message.translation("server.builderTools.rotate.angleUsage");
      @Nonnull
      private final RequiredArg<Integer> angleArg = this.withRequiredArg("angle", "server.commands.rotate.angle.desc", ArgTypes.INTEGER);
      @Nonnull
      private final DefaultArg<Axis> axisArg = this.withDefaultArg(
         "axis", "server.commands.rotate.axis.desc", new EnumArgumentType<>("server.commands.parsing.argtype.axis.name", Axis.class), Axis.Y, "Y"
      );

      RotateAxisVariant() {
         super("server.commands.rotate.axis.variant.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
            int angle = this.angleArg.get(context);
            Axis axis = this.axisArg.get(context);
            if (angle % 90 != 0) {
               context.sendMessage(MESSAGE_BUILDER_TOOLS_ROTATE_ANGLE_USAGE);
            } else {
               BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.rotate(r, axis, angle, componentAccessor));
            }
         }
      }
   }
}
