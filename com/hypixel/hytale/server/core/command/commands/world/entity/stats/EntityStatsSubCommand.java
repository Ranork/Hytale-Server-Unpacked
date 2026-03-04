package com.hypixel.hytale.server.core.command.commands.world.entity.stats;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class EntityStatsSubCommand extends AbstractCommandCollection {
   public EntityStatsSubCommand() {
      super("stats", "server.commands.entity.stats.desc");
      this.addAliases("stat");
      this.addSubCommand(new EntityStatsDumpCommand());
      this.addSubCommand(new EntityStatsGetCommand());
      this.addSubCommand(new EntityStatsSetCommand());
      this.addSubCommand(new EntityStatsSetToMaxCommand());
      this.addSubCommand(new EntityStatsResetCommand());
      this.addSubCommand(new EntityStatsAddCommand());
   }
}
