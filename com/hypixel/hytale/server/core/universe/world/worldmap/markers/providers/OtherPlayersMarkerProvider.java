package com.hypixel.hytale.server.core.universe.world.worldmap.markers.providers;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.packets.worldmap.HeightDeltaIconComponent;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.protocol.packets.worldmap.PlayerMarkerComponent;
import com.hypixel.hytale.server.core.asset.type.gameplay.WorldMapConfig;
import com.hypixel.hytale.server.core.asset.type.gameplay.worldmap.PlayersMapMarkerConfig;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.WorldMapManager;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MapMarkerBuilder;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MarkersCollector;
import java.util.function.Predicate;
import javax.annotation.Nonnull;

public class OtherPlayersMarkerProvider implements WorldMapManager.MarkerProvider {
   public static final OtherPlayersMarkerProvider INSTANCE = new OtherPlayersMarkerProvider();

   private OtherPlayersMarkerProvider() {
   }

   @Override
   public void update(@Nonnull World world, @Nonnull Player player, @Nonnull MarkersCollector collector) {
      WorldMapConfig worldMapConfig = world.getGameplayConfig().getWorldMapConfig();
      if (worldMapConfig.isDisplayPlayers()) {
         PlayersMapMarkerConfig playersMapConfig = worldMapConfig.getPlayersConfig();
         Predicate<PlayerRef> playerMapFilter = collector.getPlayerMapFilter();

         for (PlayerRef otherPlayer : world.getPlayerRefs()) {
            if (!otherPlayer.getUuid().equals(player.getUuid())) {
               Transform otherPlayerTransform = otherPlayer.getTransform();
               Vector3d otherPos = otherPlayerTransform.getPosition();
               if (collector.isInViewDistance(otherPos) && (playerMapFilter == null || !playerMapFilter.test(otherPlayer))) {
                  PlayerMarkerComponent playerMarker = new PlayerMarkerComponent(otherPlayer.getUuid());
                  HeightDeltaIconComponent heightDeltaComponent = new HeightDeltaIconComponent(
                     playersMapConfig.getIconSwapHeightDelta(),
                     playersMapConfig.getAboveIcon(),
                     playersMapConfig.getIconSwapHeightDelta(),
                     playersMapConfig.getBelowIcon()
                  );
                  String markerId = "Player-" + otherPlayer.getUuid();
                  MapMarker marker = new MapMarkerBuilder(markerId, "Player.png", otherPlayerTransform)
                     .withCustomName(otherPlayer.getUsername())
                     .withComponent(playerMarker)
                     .withComponent(heightDeltaComponent)
                     .build();
                  collector.addIgnoreViewDistance(marker);
               }
            }
         }
      }
   }
}
