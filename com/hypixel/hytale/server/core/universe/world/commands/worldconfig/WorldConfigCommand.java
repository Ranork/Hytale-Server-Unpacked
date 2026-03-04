package com.hypixel.hytale.server.core.universe.world.commands.worldconfig;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class WorldConfigCommand extends AbstractCommandCollection {
   public WorldConfigCommand() {
      super("config", "server.commands.world.config.desc");
      this.addSubCommand(new WorldConfigPauseTimeCommand());
      this.addSubCommand(new WorldConfigSeedCommand());
      this.addSubCommand(new WorldConfigSetPvpCommand());
      this.addSubCommand(new WorldConfigSetSpawnCommand());
   }
}
