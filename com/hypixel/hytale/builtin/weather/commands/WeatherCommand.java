package com.hypixel.hytale.builtin.weather.commands;

import com.hypixel.hytale.server.core.command.system.basecommands.AbstractCommandCollection;

public class WeatherCommand extends AbstractCommandCollection {
   public WeatherCommand() {
      super("weather", "server.commands.weather.desc");
      this.setPermissionGroups("hytale:WorldEditor");
      this.addSubCommand(new WeatherSetCommand());
      this.addSubCommand(new WeatherGetCommand());
      this.addSubCommand(new WeatherResetCommand());
   }
}
