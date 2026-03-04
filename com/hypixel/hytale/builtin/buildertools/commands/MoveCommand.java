package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeDirection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MoveCommand extends AbstractPlayerCommand {
   @Nonnull
   private final FlagArg emptyFlag = this.withFlagArg("empty", "server.commands.move.empty.desc");
   @Nonnull
   private final FlagArg entitiesFlag = this.withFlagArg("entities", "server.commands.move.entities.desc");

   public MoveCommand() {
      super("move", "server.commands.move.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.addUsageVariant(new MoveCommand.MoveWithDistanceCommand());
      this.addUsageVariant(new MoveCommand.MoveWithDirectionAndDistanceCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      executeMove(store, ref, playerRef, null, 1, this.emptyFlag.get(context), this.entitiesFlag.get(context));
   }

   private static void executeMove(
      @Nonnull Store<EntityStore> store,
      @Nonnull Ref<EntityStore> ref,
      @Nonnull PlayerRef playerRef,
      @Nullable RelativeDirection direction,
      int distance,
      boolean empty,
      boolean entities
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
         HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

         assert headRotationComponent != null;

         Vector3i directionVector = RelativeDirection.toDirectionVector(direction, headRotationComponent).scale(distance);
         BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.move(r, directionVector, empty, entities, componentAccessor));
      }
   }

   private static class MoveWithDirectionAndDistanceCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<RelativeDirection> directionArg = this.withRequiredArg(
         "direction", "server.commands.move.direction.desc", RelativeDirection.ARGUMENT_TYPE
      );
      @Nonnull
      private final RequiredArg<Integer> distanceArg = this.withRequiredArg("distance", "server.commands.move.distance.desc", ArgTypes.INTEGER);
      @Nonnull
      private final FlagArg emptyFlag = this.withFlagArg("empty", "server.commands.move.empty.desc");
      @Nonnull
      private final FlagArg entitiesFlag = this.withFlagArg("entities", "server.commands.move.entities.desc");

      public MoveWithDirectionAndDistanceCommand() {
         super("server.commands.move.desc");
         this.setPermissionGroup(GameMode.Creative);
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         MoveCommand.executeMove(
            store, ref, playerRef, this.directionArg.get(context), this.distanceArg.get(context), this.emptyFlag.get(context), this.entitiesFlag.get(context)
         );
      }
   }

   private static class MoveWithDistanceCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<Integer> distanceArg = this.withRequiredArg("distance", "server.commands.move.distance.desc", ArgTypes.INTEGER);
      @Nonnull
      private final FlagArg emptyFlag = this.withFlagArg("empty", "server.commands.move.empty.desc");
      @Nonnull
      private final FlagArg entitiesFlag = this.withFlagArg("entities", "server.commands.move.entities.desc");

      public MoveWithDistanceCommand() {
         super("server.commands.move.desc");
         this.setPermissionGroup(GameMode.Creative);
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         MoveCommand.executeMove(store, ref, playerRef, null, this.distanceArg.get(context), this.emptyFlag.get(context), this.entitiesFlag.get(context));
      }
   }
}
