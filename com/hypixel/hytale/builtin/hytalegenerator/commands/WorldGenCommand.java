package com.hypixel.hytale.builtin.hytalegenerator.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class WorldGenCommand extends AbstractCommandCollection {
   public WorldGenCommand() {
      super("worldgen2", "server.commands.worldgen2.desc");
      this.addAliases("wg2");
      this.addSubCommand(new CreateCommand());
   }
}
