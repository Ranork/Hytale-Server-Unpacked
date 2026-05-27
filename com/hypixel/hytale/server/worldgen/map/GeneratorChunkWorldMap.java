package com.hypixel.hytale.server.worldgen.map;

import com.hypixel.hytale.logger.sentry.SkipSentryException;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.protocol.packets.worldmap.UpdateWorldMapSettings;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.map.WorldMap;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapSettings;
import com.hypixel.hytale.server.core.universe.world.worldmap.provider.chunk.ChunkWorldMap;
import com.hypixel.hytale.server.worldgen.chunk.ChunkGenerator;
import com.hypixel.hytale.server.worldgen.container.UniquePrefabContainer;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.annotation.Nonnull;

public class GeneratorChunkWorldMap extends ChunkWorldMap {
   private static final WorldMap EMPTY = new WorldMap(0);
   @Nonnull
   private final ChunkGenerator generator;
   @Nonnull
   private final Executor executor;

   public GeneratorChunkWorldMap(@Nonnull ChunkGenerator generator, @Nonnull Executor executor) {
      this.generator = generator;
      this.executor = executor;
   }

   @Nonnull
   @Override
   public CompletableFuture<Map<String, MapMarker>> generatePointsOfInterest(@Nonnull World world) {
      int seed = (int)world.getWorldConfig().getSeed();
      UniquePrefabContainer.UniquePrefabEntry[] uniquePrefabs = this.generator.getUniquePrefabs(seed);
      return uniquePrefabs != null && uniquePrefabs.length != 0 ? CompletableFuture.<Map<String, MapMarker>>supplyAsync(() -> {
         WorldMap worldMap = new WorldMap(0);

         for (UniquePrefabContainer.UniquePrefabEntry entry : uniquePrefabs) {
            if (!entry.isSpawnLocation() && entry.isShowOnMap()) {
               worldMap.addPointOfInterest("UniquePrefab-" + entry.getName() + "-" + entry.getPosition(), entry.getName(), "Prefab.png", entry.getPosition());
            }
         }

         return worldMap.getPointsOfInterest();
      }, this.executor).exceptionally(t -> {
         throw new SkipSentryException(t);
      }) : CompletableFuture.completedFuture(EMPTY.getPointsOfInterest());
   }

   @Nonnull
   @Override
   public WorldMapSettings getWorldMapSettings() {
      UpdateWorldMapSettings settingsPacket = new UpdateWorldMapSettings();
      settingsPacket.defaultScale = 128.0F;
      settingsPacket.minScale = 32.0F;
      settingsPacket.maxScale = 175.0F;
      return new WorldMapSettings(null, 3.0F, 2.0F, 3, 32, settingsPacket);
   }
}
