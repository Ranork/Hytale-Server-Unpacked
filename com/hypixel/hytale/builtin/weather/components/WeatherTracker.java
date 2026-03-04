package com.hypixel.hytale.builtin.weather.components;

import com.hypixel.hytale.builtin.weather.WeatherPlugin;
import com.hypixel.hytale.builtin.weather.resources.WeatherResource;
import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.world.UpdateWeather;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class WeatherTracker implements Component<EntityStore> {
   private final UpdateWeather updateWeather = new UpdateWeather(0, 10.0F);
   private final Vector3i previousBlockPosition = new Vector3i();
   private int environmentId;
   private boolean firstSendForWorld = true;

   public static ComponentType<EntityStore, WeatherTracker> getComponentType() {
      return WeatherPlugin.get().getWeatherTrackerComponentType();
   }

   public WeatherTracker() {
   }

   private WeatherTracker(@Nonnull WeatherTracker other) {
      this.environmentId = other.environmentId;
      this.updateWeather.weatherIndex = other.updateWeather.weatherIndex;
      this.previousBlockPosition.assign(other.previousBlockPosition);
   }

   public void updateWeather(
      @Nonnull PlayerRef playerRef,
      @Nonnull WeatherResource weatherComponent,
      @Nonnull TransformComponent transformComponent,
      float transitionSeconds,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      int forcedWeatherIndex = weatherComponent.getForcedWeatherIndex();
      if (forcedWeatherIndex != 0) {
         this.sendWeatherIndex(playerRef, forcedWeatherIndex, transitionSeconds);
      } else {
         this.updateEnvironment(transformComponent, componentAccessor);
         int weatherIndexForEnvironment = weatherComponent.getWeatherIndexForEnvironment(this.environmentId);
         this.sendWeatherIndex(playerRef, weatherIndexForEnvironment, transitionSeconds);
      }
   }

   public void sendWeatherIndex(@Nonnull PlayerRef playerRef, int weatherIndex, float transitionSeconds) {
      if (weatherIndex == Integer.MIN_VALUE) {
         weatherIndex = 0;
      }

      if (this.updateWeather.weatherIndex != weatherIndex) {
         this.updateWeather.weatherIndex = weatherIndex;
         this.updateWeather.transitionSeconds = transitionSeconds;
         playerRef.getPacketHandler().write(this.updateWeather);
      }
   }

   public boolean consumeFirstSendForWorld() {
      if (this.firstSendForWorld) {
         this.firstSendForWorld = false;
         return true;
      } else {
         return false;
      }
   }

   public void clear() {
      this.updateWeather.weatherIndex = 0;
      this.firstSendForWorld = true;
   }

   public void updateEnvironment(@Nonnull TransformComponent transformComponent, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      Vector3d vector = transformComponent.getPosition();
      int blockX = MathUtil.floor(vector.getX());
      int blockY = MathUtil.floor(vector.getY());
      int blockZ = MathUtil.floor(vector.getZ());
      if (this.previousBlockPosition.getX() != blockX || this.previousBlockPosition.getY() != blockY || this.previousBlockPosition.getZ() != blockZ) {
         Ref<ChunkStore> chunkRef = transformComponent.getChunkRef();
         if (chunkRef == null || !chunkRef.isValid()) {
            return;
         }

         World world = componentAccessor.getExternalData().getWorld();
         Store<ChunkStore> chunkStore = world.getChunkStore().getStore();
         BlockChunk blockChunkComponent = chunkStore.getComponent(chunkRef, BlockChunk.getComponentType());

         assert blockChunkComponent != null;

         int y = MathUtil.clamp(blockY, 0, 319);
         this.environmentId = blockChunkComponent.getEnvironment(blockX, y, blockZ);
      }

      this.previousBlockPosition.x = blockX;
      this.previousBlockPosition.y = blockY;
      this.previousBlockPosition.z = blockZ;
   }

   public int getEnvironmentId() {
      return this.environmentId;
   }

   public int getWeatherIndex() {
      return this.updateWeather.weatherIndex;
   }

   public void setWeatherIndex(@Nonnull PlayerRef playerRef, int weatherIndex) {
      this.updateWeather.weatherIndex = weatherIndex;
      playerRef.getPacketHandler().write(this.updateWeather);
   }

   @Nonnull
   @Override
   public Component<EntityStore> clone() {
      return new WeatherTracker(this);
   }
}
