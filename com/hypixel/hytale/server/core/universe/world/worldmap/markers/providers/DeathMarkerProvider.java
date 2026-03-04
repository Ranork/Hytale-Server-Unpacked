package com.hypixel.hytale.server.core.universe.world.worldmap.markers.providers;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.asset.type.gameplay.WorldMapConfig;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerDeathPositionData;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MapMarkerBuilder;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MarkersCollector;
import javax.annotation.Nonnull;

public class DeathMarkerProvider implements WorldMapManager.MarkerProvider {
   public static final DeathMarkerProvider INSTANCE = new DeathMarkerProvider();

   private DeathMarkerProvider() {
   }

   @Override
   public void update(@Nonnull World world, @Nonnull Player player, @Nonnull MarkersCollector collector) {
      WorldMapConfig worldMapConfig = world.getGameplayConfig().getWorldMapConfig();
      if (worldMapConfig.isDisplayDeathMarker()) {
         PlayerWorldData perWorldData = player.getPlayerConfigData().getPerWorldData(world.getName());

         for (PlayerDeathPositionData deathPosition : perWorldData.getDeathPositions()) {
            addDeathMarker(collector, deathPosition);
         }
      }
   }

   private static void addDeathMarker(@Nonnull MarkersCollector collector, @Nonnull PlayerDeathPositionData deathPosition) {
      Transform transform = deathPosition.getTransform();
      if (collector.isInViewDistance(transform)) {
         int deathDay = deathPosition.getDay();
         Message name = Message.translation("server.map.markers.death").param("day", deathDay);
         MapMarkerBuilder builder = new MapMarkerBuilder(deathPosition.getMarkerId(), "Death.png", transform).withName(name);
         collector.addIgnoreViewDistance(builder.build());
      }
   }
}
