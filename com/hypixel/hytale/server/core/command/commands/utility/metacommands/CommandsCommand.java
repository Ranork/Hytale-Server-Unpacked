package com.hypixel.hytale.server.core.command.commands.utility.metacommands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class CommandsCommand extends AbstractCommandCollection {
   public CommandsCommand() {
      super("commands", "server.commands.meta.desc");
      this.addSubCommand(new DumpCommandsCommand());
   }
}
