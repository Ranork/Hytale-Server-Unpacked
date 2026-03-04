package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.DefaultArg;
import com.hypixel.hytale.server.core.command.system.arguments.system.FlagArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class HollowCommand extends AbstractPlayerCommand {
   @Nonnull
   private final DefaultArg<String> blockTypeArg = this.withDefaultArg(
      "blockType", "server.commands.hollow.blockType.desc", ArgTypes.BLOCK_TYPE_KEY, "Empty", "Air"
   );
   @Nonnull
   private final DefaultArg<Integer> thicknessArg = this.withDefaultArg(
         "thickness", "server.commands.hollow.thickness.desc", ArgTypes.INTEGER, 1, "Thickness of 1"
      )
      .addValidator(Validators.range(1, 128));
   @Nonnull
   private final FlagArg floorArg = this.withFlagArg("floor", "server.commands.hollow.floor.desc").addAliases("bottom");
   @Nonnull
   private final FlagArg roofArg = this.withFlagArg("roof", "server.commands.hollow.roof.desc").addAliases("ceiling", "top");
   @Nonnull
   private final FlagArg perimeterArg = this.withFlagArg("perimeter", "server.commands.hollow.perimeter.desc").addAliases("all");

   public HollowCommand() {
      super("hollow", "server.commands.hollow.desc");
      this.setPermissionGroup(GameMode.Creative);
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
         int blockTypeIndex = BlockType.getAssetMap().getIndex(this.blockTypeArg.get(context));
         Boolean floor = this.floorArg.get(context);
         Boolean roof = this.roofArg.get(context);
         Boolean perimeter = this.perimeterArg.get(context);
         BuilderToolsPlugin.addToQueue(
            playerComponent,
            playerRef,
            (r, s, componentAccessor) -> s.hollow(r, blockTypeIndex, this.thicknessArg.get(context), roof || perimeter, floor || perimeter, componentAccessor)
         );
      }
   }
}
