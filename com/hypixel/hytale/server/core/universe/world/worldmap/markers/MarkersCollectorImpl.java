package com.hypixel.hytale.server.core.universe.world.worldmap.markers;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.WorldMapTracker;
import java.util.function.Predicate;
import javax.annotation.Nullable;

class MarkersCollectorImpl implements MarkersCollector {
   private final MapMarkerTracker tracker;
   private final int chunkViewRadius;
   private final int playerChunkX;
   private final int playerChunkZ;

   public MarkersCollectorImpl(MapMarkerTracker tracker, int chunkViewRadius, int playerChunkX, int playerChunkZ) {
      this.tracker = tracker;
      this.chunkViewRadius = chunkViewRadius;
      this.playerChunkX = playerChunkX;
      this.playerChunkZ = playerChunkZ;
   }

   public int getChunkViewRadius() {
      return this.chunkViewRadius;
   }

   public int getPlayerChunkX() {
      return this.playerChunkX;
   }

   public int getPlayerChunkZ() {
      return this.playerChunkZ;
   }

   @Override
   public boolean isInViewDistance(double x, double z) {
      return WorldMapTracker.shouldBeVisible(this.chunkViewRadius, MathUtil.floor(x) >> 5, MathUtil.floor(z) >> 5, this.playerChunkX, this.playerChunkZ);
   }

   @Override
   public void add(MapMarker marker) {
      Position position = marker.transform.position;
      if (this.isInViewDistance(position.x, position.z)) {
         this.tracker.sendMapMarker(marker);
      }
   }

   @Override
   public void addIgnoreViewDistance(MapMarker marker) {
      this.tracker.sendMapMarker(marker);
   }

   @Nullable
   @Override
   public Predicate<PlayerRef> getPlayerMapFilter() {
      return this.tracker.getPlayerMapFilter();
   }
}
