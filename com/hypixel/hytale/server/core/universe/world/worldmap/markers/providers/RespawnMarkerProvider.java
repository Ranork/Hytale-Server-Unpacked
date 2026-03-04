package com.hypixel.hytale.server.core.universe.world.worldmap.markers.providers;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.asset.type.gameplay.WorldMapConfig;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerRespawnPointData;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MapMarkerBuilder;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MarkersCollector;
import javax.annotation.Nonnull;

public class RespawnMarkerProvider implements WorldMapManager.MarkerProvider {
   public static final RespawnMarkerProvider INSTANCE = new RespawnMarkerProvider();

   private RespawnMarkerProvider() {
   }

   @Override
   public void update(@Nonnull World world, @Nonnull Player player, @Nonnull MarkersCollector collector) {
      WorldMapConfig worldMapConfig = world.getGameplayConfig().getWorldMapConfig();
      if (worldMapConfig.isDisplayHome()) {
         PlayerRespawnPointData[] respawnPoints = player.getPlayerConfigData().getPerWorldData(world.getName()).getRespawnPoints();
         if (respawnPoints != null) {
            for (int i = 0; i < respawnPoints.length; i++) {
               addRespawnMarker(collector, respawnPoints[i], i);
            }
         }
      }
   }

   private static void addRespawnMarker(MarkersCollector collector, PlayerRespawnPointData respawnPoint, int index) {
      String respawnPointName = respawnPoint.getName();
      Vector3i respawnPointPosition = respawnPoint.getBlockPosition();
      String markerId = respawnPointName + index;
      MapMarker marker = new MapMarkerBuilder(markerId, "Home.png", new Transform(respawnPointPosition)).withCustomName(respawnPointName).build();
      collector.addIgnoreViewDistance(marker);
   }
}
