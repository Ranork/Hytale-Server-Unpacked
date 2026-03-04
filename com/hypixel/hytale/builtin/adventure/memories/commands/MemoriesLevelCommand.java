package com.hypixel.hytale.builtin.adventure.memories.commands;

import com.hypixel.hytale.builtin.adventure.memories.MemoriesPlugin;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class MemoriesLevelCommand extends AbstractWorldCommand {
   public MemoriesLevelCommand() {
      super("level", "server.commands.memories.level.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      int level = MemoriesPlugin.get().getMemoriesLevel(world.getGameplayConfig());
      context.sendMessage(Message.translation("server.commands.memories.level.success").param("level", level));
   }
}
