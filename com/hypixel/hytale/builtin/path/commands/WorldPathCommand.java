package com.hypixel.hytale.builtin.path.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class WorldPathCommand extends AbstractCommandCollection {
   public WorldPathCommand() {
      super("worldpath", "server.commands.worldpath.desc");
      this.addSubCommand(new WorldPathListCommand());
      this.addSubCommand(new WorldPathRemoveCommand());
      this.addSubCommand(new WorldPathSaveCommand());
      this.addSubCommand(new WorldPathBuilderCommand());
   }
}
