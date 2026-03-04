package com.hypixel.hytale.builtin.adventure.memories.commands;

import com.hypixel.hytale.builtin.adventure.memories.MemoriesPlugin;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.CommandBase;
import javax.annotation.Nonnull;

public class MemoriesClearCommand extends CommandBase {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_MEMORIES_CLEAR_SUCCESS = Message.translation("server.commands.memories.clear.success");

   public MemoriesClearCommand() {
      super("clear", "server.commands.memories.clear.desc");
   }

   @Override
   protected void executeSync(@Nonnull CommandContext context) {
      MemoriesPlugin.get().clearRecordedMemories();
      context.sendMessage(MESSAGE_COMMANDS_MEMORIES_CLEAR_SUCCESS);
   }
}
