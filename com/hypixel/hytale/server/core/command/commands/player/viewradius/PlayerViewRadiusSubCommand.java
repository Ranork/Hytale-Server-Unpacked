package com.hypixel.hytale.server.core.command.commands.player.viewradius;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class PlayerViewRadiusSubCommand extends AbstractCommandCollection {
   public PlayerViewRadiusSubCommand() {
      super("viewradius", "server.commands.player.viewradius.desc");
      this.addSubCommand(new PlayerViewRadiusGetCommand());
      this.addSubCommand(new PlayerViewRadiusSetCommand());
   }
}
