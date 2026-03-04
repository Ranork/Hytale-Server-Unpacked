package com.hypixel.hytale.builtin.weather.systems;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.builtin.weather.components.WeatherTracker;
import com.hypixel.hytale.builtin.weather.resources.WeatherResource;
import com.hypixel.hytale.common.map.IWeightedMap;
import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.Archetype;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.Order;
import com.hypixel.hytale.component.dependency.SystemDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.HolderSystem;
import com.hypixel.hytale.component.system.RefChangeSystem;
import com.hypixel.hytale.component.system.StoreSystem;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.asset.type.environment.config.WeatherForecast;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.teleport.Teleport;
import com.hypixel.hytale.server.core.modules.entity.teleport.TeleportSystems;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WeatherSystem {
   private static final float JOIN_TRANSITION_SECONDS = 0.5F;
   private static final float WEATHERCHANGE_TRANSITION_SECONDS = 10.0F;

   public static class InvalidateWeatherAfterTeleport extends RefChangeSystem<EntityStore, Teleport> {
      @Nonnull
      private static final Query<EntityStore> QUERY = WeatherTracker.getComponentType();
      @Nonnull
      private static final Set<Dependency<EntityStore>> DEPENDENCIES = Set.of(new SystemDependency<>(Order.AFTER, TeleportSystems.PlayerMoveSystem.class));

      @Nonnull
      @Override
      public ComponentType<EntityStore, Teleport> componentType() {
         return Teleport.getComponentType();
      }

      public void onComponentAdded(
         @Nonnull Ref<EntityStore> ref, @Nonnull Teleport component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         WeatherTracker weatherTrackerComponent = commandBuffer.getComponent(ref, WeatherTracker.getComponentType());

         assert weatherTrackerComponent != null;

         weatherTrackerComponent.clear();
      }

      public void onComponentSet(
         @Nonnull Ref<EntityStore> ref,
         @Nullable Teleport oldComponent,
         @Nonnull Teleport newComponent,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      public void onComponentRemoved(
         @Nonnull Ref<EntityStore> ref, @Nonnull Teleport component, @Nonnull Store<EntityStore> store, @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
      }

      @Nullable
      @Override
      public Query<EntityStore> getQuery() {
         return QUERY;
      }

      @Nonnull
      @Override
      public Set<Dependency<EntityStore>> getDependencies() {
         return DEPENDENCIES;
      }
   }

   public static class PlayerAddedSystem extends HolderSystem<EntityStore> {
      private static final ComponentType<EntityStore, PlayerRef> PLAYER_REF_COMPONENT_TYPE = PlayerRef.getComponentType();
      private static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
      private static final ComponentType<EntityStore, WeatherTracker> WEATHER_TRACKER_COMPONENT_TYPE = WeatherTracker.getComponentType();
      private static final Query<EntityStore> QUERY = Archetype.of(PLAYER_REF_COMPONENT_TYPE, TRANSFORM_COMPONENT_TYPE);

      @Override
      public Query<EntityStore> getQuery() {
         return QUERY;
      }

      @Override
      public void onEntityAdd(@Nonnull Holder<EntityStore> holder, @Nonnull AddReason reason, @Nonnull Store<EntityStore> store) {
         holder.ensureComponent(WEATHER_TRACKER_COMPONENT_TYPE);
      }

      @Override
      public void onEntityRemoved(@Nonnull Holder<EntityStore> holder, @Nonnull RemoveReason reason, @Nonnull Store<EntityStore> store) {
         WeatherTracker weatherTrackerComponent = holder.ensureAndGetComponent(WEATHER_TRACKER_COMPONENT_TYPE);
         weatherTrackerComponent.clear();
      }
   }

   public static class TickingSystem extends EntityTickingSystem<EntityStore> {
      private static final ComponentType<EntityStore, PlayerRef> PLAYER_REF_COMPONENT_TYPE = PlayerRef.getComponentType();
      private static final ComponentType<EntityStore, TransformComponent> TRANSFORM_COMPONENT_TYPE = TransformComponent.getComponentType();
      private static final ComponentType<EntityStore, WeatherTracker> WEATHER_TRACKER_COMPONENT_TYPE = WeatherTracker.getComponentType();
      private static final ResourceType<EntityStore, WeatherResource> WEATHER_RESOURCE_TYPE = WeatherResource.getResourceType();
      private static final Query<EntityStore> QUERY = Archetype.of(PLAYER_REF_COMPONENT_TYPE, TRANSFORM_COMPONENT_TYPE, WEATHER_TRACKER_COMPONENT_TYPE);

      @Override
      public Query<EntityStore> getQuery() {
         return QUERY;
      }

      @Override
      public void tick(float dt, int systemIndex, @Nonnull Store<EntityStore> store) {
         WeatherResource weatherResource = store.getResource(WEATHER_RESOURCE_TYPE);
         if (weatherResource.consumeForcedWeatherChange()) {
            weatherResource.playerUpdateDelay = 1.0F;
            store.tick(this, dt, systemIndex);
         } else {
            if (weatherResource.getForcedWeatherIndex() == 0) {
               WorldTimeResource worldTimeResource = store.getResource(WorldTimeResource.getResourceType());
               int currentHour = worldTimeResource.getCurrentHour();
               if (weatherResource.compareAndSwapHour(currentHour)) {
                  Int2IntMap environmentWeather = weatherResource.getEnvironmentWeather();
                  ThreadLocalRandom random = ThreadLocalRandom.current();
                  IndexedLookupTableAssetMap<String, Environment> assetMap = Environment.getAssetMap();

                  for (Entry<String, Environment> entry : assetMap.getAssetMap().entrySet()) {
                     String key = entry.getKey();
                     int index = assetMap.getIndex(key);
                     if (index == Integer.MIN_VALUE) {
                        throw new IllegalArgumentException("Unknown key! " + key);
                     }

                     IWeightedMap<WeatherForecast> weatherForecast = entry.getValue().getWeatherForecast(currentHour);
                     int selectedWeatherIndex = weatherForecast.get(random).getWeatherIndex();
                     environmentWeather.put(index, selectedWeatherIndex);
                  }
               }
            }

            weatherResource.playerUpdateDelay -= dt;
            if (weatherResource.playerUpdateDelay <= 0.0F) {
               weatherResource.playerUpdateDelay = 1.0F;
               store.tick(this, dt, systemIndex);
            }
         }
      }

      @Override
      public boolean isParallel(int archetypeChunkSize, int taskCount) {
         return false;
      }

      @Override
      public void tick(
         float dt,
         int index,
         @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
         @Nonnull Store<EntityStore> store,
         @Nonnull CommandBuffer<EntityStore> commandBuffer
      ) {
         WeatherResource weatherResource = store.getResource(WEATHER_RESOURCE_TYPE);
         PlayerRef playerRefComponent = archetypeChunk.getComponent(index, PLAYER_REF_COMPONENT_TYPE);

         assert playerRefComponent != null;

         TransformComponent transformComponent = archetypeChunk.getComponent(index, TRANSFORM_COMPONENT_TYPE);

         assert transformComponent != null;

         WeatherTracker weatherTrackerComponent = archetypeChunk.getComponent(index, WEATHER_TRACKER_COMPONENT_TYPE);

         assert weatherTrackerComponent != null;

         float transitionSeconds = weatherTrackerComponent.consumeFirstSendForWorld() ? 0.5F : 10.0F;
         weatherTrackerComponent.updateWeather(playerRefComponent, weatherResource, transformComponent, transitionSeconds, commandBuffer);
      }
   }

   public static class WorldAddedSystem extends StoreSystem<EntityStore> {
      @Nonnull
      private final ResourceType<EntityStore, WeatherResource> weatherResourceType = WeatherResource.getResourceType();

      @Override
      public void onSystemAddedToStore(@Nonnull Store<EntityStore> store) {
         String forcedWeather = store.getExternalData().getWorld().getWorldConfig().getForcedWeather();
         store.getResource(this.weatherResourceType).setForcedWeather(forcedWeather);
      }

      @Override
      public void onSystemRemovedFromStore(@Nonnull Store<EntityStore> store) {
      }
   }
}
