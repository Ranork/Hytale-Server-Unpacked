package com.hypixel.hytale.server.core.universe.world.commands.worldconfig;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Constants;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class WorldPauseCommand extends AbstractWorldCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_PAUSE_TOO_MANY_PLAYERS = Message.translation("server.commands.pause.tooManyPlayers");

   public WorldPauseCommand() {
      super("pause", "server.commands.pause.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      if (world.getPlayerCount() == 1 && Constants.SINGLEPLAYER) {
         world.setPaused(!world.isPaused());
         context.sendMessage(
            Message.translation("server.commands.pause.updated")
               .param("state", Message.translation(world.isPaused() ? "server.commands.pause.paused" : "server.commands.pause.unpaused"))
         );
      } else {
         context.sendMessage(MESSAGE_COMMANDS_PAUSE_TOO_MANY_PLAYERS);
      }
   }
}
