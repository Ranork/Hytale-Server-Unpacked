package com.hypixel.hytale.server.core.universe.world.worldmap;

import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.map.WorldMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface IWorldMap {
   WorldMapSettings getWorldMapSettings();

   CompletableFuture<WorldMap> generate(World var1, int var2, int var3, LongSet var4);

   CompletableFuture<Map<String, MapMarker>> generatePointsOfInterest(World var1);

   default void shutdown() {
   }
}
