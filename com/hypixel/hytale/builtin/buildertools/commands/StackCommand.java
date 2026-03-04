package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
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

public class StackCommand extends AbstractPlayerCommand {
   @Nonnull
   private final FlagArg emptyFlag = this.withFlagArg("empty", "server.commands.stack.empty.desc");
   @Nonnull
   private final OptionalArg<Integer> spacingArg = this.withOptionalArg("spacing", "server.commands.stack.spacing.desc", ArgTypes.INTEGER);

   public StackCommand() {
      super("stack", "server.commands.stack.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.addUsageVariant(new StackCommand.StackWithCountCommand());
      this.addUsageVariant(new StackCommand.StackWithDirectionAndCountCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      executeStack(store, ref, null, 1, this.emptyFlag.get(context), this.spacingArg.provided(context) ? this.spacingArg.get(context) : 0);
   }

   private static void executeStack(
      @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nullable RelativeDirection direction, int count, boolean empty, int spacing
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      PlayerRef playerRefComponent = store.getComponent(ref, PlayerRef.getComponentType());

      assert playerRefComponent != null;

      if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
         HeadRotation headRotationComponent = store.getComponent(ref, HeadRotation.getComponentType());

         assert headRotationComponent != null;

         Vector3i directionVector = RelativeDirection.toDirectionVector(direction, headRotationComponent);
         BuilderToolsPlugin.addToQueue(
            playerComponent, playerRefComponent, (r, s, componentAccessor) -> s.stack(r, directionVector, count, empty, spacing, componentAccessor)
         );
      }
   }

   private static class StackWithCountCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<Integer> countArg = this.withRequiredArg("count", "server.commands.stack.count.desc", ArgTypes.INTEGER);
      @Nonnull
      private final FlagArg emptyFlag = this.withFlagArg("empty", "server.commands.stack.empty.desc");
      @Nonnull
      private final OptionalArg<Integer> spacingArg = this.withOptionalArg("spacing", "server.commands.stack.spacing.desc", ArgTypes.INTEGER);

      public StackWithCountCommand() {
         super("server.commands.stack.desc");
         this.setPermissionGroup(GameMode.Creative);
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         StackCommand.executeStack(
            store, ref, null, this.countArg.get(context), this.emptyFlag.get(context), this.spacingArg.provided(context) ? this.spacingArg.get(context) : 0
         );
      }
   }

   private static class StackWithDirectionAndCountCommand extends AbstractPlayerCommand {
      @Nonnull
      private final RequiredArg<RelativeDirection> directionArg = this.withRequiredArg(
         "direction", "server.commands.stack.direction.desc", RelativeDirection.ARGUMENT_TYPE
      );
      @Nonnull
      private final RequiredArg<Integer> countArg = this.withRequiredArg("count", "server.commands.stack.count.desc", ArgTypes.INTEGER);
      @Nonnull
      private final FlagArg emptyFlag = this.withFlagArg("empty", "server.commands.stack.empty.desc");
      @Nonnull
      private final OptionalArg<Integer> spacingArg = this.withOptionalArg("spacing", "server.commands.stack.spacing.desc", ArgTypes.INTEGER);

      public StackWithDirectionAndCountCommand() {
         super("server.commands.stack.desc");
         this.setPermissionGroup(GameMode.Creative);
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         StackCommand.executeStack(
            store,
            ref,
            this.directionArg.get(context),
            this.countArg.get(context),
            this.emptyFlag.get(context),
            this.spacingArg.provided(context) ? this.spacingArg.get(context) : 0
         );
      }
   }
}
