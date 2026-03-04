package com.hypixel.hytale.server.core.modules.interaction.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class InteractionCommand extends AbstractCommandCollection {
   public InteractionCommand() {
      super("interaction", "server.commands.interaction.desc");
      this.addAliases("interact");
      this.addSubCommand(new InteractionRunCommand());
      this.addSubCommand(new InteractionSnapshotSourceCommand());
      this.addSubCommand(new InteractionClearCommand());
   }
}
