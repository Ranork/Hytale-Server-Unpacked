package com.hypixel.hytale.server.core.universe.world.commands.worldconfig;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.CommandSender;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class WorldConfigPauseTimeCommand extends AbstractWorldCommand {
   public WorldConfigPauseTimeCommand() {
      super("pausetime", "server.commands.pausetime.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      pauseTime(context.sender(), world, store);
   }

   public static void pauseTime(@Nonnull CommandSender commandSender, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      WorldTimeResource worldTimeResource = store.getResource(WorldTimeResource.getResourceType());
      boolean timePause = !world.getWorldConfig().isGameTimePaused();
      WorldConfig worldConfig = world.getWorldConfig();
      worldConfig.setGameTimePaused(timePause);
      worldConfig.markChanged();
      Message timePausedMessage = Message.translation(timePause ? "server.general.paused" : "server.general.resumed");
      commandSender.sendMessage(
         Message.translation("server.commands.pausetime.timeInfo")
            .param("msg", timePausedMessage)
            .param("worldName", world.getName())
            .param("time", worldTimeResource.getGameTime().toString())
      );
   }
}
