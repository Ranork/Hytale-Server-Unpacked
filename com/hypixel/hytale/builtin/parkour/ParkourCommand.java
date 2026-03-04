package com.hypixel.hytale.builtin.parkour;

import com.hypixel.hytale.builtin.parkour.commands.CheckpointAddCommand;
import com.hypixel.hytale.builtin.parkour.commands.CheckpointRemoveCommand;
import com.hypixel.hytale.builtin.parkour.commands.CheckpointResetCommand;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class ParkourCommand extends AbstractCommandCollection {
   public ParkourCommand() {
      super("checkpoint", "server.commands.checkpoint.desc");
      this.addSubCommand(new CheckpointAddCommand());
      this.addSubCommand(new CheckpointRemoveCommand());
      this.addSubCommand(new CheckpointResetCommand());
   }
}
