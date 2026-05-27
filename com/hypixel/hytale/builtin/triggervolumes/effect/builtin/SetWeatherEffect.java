package com.hypixel.hytale.builtin.triggervolumes.effect.builtin;

import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerContext;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEffect;
import com.hypixel.hytale.builtin.triggervolumes.effect.TriggerEventType;
import com.hypixel.hytale.builtin.weather.components.WeatherTracker;
import com.hypixel.hytale.builtin.weather.resources.WeatherResource;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.asset.type.weather.config.Weather;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SetWeatherEffect extends TriggerEffect {
   @Nonnull
   public static final BuilderCodec<SetWeatherEffect> CODEC = BuilderCodec.builder(SetWeatherEffect.class, SetWeatherEffect::new, BASE_CODEC)
      .append(new KeyedCodec<>("Weather", Codec.STRING), (e, v) -> e.weatherId = v, e -> e.weatherId)
      .add()
      .append(new KeyedCodec<>("PlayerOnly", Codec.BOOLEAN, false), (e, v) -> e.playerOnly = v, e -> e.playerOnly)
      .add()
      .append(new KeyedCodec<>("ResetWeather", Codec.BOOLEAN, false), (e, v) -> e.resetWeather = v, e -> e.resetWeather)
      .add()
      .build();
   @Nullable
   private String weatherId;
   private boolean playerOnly = true;
   private boolean resetWeather;

   @Override
   public void execute(@Nonnull TriggerContext context) {
      Store<EntityStore> store = context.getStore();
      if (this.playerOnly) {
         PlayerRef playerRef = store.getComponent(context.getEntityRef(), PlayerRef.getComponentType());
         if (playerRef == null) {
            return;
         }

         WeatherTracker tracker = store.getComponent(context.getEntityRef(), WeatherTracker.getComponentType());
         if (tracker == null) {
            return;
         }

         if (context.getEventType() == TriggerEventType.EXIT && this.resetWeather) {
            tracker.clearOverrideWeatherIndex();
            WeatherResource weatherResource = store.getResource(WeatherResource.getResourceType());
            if (weatherResource != null) {
               int idx = weatherResource.getWeatherIndexForEnvironment(tracker.getEnvironmentId());
               tracker.sendWeatherIndex(playerRef, idx, 2.0F);
            }

            return;
         }

         if (this.weatherId == null) {
            return;
         }

         int weatherIndex = Weather.getAssetMap().getIndex(this.weatherId);
         if (weatherIndex == Integer.MIN_VALUE) {
            return;
         }

         tracker.setOverrideWeatherIndex(weatherIndex);
         tracker.sendWeatherIndex(playerRef, weatherIndex, 2.0F);
      } else {
         if (context.getEventType() == TriggerEventType.EXIT && this.resetWeather) {
            if (context.getVolume().getTrackedEntities().isEmpty()) {
               WeatherResource weatherResource = store.getResource(WeatherResource.getResourceType());
               if (weatherResource != null) {
                  weatherResource.setForcedWeather(null);
                  World world = store.getExternalData().getWorld();
                  if (world != null) {
                     world.getWorldConfig().setForcedWeather(null);
                     world.getWorldConfig().markChanged();
                  }
               }
            }

            return;
         }

         if (this.weatherId == null) {
            return;
         }

         int weatherIndex = Weather.getAssetMap().getIndex(this.weatherId);
         if (weatherIndex == Integer.MIN_VALUE) {
            return;
         }

         WeatherResource weatherResource = store.getResource(WeatherResource.getResourceType());
         if (weatherResource == null) {
            return;
         }

         weatherResource.setForcedWeather(this.weatherId);
         World world = store.getExternalData().getWorld();
         if (world != null) {
            world.getWorldConfig().setForcedWeather(this.weatherId);
            world.getWorldConfig().markChanged();
         }
      }
   }
}
