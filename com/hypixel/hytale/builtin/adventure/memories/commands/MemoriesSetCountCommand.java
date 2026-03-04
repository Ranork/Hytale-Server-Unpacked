package com.hypixel.hytale.builtin.adventure.memories.commands;

import com.hypixel.hytale.builtin.adventure.memories.MemoriesPlugin;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class MemoriesSetCountCommand extends AbstractWorldCommand {
   @Nonnull
   private static final Message MESSAGE_COMMANDS_MEMORIES_SETCOUNT_INVALID = Message.translation("server.commands.memories.setCount.invalid");
   @Nonnull
   private final RequiredArg<Integer> countArg = this.withRequiredArg("count", "server.commands.memories.setCount.count.desc", ArgTypes.INTEGER);

   public MemoriesSetCountCommand() {
      super("setCount", "server.commands.memories.setCount.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      int count = this.countArg.get(context);
      if (count < 0) {
         context.sendMessage(MESSAGE_COMMANDS_MEMORIES_SETCOUNT_INVALID);
      } else {
         int actualCount = MemoriesPlugin.get().setRecordedMemoriesCount(count);
         context.sendMessage(Message.translation("server.commands.memories.setCount.success").param("requested", count).param("actual", actualCount));
      }
   }
}
