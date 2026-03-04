package com.hypixel.hytale.server.core.universe.world.worldmap.markers.providers;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.entity.entities.player.data.PlayerWorldData;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MarkersCollector;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.user.UserMapMarker;
import javax.annotation.Nonnull;

public class PersonalMarkersProvider implements WorldMapManager.MarkerProvider {
   public static final PersonalMarkersProvider INSTANCE = new PersonalMarkersProvider();

   private PersonalMarkersProvider() {
   }

   @Override
   public void update(@Nonnull World world, @Nonnull Player player, @Nonnull MarkersCollector collector) {
      PlayerWorldData perWorldData = player.getPlayerConfigData().getPerWorldData(world.getName());

      for (UserMapMarker userMapMarker : perWorldData.getUserMapMarkers()) {
         collector.add(userMapMarker.toProtocolMarker());
      }
   }
}
