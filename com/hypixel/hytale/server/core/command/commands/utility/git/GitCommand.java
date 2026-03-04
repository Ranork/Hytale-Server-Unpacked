package com.hypixel.hytale.server.core.command.commands.utility.git;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class GitCommand extends AbstractCommandCollection {
   public GitCommand() {
      super("git", "server.commands.git.desc");
      this.addSubCommand(new UpdateAssetsCommand());
      this.addSubCommand(new UpdatePrefabsCommand());
   }
}
