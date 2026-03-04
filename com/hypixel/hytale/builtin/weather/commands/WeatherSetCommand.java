package com.hypixel.hytale.builtin.weather.commands;

import com.hypixel.hytale.builtin.weather.resources.WeatherResource;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.arguments.system.RequiredArg;
import com.hypixel.hytale.server.core.command.system.arguments.types.ArgTypes;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.WorldConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WeatherSetCommand extends AbstractWorldCommand {
   @Nonnull
   private final RequiredArg<Weather> weatherArg = this.withRequiredArg("weather", "server.commands.weather.set.weather.desc", ArgTypes.WEATHER_ASSET);

   public WeatherSetCommand() {
      super("set", "server.commands.weather.set.desc");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      Weather weather = this.weatherArg.get(context);
      String weatherName = weather.getId();
      setForcedWeather(world, weatherName, store);
      context.sendMessage(Message.translation("server.commands.weather.set.forcedWeatherSet").param("worldName", world.getName()).param("weather", weatherName));
   }

   protected static void setForcedWeather(@Nonnull World world, @Nullable String forcedWeather, ComponentAccessor<EntityStore> componentAccessor) {
      WeatherResource weatherResource = componentAccessor.getResource(WeatherResource.getResourceType());
      weatherResource.setForcedWeather(forcedWeather);
      WorldConfig config = world.getWorldConfig();
      config.setForcedWeather(forcedWeather);
      config.markChanged();
   }
}
