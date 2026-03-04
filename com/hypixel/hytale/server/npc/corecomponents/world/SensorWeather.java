package com.hypixel.hytale.server.npc.corecomponents.world;

import com.hypixel.hytale.common.util.StringUtil;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.corecomponents.world.builders.BuilderSensorWeather;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SensorWeather extends SensorBase {
   @Nullable
   protected final String[] weathers;
   protected int prevWeatherIndex;
   protected boolean cachedResult;

   public SensorWeather(@Nonnull BuilderSensorWeather builder, @Nonnull BuilderSupport support) {
      super(builder);
      this.weathers = builder.getWeathers(support);
   }

   @Override
   public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, double dt, @Nonnull Store<EntityStore> store) {
      if (!super.matches(ref, role, dt, store)) {
         return false;
      } else {
         int weatherIndex = role.getWorldSupport().getCurrentWeatherIndex(store);
         if (weatherIndex == 0) {
            return false;
         } else if (weatherIndex == this.prevWeatherIndex) {
            return this.cachedResult;
         } else {
            String weatherAssetId = Weather.getAssetMap().getAsset(weatherIndex).getId();
            this.prevWeatherIndex = weatherIndex;
            this.cachedResult = this.matchesWeather(weatherAssetId);
            return this.cachedResult;
         }
      }
   }

   @Override
   public InfoProvider getSensorInfo() {
      return null;
   }

   protected boolean matchesWeather(@Nullable String weather) {
      if (weather == null) {
         return false;
      } else {
         for (String weatherMatcher : this.weathers) {
            if (StringUtil.isGlobMatching(weatherMatcher, weather)) {
               return true;
            }
         }

         return false;
      }
   }
}
