package com.hypixel.hytale.server.core.universe.world.worldmap.markers.providers;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MarkersCollector;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.worldstore.WorldMarkersResource;
import javax.annotation.Nonnull;

public class SharedMarkersProvider implements WorldMapManager.MarkerProvider {
   public static final SharedMarkersProvider INSTANCE = new SharedMarkersProvider();

   private SharedMarkersProvider() {
   }

   @Override
   public void update(@Nonnull World world, @Nonnull Player player, @Nonnull MarkersCollector collector) {
      WorldMarkersResource worldMarkersResource = world.getChunkStore().getStore().getResource(WorldMarkersResource.getResourceType());

      for (UserMapMarker userMapMarker : worldMarkersResource.getUserMapMarkers()) {
         collector.add(userMapMarker.toProtocolMarker());
      }
   }
}
