package com.hypixel.hytale.server.core.command.commands.utility.lighting;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class LightingCommand extends AbstractCommandCollection {
   public LightingCommand() {
      super("lighting", "server.commands.lighting.desc");
      this.addAliases("light");
      this.addSubCommand(new LightingCalculationCommand());
      this.addSubCommand(new LightingGetCommand());
      this.addSubCommand(new LightingInvalidateCommand());
      this.addSubCommand(new LightingInfoCommand());
      this.addSubCommand(new LightingSendCommand());
   }
}
