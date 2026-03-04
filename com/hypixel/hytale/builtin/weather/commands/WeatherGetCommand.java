package com.hypixel.hytale.builtin.weather.commands;

import com.hypixel.hytale.builtin.weather.resources.WeatherResource;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class WeatherGetCommand extends AbstractWorldCommand {
   public WeatherGetCommand() {
      super("get", "server.commands.weather.get.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      WeatherResource weatherResource = store.getResource(WeatherResource.getResourceType());
      int forcedWeatherIndex = weatherResource.getForcedWeatherIndex();
      String weatherId;
      if (forcedWeatherIndex != 0) {
         Weather weatherAsset = Weather.getAssetMap().getAsset(forcedWeatherIndex);
         weatherId = weatherAsset.getId();
      } else {
         weatherId = "not locked";
      }

      context.sendMessage(Message.translation("server.commands.weather.get.getForcedWeather").param("worldName", world.getName()).param("weather", weatherId));
   }
}
