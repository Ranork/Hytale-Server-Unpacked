package com.hypixel.hytale.server.core.universe.world.worldmap.provider.chunk;

import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.protocol.packets.worldmap.UpdateWorldMapSettings;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.map.WorldMap;
import com.hypixel.hytale.server.core.universe.world.worldmap.IWorldMap;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapSettings;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class ChunkWorldMap implements IWorldMap {
   public static final ChunkWorldMap INSTANCE = new ChunkWorldMap();

   @Nonnull
   @Override
   public WorldMapSettings getWorldMapSettings() {
      UpdateWorldMapSettings settingsPacket = new UpdateWorldMapSettings();
      settingsPacket.defaultScale = 128.0F;
      settingsPacket.minScale = 32.0F;
      settingsPacket.maxScale = 175.0F;
      return new WorldMapSettings(null, 3.0F, 2.0F, 3, 32, settingsPacket);
   }

   @Nonnull
   @Override
   public CompletableFuture<WorldMap> generate(World world, int imageWidth, int imageHeight, @Nonnull LongSet chunksToGenerate) {
      CompletableFuture<ImageBuilder>[] futures = new CompletableFuture[chunksToGenerate.size()];
      int futureIndex = 0;
      LongIterator iterator = chunksToGenerate.iterator();

      while (iterator.hasNext()) {
         futures[futureIndex++] = ImageBuilder.build(iterator.nextLong(), imageWidth, imageHeight, world);
      }

      return CompletableFuture.allOf(futures).thenApply(unused -> {
         WorldMap worldMap = new WorldMap(futures.length);

         for (CompletableFuture<ImageBuilder> future : futures) {
            ImageBuilder builder = future.getNow(null);
            if (builder != null) {
               worldMap.getChunks().put(builder.getIndex(), builder.getImage());
            }
         }

         return worldMap;
      });
   }

   @Nonnull
   @Override
   public CompletableFuture<Map<String, MapMarker>> generatePointsOfInterest(World world) {
      return CompletableFuture.completedFuture(Collections.emptyMap());
   }
}
