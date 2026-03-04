package com.hypixel.hytale.builtin.buildertools.prefabeditor.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class PrefabEditCommand extends AbstractCommandCollection {
   public PrefabEditCommand() {
      super("editprefab", "server.commands.editprefab.desc");
      this.addAliases("prefabedit", "pedit");
      this.addSubCommand(new PrefabEditExitCommand());
      this.addSubCommand(new PrefabEditLoadCommand());
      this.addSubCommand(new PrefabEditCreateNewCommand());
      this.addSubCommand(new PrefabEditSelectCommand());
      this.addSubCommand(new PrefabEditSaveCommand());
      this.addSubCommand(new PrefabEditSaveUICommand());
      this.addSubCommand(new PrefabEditKillEntitiesCommand());
      this.addSubCommand(new PrefabEditSaveAsCommand());
      this.addSubCommand(new PrefabEditUpdateBoxCommand());
      this.addSubCommand(new PrefabEditInfoCommand());
      this.addSubCommand(new PrefabEditTeleportCommand());
      this.addSubCommand(new PrefabEditModifiedCommand());
   }
}
