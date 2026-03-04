package com.hypixel.hytale.builtin.buildertools.scriptedbrushes.commands;

import com.hypixel.hytale.builtin.buildertools.PrototypePlayerBuilderToolSettings;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfig;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.BrushConfigCommandExecutor;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.ScriptedBrushAsset;
import com.hypixel.hytale.builtin.buildertools.scriptedbrushes.ui.ScriptedBrushPage;
import com.hypixel.hytale.builtin.buildertools.tooloperations.ToolOperation;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.AssetArgumentType;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;

public class BrushConfigLoadCommand extends AbstractPlayerCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_BRUSH_CONFIG_CANNOT_USE_COMMAND_DURING_EXEC = Message.translation(
      "server.commands.brushConfig.cannotUseCommandDuringExec"
   );

   public BrushConfigLoadCommand() {
      super("load", "server.commands.scriptedbrushes.load.desc");
      this.addUsageVariant(new BrushConfigLoadCommand.LoadByNameCommand());
   }

   @Override
   protected void execute(
      @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
   ) {
      UUID playerUUID = playerRef.getUuid();
      PrototypePlayerBuilderToolSettings prototypeSettings = ToolOperation.getOrCreatePrototypeSettings(playerUUID);
      BrushConfig brushConfig = prototypeSettings.getBrushConfig();
      if (brushConfig.isCurrentlyExecuting()) {
         playerRef.sendMessage(MESSAGE_COMMANDS_BRUSH_CONFIG_CANNOT_USE_COMMAND_DURING_EXEC);
      } else {
         Player playerComponent = store.getComponent(ref, Player.getComponentType());

         assert playerComponent != null;

         playerComponent.getPageManager().openCustomPage(ref, store, new ScriptedBrushPage(playerRef));
      }
   }

   private static class LoadByNameCommand extends AbstractPlayerCommand {
      @Nonnull
      private static final Message MESSAGE_COMMANDS_BRUSH_CONFIG_CANNOT_USE_COMMAND_DURING_EXEC = Message.translation(
         "server.commands.brushConfig.cannotUseCommandDuringExec"
      );
      @Nonnull
      private final RequiredArg<ScriptedBrushAsset> brushNameArg = this.withRequiredArg(
         "brushName",
         "server.commands.scriptedbrushes.load.brushName.desc",
         new AssetArgumentType(
            "server.commands.parsing.argtype.asset.scriptedbrush.name", ScriptedBrushAsset.class, "server.commands.parsing.argtype.asset.scriptedbrush.usage"
         )
      );

      public LoadByNameCommand() {
         super("server.commands.scriptedbrushes.load.byName.desc");
      }

      @Override
      protected void execute(
         @Nonnull CommandContext context, @Nonnull Store<EntityStore> store, @Nonnull Ref<EntityStore> ref, @Nonnull PlayerRef playerRef, @Nonnull World world
      ) {
         UUID playerUUID = playerRef.getUuid();
         PrototypePlayerBuilderToolSettings prototypeSettings = ToolOperation.getOrCreatePrototypeSettings(playerUUID);
         BrushConfig brushConfig = prototypeSettings.getBrushConfig();
         BrushConfigCommandExecutor brushConfigCommandExecutor = prototypeSettings.getBrushConfigCommandExecutor();
         if (brushConfig.isCurrentlyExecuting()) {
            playerRef.sendMessage(MESSAGE_COMMANDS_BRUSH_CONFIG_CANNOT_USE_COMMAND_DURING_EXEC);
         } else {
            ScriptedBrushAsset brushAssetArg = this.brushNameArg.get(context);
            brushAssetArg.loadIntoExecutor(brushConfigCommandExecutor);
            prototypeSettings.setCurrentlyLoadedBrushConfigName(brushAssetArg.getId());
            prototypeSettings.setUsePrototypeBrushConfigurations(true);
            playerRef.sendMessage(Message.translation("server.commands.brushConfig.loaded").param("name", brushAssetArg.getId()));
         }
      }
   }
}
