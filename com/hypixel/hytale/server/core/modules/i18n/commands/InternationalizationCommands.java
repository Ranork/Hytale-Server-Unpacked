package com.hypixel.hytale.server.core.modules.i18n.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class InternationalizationCommands extends AbstractCommandCollection {
   public InternationalizationCommands() {
      super("lang", "server.commands.i18n.desc");
      this.addAliases("internationalization", "il8n", "translation");
      this.addSubCommand(new GenerateI18nCommand());
   }
}
