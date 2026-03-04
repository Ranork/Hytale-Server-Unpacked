package com.hypixel.hytale.server.core.command.commands.debug.server;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class ServerCommand extends AbstractCommandCollection {
   public ServerCommand() {
      super("server", "server.commands.server.desc");
      this.addSubCommand(new ServerStatsCommand());
      this.addSubCommand(new ServerGCCommand());
      this.addSubCommand(new ServerDumpCommand());
   }
}
