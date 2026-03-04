package com.hypixel.hytale.server.core.asset.type.gameplay;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.server.core.asset.type.gameplay.worldmap.PlayersMapMarkerConfig;
import com.hypixel.hytale.server.core.asset.type.gameplay.worldmap.UserMapMarkerConfig;
import javax.annotation.Nonnull;

public class WorldMapConfig {
   @Nonnull
   public static final BuilderCodec<WorldMapConfig> CODEC = BuilderCodec.builder(WorldMapConfig.class, WorldMapConfig::new)
      .append(
         new KeyedCodec<>("DisplaySpawn", Codec.BOOLEAN), (worldMapConfig, o) -> worldMapConfig.displaySpawn = o, worldMapConfig -> worldMapConfig.displaySpawn
      )
      .add()
      .append(
         new KeyedCodec<>("DisplayHome", Codec.BOOLEAN), (worldMapConfig, o) -> worldMapConfig.displayHome = o, worldMapConfig -> worldMapConfig.displayHome
      )
      .add()
      .append(
         new KeyedCodec<>("DisplayWarps", Codec.BOOLEAN), (worldMapConfig, o) -> worldMapConfig.displayWarps = o, worldMapConfig -> worldMapConfig.displayWarps
      )
      .add()
      .append(
         new KeyedCodec<>("DisplayDeathMarker", Codec.BOOLEAN),
         (worldMapConfig, o) -> worldMapConfig.displayDeathMarker = o,
         worldMapConfig -> worldMapConfig.displayDeathMarker
      )
      .add()
      .append(
         new KeyedCodec<>("DisplayPlayers", Codec.BOOLEAN),
         (worldMapConfig, o) -> worldMapConfig.displayPlayers = o,
         worldMapConfig -> worldMapConfig.displayPlayers
      )
      .add()
      .append(
         new KeyedCodec<>("PlayersMarkersConfig", PlayersMapMarkerConfig.CODEC),
         (worldMapConfig, o) -> worldMapConfig.playersConfig = o,
         worldMapConfig -> worldMapConfig.playersConfig
      )
      .add()
      .<Boolean>append(
         new KeyedCodec<>("CanTrackPlayersInCompass", Codec.BOOLEAN),
         (worldMapConfig, o) -> worldMapConfig.canTrackPlayersInCompass = o,
         worldMapConfig -> worldMapConfig.canTrackPlayersInCompass
      )
      .documentation("Whether the client functionality to toggle tracking other players in compass is enabled")
      .add()
      .<Boolean>append(
         new KeyedCodec<>("CanTogglePlayersInMap", Codec.BOOLEAN),
         (worldMapConfig, o) -> worldMapConfig.canTogglePlayersInMap = o,
         worldMapConfig -> worldMapConfig.canTogglePlayersInMap
      )
      .documentation("Whether the client functionality to toggle other players visibility on the world map is enabled")
      .add()
      .append(
         new KeyedCodec<>("UserMapMarkers", UserMapMarkerConfig.CODEC),
         (worldMapConfig, o) -> worldMapConfig.userMapMarkerConfig = o,
         worldMapConfig -> worldMapConfig.userMapMarkerConfig
      )
      .add()
      .build();
   protected boolean displaySpawn = true;
   protected boolean displayHome = true;
   protected boolean displayWarps = true;
   protected boolean displayDeathMarker = true;
   protected boolean displayPlayers = true;
   protected boolean canTrackPlayersInCompass = true;
   protected boolean canTogglePlayersInMap = true;
   protected PlayersMapMarkerConfig playersConfig = new PlayersMapMarkerConfig();
   protected UserMapMarkerConfig userMapMarkerConfig = new UserMapMarkerConfig();

   public boolean isDisplaySpawn() {
      return this.displaySpawn;
   }

   public boolean isDisplayHome() {
      return this.displayHome;
   }

   public boolean isDisplayWarps() {
      return this.displayWarps;
   }

   public boolean isDisplayDeathMarker() {
      return this.displayDeathMarker;
   }

   public boolean isDisplayPlayers() {
      return this.displayPlayers;
   }

   public PlayersMapMarkerConfig getPlayersConfig() {
      return this.playersConfig;
   }

   public boolean canTrackPlayersInCompass() {
      return this.canTrackPlayersInCompass;
   }

   public boolean canTogglePlayersInMap() {
      return this.canTogglePlayersInMap;
   }

   public UserMapMarkerConfig getUserMapMarkerConfig() {
      return this.userMapMarkerConfig;
   }
}
