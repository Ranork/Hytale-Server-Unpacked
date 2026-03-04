package com.hypixel.hytale.server.core.universe.world.commands.worldconfig;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.logging.Level;
import javax.annotation.Nonnull;

public class WorldConfigSetSpawnDefaultCommand extends AbstractWorldCommand {
   @Nonnull
   private static final Message MESSAGE_UNIVERSE_SET_SPAWN_DEFAULT = Message.translation("server.universe.setspawn.default");

   public WorldConfigSetSpawnDefaultCommand() {
      super("default", "server.commands.world.config.setspawn.default.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      world.getWorldConfig().setSpawnProvider(null);
      world.getLogger().at(Level.INFO).log("Set spawn provider to: %s", world.getWorldConfig().getSpawnProvider());
      context.sendMessage(MESSAGE_UNIVERSE_SET_SPAWN_DEFAULT);
   }
}
