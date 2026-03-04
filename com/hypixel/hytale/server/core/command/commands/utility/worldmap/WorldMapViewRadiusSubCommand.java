package com.hypixel.hytale.server.core.command.commands.utility.worldmap;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class WorldMapViewRadiusSubCommand extends AbstractCommandCollection {
   public WorldMapViewRadiusSubCommand() {
      super("viewradius", "server.commands.worldmap.viewradius.desc");
      this.addSubCommand(new WorldMapViewRadiusGetCommand());
      this.addSubCommand(new WorldMapViewRadiusSetCommand());
      this.addSubCommand(new WorldMapViewRadiusRemoveCommand());
   }
}
