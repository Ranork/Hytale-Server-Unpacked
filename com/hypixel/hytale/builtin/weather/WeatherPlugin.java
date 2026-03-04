package com.hypixel.hytale.builtin.weather;

import com.hypixel.hytale.builtin.weather.commands.WeatherCommand;
import com.hypixel.hytale.builtin.weather.components.WeatherTracker;
import com.hypixel.hytale.builtin.weather.resources.WeatherResource;
import com.hypixel.hytale.builtin.weather.systems.WeatherSystem;
import com.hypixel.hytale.component.ComponentRegistryProxy;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class WeatherPlugin extends JavaPlugin {
   private static WeatherPlugin instance;
   private ComponentType<EntityStore, WeatherTracker> weatherTrackerComponentType;
   private ResourceType<EntityStore, WeatherResource> weatherResourceType;

   public static WeatherPlugin get() {
      return instance;
   }

   public WeatherPlugin(@Nonnull JavaPluginInit init) {
      super(init);
      instance = this;
   }

   @Override
   protected void setup() {
      ComponentRegistryProxy<EntityStore> entityStoreRegistry = this.getEntityStoreRegistry();
      this.weatherResourceType = EntityStore.REGISTRY.registerResource(WeatherResource.class, WeatherResource::new);
      this.weatherTrackerComponentType = EntityStore.REGISTRY.registerComponent(WeatherTracker.class, WeatherTracker::new);
      entityStoreRegistry.registerSystem(new WeatherSystem.WorldAddedSystem());
      entityStoreRegistry.registerSystem(new WeatherSystem.PlayerAddedSystem());
      entityStoreRegistry.registerSystem(new WeatherSystem.TickingSystem());
      entityStoreRegistry.registerSystem(new WeatherSystem.InvalidateWeatherAfterTeleport());
      CommandManager.get().registerSystemCommand(new WeatherCommand());
   }

   @Nonnull
   public ComponentType<EntityStore, WeatherTracker> getWeatherTrackerComponentType() {
      return this.weatherTrackerComponentType;
   }

   @Nonnull
   public ResourceType<EntityStore, WeatherResource> getWeatherResourceType() {
      return this.weatherResourceType;
   }
}
