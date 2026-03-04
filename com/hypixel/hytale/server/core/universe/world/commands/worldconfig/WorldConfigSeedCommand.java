package com.hypixel.hytale.server.core.universe.world.commands.worldconfig;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class WorldConfigSeedCommand extends AbstractWorldCommand {
   public WorldConfigSeedCommand() {
      super("seed", "server.commands.seed.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      context.sendMessage(Message.translation("server.universe.seed.info").param("seed", world.getWorldConfig().getSeed()));
   }
}
