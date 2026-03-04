package com.hypixel.hytale.server.core.command.commands.utility.worldmap;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class WorldMapCommand extends AbstractCommandCollection {
   public WorldMapCommand() {
      super("worldmap", "server.commands.worldmap.desc");
      this.addAliases("map");
      this.addSubCommand(new WorldMapReloadCommand());
      this.addSubCommand(new WorldMapDiscoverCommand());
      this.addSubCommand(new WorldMapUndiscoverCommand());
      this.addSubCommand(new WorldMapClearMarkersCommand());
      this.addSubCommand(new WorldMapViewRadiusSubCommand());
   }
}
