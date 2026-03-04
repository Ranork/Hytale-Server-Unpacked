package com.hypixel.hytale.server.core.command.commands.debug.server;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class ServerStatsCommand extends AbstractCommandCollection {
   public ServerStatsCommand() {
      super("stats", "server.commands.server.stats.desc");
      this.addSubCommand(new ServerStatsCpuCommand());
      this.addSubCommand(new ServerStatsMemoryCommand());
      this.addSubCommand(new ServerStatsGcCommand());
   }
}
