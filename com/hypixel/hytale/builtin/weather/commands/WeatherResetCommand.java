package com.hypixel.hytale.builtin.weather.commands;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractWorldCommand;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class WeatherResetCommand extends AbstractWorldCommand {
   public WeatherResetCommand() {
      super("reset", "server.commands.weather.reset.desc");
      this.addAliases("clear");
   }

   @Override
   protected void execute(@Nonnull CommandContext context, @Nonnull World world, @Nonnull Store<EntityStore> store) {
      WeatherSetCommand.setForcedWeather(world, null, store);
      context.sendMessage(Message.translation("server.commands.weather.reset.forcedWeatherReset").param("worldName", world.getName()));
   }
}
