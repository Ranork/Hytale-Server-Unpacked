package com.hypixel.hytale.builtin.locate.command;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class LocateCommand extends AbstractCommandCollection {
   public LocateCommand() {
      super("locate", "server.commands.locate.desc");
      this.addSubCommand(new LocateBiomeCommand());
      this.addSubCommand(new LocateZoneCommand());
      this.addSubCommand(new LocateRegionCommand());
      this.addSubCommand(new LocatePrefabCommand());
   }
}
