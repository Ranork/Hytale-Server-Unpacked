package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.RelativeDirection;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FlipCommand extends AbstractPlayerCommand {
   public FlipCommand() {
      super("flip", "server.commands.flip.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.addUsageVariant(new FlipCommand.FlipWithDirectionCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      executeFlip(store, ref, playerRef, null);
   }

   private static void executeFlip(
      @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nullable RelativeDirection direction
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
         HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());
         if (headRotationComponent != null) {
            Axis axis;
            if (direction != null) {
               axis = RelativeDirection.toAxis(direction, headRotationComponent);
            } else {
               axis = headRotationComponent.getAxis();
            }

            BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.flip(r, axis, componentAccessor));
         }
      }
   }

   private static class FlipWithDirectionCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<RelativeDirection> directionArg = this.withRequiredArg(
         "direction", "server.commands.flip.direction.desc", RelativeDirection.ARGUMENT_TYPE
      );

      public FlipWithDirectionCommand() {
         super("server.commands.flip.desc");
         this.setPermissionGroup(GameMode.Creative);
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         FlipCommand.executeFlip(store, ref, playerRef, this.directionArg.get(context));
      }
   }
}
