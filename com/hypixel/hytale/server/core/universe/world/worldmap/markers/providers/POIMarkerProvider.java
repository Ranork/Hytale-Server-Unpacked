package com.hypixel.hytale.server.core.universe.world.worldmap.markers.providers;

import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MarkersCollector;
import java.util.Map;
import javax.annotation.Nonnull;

public class POIMarkerProvider implements WorldMapManager.MarkerProvider {
   public static final POIMarkerProvider INSTANCE = new POIMarkerProvider();

   private POIMarkerProvider() {
   }

   @Override
   public void update(@Nonnull World world, @Nonnull Player player, @Nonnull MarkersCollector collector) {
      Map<String, MapMarker> globalMarkers = world.getWorldMapManager().getPointsOfInterest();
      if (!globalMarkers.isEmpty()) {
         for (MapMarker marker : globalMarkers.values()) {
            collector.add(marker);
         }
      }
   }
}
