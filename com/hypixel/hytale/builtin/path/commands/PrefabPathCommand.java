package com.hypixel.hytale.builtin.path.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class PrefabPathCommand extends AbstractCommandCollection {
   public PrefabPathCommand() {
      super("path", "server.commands.npcpath.desc");
      this.addSubCommand(new PrefabPathListCommand());
      this.addSubCommand(new PrefabPathNodesCommand());
      this.addSubCommand(new PrefabPathNewCommand());
      this.addSubCommand(new PrefabPathEditCommand());
      this.addSubCommand(new PrefabPathAddCommand());
      this.addSubCommand(new PrefabPathMergeCommand());
      this.addSubCommand(new PrefabPathUpdateCommand());
   }
}
