package com.hypixel.hytale.server.spawning.world.component;

import com.hypixel.hytale.component.Component;
import com.hypixel.hytale.component.ComponentType;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.world.ChunkEnvironmentSpawnData;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import javax.annotation.Nonnull;

public class ChunkSpawnData implements Component<ChunkStore> {
   private final Int2ObjectMap<ChunkEnvironmentSpawnData> chunkEnvironmentSpawnDataMap = new Int2ObjectOpenHashMap();
   private boolean started;
   private long lastSpawn;

   public static ComponentType<ChunkStore, ChunkSpawnData> getComponentType() {
      return SpawningPlugin.get().getChunkSpawnDataComponentType();
   }

   @Nonnull
   public Int2ObjectMap<ChunkEnvironmentSpawnData> getChunkEnvironmentSpawnDataMap() {
      return this.chunkEnvironmentSpawnDataMap;
   }

   public boolean isStarted() {
      return this.started;
   }

   public void setStarted(boolean started) {
      this.started = started;
   }

   public void setLastSpawn(long lastSpawn) {
      this.lastSpawn = lastSpawn;
   }

   public long getLastSpawn() {
      return this.lastSpawn;
   }

   @Override
   public Component<ChunkStore> clone() {
      throw new UnsupportedOperationException("Not implemented!");
   }

   @Nonnull
   public ChunkEnvironmentSpawnData getEnvironmentSpawnData(int environment) {
      ChunkEnvironmentSpawnData chunkEnvironmentSpawnData = (ChunkEnvironmentSpawnData)this.chunkEnvironmentSpawnDataMap.get(environment);
      if (chunkEnvironmentSpawnData == null) {
         throw new NullPointerException("Failed to get environment data for chunk");
      } else {
         return chunkEnvironmentSpawnData;
      }
   }

   public boolean isOnSpawnCooldown() {
      return this.lastSpawn != 0L;
   }
}
