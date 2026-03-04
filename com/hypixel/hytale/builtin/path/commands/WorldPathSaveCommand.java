package com.hypixel.hytale.builtin.path.commands;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class WorldPathSaveCommand extends AbstractWorldCommand {
   @Nonnull
   private static final Message MESSAGE_UNIVERSE_WORLD_PATH_CONFIG_SAVED = Message.translation("server.universe.worldpath.configSaved");

   public WorldPathSaveCommand() {
      super("save", "server.commands.worldpath.save.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      world.getWorldPathConfig().save(world);
      context.sendMessage(MESSAGE_UNIVERSE_WORLD_PATH_CONFIG_SAVED);
   }
}
