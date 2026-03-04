package com.hypixel.hytale.server.core.command.commands.utility.worldmap;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.core.universe.world.worldmap.IWorldMap;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapLoadException;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class WorldMapReloadCommand extends AbstractWorldCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_WORLD_MAP_CLEAR_IMAGES = Message.translation("server.commands.worldmap.clearimages");

   public WorldMapReloadCommand() {
      super("reload", "server.commands.worldmap.reload.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      try {
         IWorldMap worldMap = world.getWorldConfig().getWorldMapProvider().getGenerator(world);
         world.getWorldMapManager().setGenerator(worldMap);
         context.sendMessage(MESSAGE_COMMANDS_WORLD_MAP_CLEAR_IMAGES);
      } catch (WorldMapLoadException var5) {
         HytaleLogger.getLogger().at(Level.SEVERE).log("Failed to reload world map for world " + world.getName(), var5);
         context.sendMessage(Message.translation("server.commands.worldmap.reloadFailed").param("error", var5.getMessage()));
      }
   }
}
