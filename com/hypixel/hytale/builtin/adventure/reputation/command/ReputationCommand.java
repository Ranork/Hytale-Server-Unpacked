package com.hypixel.hytale.builtin.adventure.reputation.command;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class ReputationCommand extends AbstractCommandCollection {
   public ReputationCommand() {
      super("reputation", "server.commands.reputation.desc");
      this.addSubCommand(new ReputationAddCommand());
      this.addSubCommand(new ReputationSetCommand());
      this.addSubCommand(new ReputationRankCommand());
      this.addSubCommand(new ReputationValueCommand());
   }
}
