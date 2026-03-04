package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.builtin.buildertools.utils.FluidPatternHelper;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.protocol.GameMode;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockPattern;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class SubmergeCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<String> fluidItemArg = this.withRequiredArg("fluid-item", "server.commands.submerge.fluidType.desc", ArgTypes.BLOCK_TYPE_KEY);

   public SubmergeCommand() {
      super("submerge", "server.commands.submerge.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.addAliases("flood");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      Player playerComponent = store.getComponent(ref, Player.getComponentType());

      assert playerComponent != null;

      if (PrototypePlayerBuilderToolSettings.isOkayToDoCommandsOnSelection(ref, playerComponent, store)) {
         String fluidItemKey = this.fluidItemArg.get(context);
         if (!FluidPatternHelper.isFluidItem(fluidItemKey)) {
            context.sendMessage(Message.translation("server.builderTools.invalidBlockType").param("name", fluidItemKey).param("key", fluidItemKey));
         } else {
            BlockPattern pattern = BlockPattern.parse(fluidItemKey);
            BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.set(pattern, componentAccessor));
         }
      }
   }
}
