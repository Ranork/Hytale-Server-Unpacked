package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.OptionalArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.arguments.types.EnumArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.HeadRotation;
import com.hypixel.hytale.server.core.permissions.HytalePermissions;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import org.joml.Vector3i;

public class ExtendFaceCommand extends AbstractPlayerCommand {
   private static final EnumArgumentType<ExtendFaceCommand.Direction> DIRECTION_TYPE = new EnumArgumentType<>(
      "server.commands.extendface.argtype.direction", ExtendFaceCommand.Direction.class
   );
   private static final EnumArgumentType<ExtendFaceCommand.ExtrudeFilter> FILTER_TYPE = new EnumArgumentType<>(
      "server.commands.extendface.argtype.filter", ExtendFaceCommand.ExtrudeFilter.class
   );
   private static final EnumArgumentType<ExtendFaceCommand.ExtrudeStrategy> STRATEGY_TYPE = new EnumArgumentType<>(
      "server.commands.extendface.argtype.strategy", ExtendFaceCommand.ExtrudeStrategy.class
   );
   @Nonnull
   private final RequiredArg<Integer> xArg = this.withRequiredArg("x", "server.commands.extendface.x.desc", ArgTypes.INTEGER);
   @Nonnull
   private final RequiredArg<Integer> yArg = this.withRequiredArg("y", "server.commands.extendface.y.desc", ArgTypes.INTEGER);
   @Nonnull
   private final RequiredArg<Integer> zArg = this.withRequiredArg("z", "server.commands.extendface.z.desc", ArgTypes.INTEGER);
   @Nonnull
   private final RequiredArg<Integer> depthArg = this.withRequiredArg("depth", "server.commands.extendface.depth.desc", ArgTypes.INTEGER);
   @Nonnull
   private final RequiredArg<Integer> widthArg = this.withRequiredArg("width", "server.commands.extendface.width.desc", ArgTypes.INTEGER);
   @Nonnull
   private final RequiredArg<Integer> lengthArg = this.withRequiredArg("length", "server.commands.extendface.length.desc", ArgTypes.INTEGER);
   @Nonnull
   private final OptionalArg<ExtendFaceCommand.Direction> directionArg = this.withOptionalArg(
      "direction", "server.commands.extendface.direction.desc", DIRECTION_TYPE
   );
   @Nonnull
   private final OptionalArg<BlockPattern> patternArg = this.withOptionalArg("pattern", "server.commands.extendface.pattern.desc", ArgTypes.BLOCK_PATTERN);
   @Nonnull
   private final DefaultArg<ExtendFaceCommand.ExtrudeFilter> filterArg = this.withDefaultArg(
      "filter", "server.commands.extendface.filter.desc", FILTER_TYPE, ExtendFaceCommand.ExtrudeFilter.ALL, "All"
   );
   @Nonnull
   private final DefaultArg<ExtendFaceCommand.ExtrudeStrategy> strategyArg = this.withDefaultArg(
      "strategy", "server.commands.extendface.strategy.desc", STRATEGY_TYPE, ExtendFaceCommand.ExtrudeStrategy.DEFAULT, "Default"
   );
   @Nonnull
   private final FlagArg shrinkFlag = this.withFlagArg("shrink", "server.commands.extendface.shrink.desc");

   public ExtendFaceCommand() {
      super("extendface", "server.commands.extendface.desc");
      this.setPermissionGroups("hytale:WorldEditor");
      this.requirePermission(HytalePermissions.EDITOR_SELECTION_MODIFY);
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerRef, store)) {
         int x = this.xArg.get(context);
         int y = this.yArg.get(context);
         int z = this.zArg.get(context);
         Vector3i normal;
         if (this.directionArg.provided(context)) {
            normal = this.directionArg.get(context).toNormal();
         } else {
            HeadRotation headRotation = store.getComponent(ref, HeadRotation.getComponentType());
            if (headRotation == null) {
               return;
            }

            normal = headRotation.getAxisDirection();
         }

         int depth = this.depthArg.get(context);
         int width = this.widthArg.get(context);
         int length = this.lengthArg.get(context);
         boolean shrink = this.shrinkFlag.get(context);
         BlockPattern pattern = this.patternArg.provided(context) && !shrink ? this.patternArg.get(context) : BlockPattern.EMPTY;
         String filterMode = this.filterArg.get(context).toFilterMode();
         String strategyMode = this.strategyArg.get(context).toStrategyMode();
         BuilderToolsPlugin.addToQueue(
            playerComponent,
            playerRef,
            (r, s, ca) -> s.extendOrShrinkFace(
               x, y, z, normal.x(), normal.y(), normal.z(), depth, width, length, shrink, pattern, filterMode, strategyMode, 1, false, ca
            )
         );
      }
   }

   static enum Direction {
      UP,
      DOWN,
      EAST,
      WEST,
      NORTH,
      SOUTH;

      public Vector3i toNormal() {
         return switch (this) {
            case UP -> new Vector3i(0, 1, 0);
            case DOWN -> new Vector3i(0, -1, 0);
            case EAST -> new Vector3i(1, 0, 0);
            case WEST -> new Vector3i(-1, 0, 0);
            case NORTH -> new Vector3i(0, 0, -1);
            case SOUTH -> new Vector3i(0, 0, 1);
         };
      }
   }

   static enum ExtrudeFilter {
      ALL,
      SAME_MATERIAL,
      SAME_SHAPE,
      FULL_BLOCKS,
      NOT_FULL_BLOCKS;

      public String toFilterMode() {
         return switch (this) {
            case ALL -> "All";
            case SAME_MATERIAL -> "SameMaterial";
            case SAME_SHAPE -> "SameShape";
            case FULL_BLOCKS -> "FullBlocks";
            case NOT_FULL_BLOCKS -> "NotFullBlocks";
         };
      }
   }

   static enum ExtrudeStrategy {
      ALL,
      DEFAULT;

      public String toStrategyMode() {
         return switch (this) {
            case ALL -> "All";
            case DEFAULT -> "Default";
         };
      }
   }
}
