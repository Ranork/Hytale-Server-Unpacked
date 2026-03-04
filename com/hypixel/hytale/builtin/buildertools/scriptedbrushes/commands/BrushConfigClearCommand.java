package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.commands;

import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.tooloperations.ToolOperation;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;

public class BrushConfigClearCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_BRUSH_CONFIG_CANNOT_USE_COMMAND_DURING_EXEC = Message.translation(
      "server.commands.brushConfig.cannotUseCommandDuringExec"
   );
   @Nonnull
   private static final Message MESSAGE_COMMANDS_BRUSH_CONFIG_CLEARED = Message.translation("server.commands.brushConfig.cleared");

   public BrushConfigClearCommand() {
      super("clear", "server.commands.scriptedbrushes.clear.desc");
      this.addAliases("disable");
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      UUID playerUUID = playerRef.getUuid();
      PrototypePlayerBuilderToolSettings prototypeSettings = ToolOperation.getOrCreatePrototypeSettings(playerUUID);
      BrushConfigCommandExecutor brushConfigCommandExecutor = ToolOperation.getOrCreatePrototypeSettings(playerUUID).getBrushConfigCommandExecutor();
      if (prototypeSettings.getBrushConfig().isCurrentlyExecuting()) {
         playerRef.sendMessage(MESSAGE_COMMANDS_BRUSH_CONFIG_CANNOT_USE_COMMAND_DURING_EXEC);
      } else {
         brushConfigCommandExecutor.getSequentialOperations().clear();
         brushConfigCommandExecutor.getGlobalOperations().clear();
         prototypeSettings.setUsePrototypeBrushConfigurations(false);
         playerRef.sendMessage(MESSAGE_COMMANDS_BRUSH_CONFIG_CLEARED);
      }
   }
}
