package com.hypixel.hytale.builtin.adventure.objectives.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class ObjectiveCommand extends AbstractCommandCollection {
   public ObjectiveCommand() {
      super("objective", "server.commands.objective");
      this.addAliases("obj");
      this.addSubCommand(new ObjectiveStartCommand());
      this.addSubCommand(new ObjectiveCompleteCommand());
      this.addSubCommand(new ObjectivePanelCommand());
      this.addSubCommand(new ObjectiveHistoryCommand());
      this.addSubCommand(new ObjectiveLocationMarkerCommand());
      this.addSubCommand(new ObjectiveReachLocationMarkerCommand());
   }
}
