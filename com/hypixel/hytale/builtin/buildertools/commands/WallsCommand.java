package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class WallsCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<BlockPattern> patternArg = this.withRequiredArg("pattern", "server.commands.walls.blockType.desc", ArgTypes.BLOCK_PATTERN);
   @Nonnull
   private final DefaultArg<Integer> thicknessArg = this.withDefaultArg(
         "thickness", "server.commands.walls.thickness.desc", ArgTypes.INTEGER, 1, "Thickness of one"
      )
      .addValidator(Validators.range(1, 128));
   @Nonnull
   private final FlagArg floorArg = this.withFlagArg("floor", "server.commands.walls.floor.desc").addAliases("bottom");
   @Nonnull
   private final FlagArg roofArg = this.withFlagArg("roof", "server.commands.walls.roof.desc").addAliases("ceiling", "top");
   @Nonnull
   private final FlagArg perimeterArg = this.withFlagArg("perimeter", "server.commands.walls.perimeter.desc").addAliases("all");

   public WallsCommand() {
      super("wall", "server.commands.walls.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.addAliases("walls", "side", "sides");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
         BlockPattern pattern = this.patternArg.get(context);
         if (pattern != null && !pattern.isEmpty()) {
            Boolean floor = this.floorArg.get(context);
            Boolean roof = this.roofArg.get(context);
            Boolean perimeter = this.perimeterArg.get(context);
            BuilderToolsPlugin.addToQueue(
               playerComponent,
               playerRef,
               (r, s, componentAccessor) -> s.walls(r, pattern, this.thicknessArg.get(context), roof || perimeter, floor || perimeter, componentAccessor)
            );
         } else {
            context.sendMessage(Message.translation("server.builderTools.invalidBlockType").param("name", "").param("key", ""));
         }
      }
   }
}
