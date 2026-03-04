package com.hypixel.hytale.builtin.adventure.memories.commands;

import com.hypixel.hytale.builtin.adventure.memories.MemoriesPlugin;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class MemoriesUnlockCommand extends AbstractWorldCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_MEMORIES_UNLOCK_ALL_SUCCESS = Message.translation("server.commands.memories.unlockAll.success");

   public MemoriesUnlockCommand() {
      super("unlockAll", "server.commands.memories.unlockAll.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      MemoriesPlugin.get().recordAllMemories();
      context.sendMessage(MESSAGE_COMMANDS_MEMORIES_UNLOCK_ALL_SUCCESS);
   }
}
