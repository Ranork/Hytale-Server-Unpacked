package com.hypixel.hytale.server.core.modules.prefabspawner.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class PrefabSpawnerCommand extends AbstractCommandCollection {
   public PrefabSpawnerCommand() {
      super("prefabspawner", "server.commands.prefabspawner.desc");
      this.addAliases("pspawner");
      this.addSubCommand(new PrefabSpawnerGetCommand());
      this.addSubCommand(new PrefabSpawnerSetCommand());
      this.addSubCommand(new PrefabSpawnerWeightCommand());
   }
}
