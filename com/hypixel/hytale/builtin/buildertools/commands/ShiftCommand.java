package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EnumArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class ShiftCommand extends AbstractPlayerCommand {
   @Nonnull
   private final DefaultArg<Integer> distanceArg = this.withDefaultArg("distance", "server.commands.shift.distance.desc", ArgTypes.INTEGER, 1, "1");
   @Nonnull
   private final OptionalArg<Axis> axisArg = this.withOptionalArg(
      "axis", "server.commands.shift.axis.desc", new EnumArgumentType<>("server.commands.parsing.argtype.axis.name", Axis.class)
   );

   public ShiftCommand() {
      super("shift", "server.commands.shift.desc");
      this.setPermissionGroup(GameMode.Creative);
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
         Integer distance = this.distanceArg.get(context);
         Vector3i direction;
         if (this.axisArg.provided(context)) {
            direction = this.axisArg.get(context).getDirection().scale(distance);
         } else {
            HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

            assert headRotationComponent != null;

            direction = headRotationComponent.getAxisDirection().scale(distance);
         }

         BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.shift(r, direction, componentAccessor));
      }
   }
}
