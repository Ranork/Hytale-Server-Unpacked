package com.hypixel.hytale.server.core.universe.world.worldmap.markers.providers;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.gameplay.WorldMapConfig;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MapMarkerBuilder;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MarkersCollector;
import javax.annotation.Nonnull;

public class SpawnMarkerProvider implements WorldMapManager.MarkerProvider {
   public static final SpawnMarkerProvider INSTANCE = new SpawnMarkerProvider();

   private SpawnMarkerProvider() {
   }

   @Override
   public void update(@Nonnull World world, @Nonnull Player player, @Nonnull MarkersCollector collector) {
      WorldMapConfig worldMapConfig = world.getGameplayConfig().getWorldMapConfig();
      if (worldMapConfig.isDisplaySpawn()) {
         Transform spawnPoint = world.getWorldConfig().getSpawnProvider().getSpawnPoint(player);
         if (spawnPoint != null) {
            Vector3d spawnPosition = spawnPoint.getPosition();
            MapMarker marker = new MapMarkerBuilder("Spawn", "Spawn.png", new Transform(spawnPosition))
               .withName(Message.translation("server.general.spawn"))
               .build();
            collector.add(marker);
         }
      }
   }
}
