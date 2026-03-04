package com.hypixel.hytale.server.core.command.commands.world.worldgen;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class WorldGenCommand extends AbstractCommandCollection {
   public WorldGenCommand() {
      super("worldgen", "server.commands.worldgen.desc");
      this.addAliases("wg");
      this.addSubCommand(new WorldGenBenchmarkCommand());
      this.addSubCommand(new WorldGenReloadCommand());
   }
}
