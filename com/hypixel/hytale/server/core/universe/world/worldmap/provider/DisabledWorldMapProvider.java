package com.hypixel.hytale.server.core.universe.world.worldmap.provider;

import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.map.WorldMap;
import com.hypixel.hytale.server.core.universe.world.worldmap.IWorldMap;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapLoadException;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapSettings;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Nonnull;

public class DisabledWorldMapProvider implements IWorldMapProvider {
   public static final String ID = "Disabled";
   public static final BuilderCodec<DisabledWorldMapProvider> CODEC = BuilderCodec.builder(DisabledWorldMapProvider.class, DisabledWorldMapProvider::new)
      .build();

   @Nonnull
   @Override
   public IWorldMap getGenerator(World world) throws WorldMapLoadException {
      return DisabledWorldMapProvider.DisabledWorldMap.INSTANCE;
   }

   @Nonnull
   @Override
   public String toString() {
      return "DisabledWorldMapProvider{}";
   }

   static class DisabledWorldMap implements IWorldMap {
      public static final IWorldMap INSTANCE = new DisabledWorldMapProvider.DisabledWorldMap();

      @Nonnull
      @Override
      public WorldMapSettings getWorldMapSettings() {
         return WorldMapSettings.DISABLED;
      }

      @Nonnull
      @Override
      public CompletableFuture<WorldMap> generate(World world, int imageWidth, int imageHeight, LongSet chunksToGenerate) {
         return CompletableFuture.completedFuture(new WorldMap(0));
      }

      @Nonnull
      @Override
      public CompletableFuture<Map<String, MapMarker>> generatePointsOfInterest(World world) {
         return CompletableFuture.completedFuture(Collections.emptyMap());
      }
   }
}
