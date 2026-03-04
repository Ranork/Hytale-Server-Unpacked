package com.hypixel.hytale.builtin.weather.resources;

import com.hypixel.hytale.builtin.weather.WeatherPlugin;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WeatherResource implements Resource<EntityStore> {
   public static final float WEATHER_UPDATE_RATE_S = 1.0F;
   private int forcedWeatherIndex;
   private int previousForcedWeatherIndex;
   @Nonnull
   private final Int2IntMap environmentWeather = new Int2IntOpenHashMap();
   private int previousHour = -1;
   public float playerUpdateDelay;

   public static ResourceType<EntityStore, WeatherResource> getResourceType() {
      return WeatherPlugin.get().getWeatherResourceType();
   }

   public WeatherResource() {
      this.environmentWeather.defaultReturnValue(Integer.MIN_VALUE);
   }

   @Nonnull
   public Int2IntMap getEnvironmentWeather() {
      return this.environmentWeather;
   }

   public int getWeatherIndexForEnvironment(int environmentId) {
      return this.environmentWeather.get(environmentId);
   }

   public int getForcedWeatherIndex() {
      return this.forcedWeatherIndex;
   }

   public void setForcedWeather(@Nullable String forcedWeather) {
      this.previousForcedWeatherIndex = this.forcedWeatherIndex;
      this.forcedWeatherIndex = forcedWeather == null ? 0 : Weather.getAssetMap().getIndex(forcedWeather);
   }

   public boolean consumeForcedWeatherChange() {
      if (this.previousForcedWeatherIndex == this.forcedWeatherIndex) {
         return false;
      } else {
         this.previousForcedWeatherIndex = this.forcedWeatherIndex;
         return true;
      }
   }

   public boolean compareAndSwapHour(int currentHour) {
      if (currentHour == this.previousHour) {
         return false;
      } else {
         this.previousHour = currentHour;
         return true;
      }
   }

   @Nonnull
   @Override
   public Resource<EntityStore> clone() {
      return new WeatherResource();
   }
}
