package com.hypixel.hytale.builtin.adventure.memories.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class MemoriesCommand extends AbstractCommandCollection {
   public MemoriesCommand() {
      super("memories", "server.commands.memories.desc");
      this.addSubCommand(new MemoriesClearCommand());
      this.addSubCommand(new MemoriesCapacityCommand());
      this.addSubCommand(new MemoriesLevelCommand());
      this.addSubCommand(new MemoriesUnlockCommand());
      this.addSubCommand(new MemoriesSetCountCommand());
   }
}
