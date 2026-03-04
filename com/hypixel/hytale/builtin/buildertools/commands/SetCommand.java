package com.hypixel.hytale.builtin.buildertools.commands;

import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
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

public class SetCommand extends AbstractPlayerCommand {
   @Nonnull
   private final RequiredArg<BlockPattern> patternArg = this.withRequiredArg("pattern", "server.commands.set.args.blocktype.desc", ArgTypes.BLOCK_PATTERN);

   public SetCommand() {
      super("setBlocks", "server.commands.set.desc");
      this.setPermissionGroup(GameMode.Creative);
      this.requirePermission("hytale.editor.selection.modify");
      this.addAliases("set");
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
            BuilderToolsPlugin.addToQueue(playerComponent, playerRef, (r, s, componentAccessor) -> s.set(pattern, componentAccessor));
            context.sendMessage(Message.translation("server.builderTools.set.selectionSet").param("key", pattern.toString()));
         } else {
            context.sendMessage(Message.translation("server.builderTools.invalidBlockType").param("name", "").param("key", ""));
         }
      }
   }
}
