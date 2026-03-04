package com.hypixel.hytale.builtin.portals.integrations;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.spawn.IndividualSpawnProvider;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MapMarkerBuilder;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MarkersCollector;
import javax.annotation.Nonnull;

public class PortalMarkerProvider implements WorldMapManager.MarkerProvider {
   public static final PortalMarkerProvider INSTANCE = new PortalMarkerProvider();

   @Override
   public void update(@Nonnull World world, @Nonnull Player player, @Nonnull MarkersCollector collector) {
      if (world.getWorldConfig().getSpawnProvider() instanceof IndividualSpawnProvider individualSpawnProvider) {
         Transform spawnPoint = individualSpawnProvider.getFirstSpawnPoint();
         if (spawnPoint != null) {
            MapMarker marker = new MapMarkerBuilder("Portal", "Portal.png", spawnPoint).withName(Message.translation("server.portals.exit.marker")).build();
            collector.add(marker);
         }
      }
   }
}
