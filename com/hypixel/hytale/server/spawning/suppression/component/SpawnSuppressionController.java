package com.hypixel.hytale.server.spawning.suppression.component;

import com.hypixel.fastutil.longs.Long2ObjectConcurrentHashMap;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.map.ObjectMapCodec;
import com.hypixel.hytale.component.Resource;
import com.hypixel.hytale.component.ResourceType;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.spawning.SpawningPlugin;
import com.hypixel.hytale.server.spawning.suppression.SpawnSuppressorEntry;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nonnull;

public class SpawnSuppressionController implements Resource<EntityStore> {
   public static final BuilderCodec<SpawnSuppressionController> CODEC = BuilderCodec.builder(SpawnSuppressionController.class, SpawnSuppressionController::new)
      .append(
         new KeyedCodec<>(
            "SpawnSuppressorMap", new ObjectMapCodec<>(SpawnSuppressorEntry.CODEC, ConcurrentHashMap::new, UUID::toString, UUID::fromString, false)
         ),
         (spawnSuppressionController, o) -> spawnSuppressionController.spawnSuppressorMap = o,
         spawnSuppressionController -> spawnSuppressionController.spawnSuppressorMap
      )
      .add()
      .build();
   private final Long2ObjectConcurrentHashMap<ChunkSuppressionEntry> chunkSuppressionMap = new Long2ObjectConcurrentHashMap<>(
      true, ChunkUtil.indexChunk(Integer.MIN_VALUE, Integer.MIN_VALUE)
   );
   private Map<UUID, SpawnSuppressorEntry> spawnSuppressorMap = new ConcurrentHashMap<>();

   public static ResourceType<EntityStore, SpawnSuppressionController> getResourceType() {
      return SpawningPlugin.get().getSpawnSuppressionControllerResourceType();
   }

   public Map<UUID, SpawnSuppressorEntry> getSpawnSuppressorMap() {
      return this.spawnSuppressorMap;
   }

   @Nonnull
   public Long2ObjectConcurrentHashMap<ChunkSuppressionEntry> getChunkSuppressionMap() {
      return this.chunkSuppressionMap;
   }

   @Nonnull
   @Override
   public Resource<EntityStore> clone() {
      SpawnSuppressionController controller = new SpawnSuppressionController();
      controller.chunkSuppressionMap.putAll(this.chunkSuppressionMap);
      controller.spawnSuppressorMap.putAll(this.spawnSuppressorMap);
      return controller;
   }
}
