package com.hypixel.hytale.server.core.command.commands.debug.packs;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class PacksCommand extends AbstractCommandCollection {
   public PacksCommand() {
      super("packs", "server.commands.packs.desc");
      this.addSubCommand(new PacksListCommand());
   }
}
