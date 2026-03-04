package com.hypixel.hytale.builtin.path.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class PrefabPathUpdateCommand extends AbstractCommandCollection {
   public PrefabPathUpdateCommand() {
      super("update", "server.commands.npcpath.update.desc");
      this.addSubCommand(new PrefabPathUpdatePauseCommand());
      this.addSubCommand(new PrefabPathUpdateObservationAngleCommand());
   }
}
