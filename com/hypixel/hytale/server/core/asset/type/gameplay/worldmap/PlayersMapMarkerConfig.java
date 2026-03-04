package com.hypixel.hytale.server.core.asset.type.gameplay.worldmap;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;

public class PlayersMapMarkerConfig {
   public static final BuilderCodec<PlayersMapMarkerConfig> CODEC = BuilderCodec.builder(PlayersMapMarkerConfig.class, PlayersMapMarkerConfig::new)
      .append(new KeyedCodec<>("IconSwapHeightDelta", Codec.INTEGER), (config, o) -> config.iconSwapHeightDelta = o, config -> config.iconSwapHeightDelta)
      .documentation("When seeing other players' icons on the map, what the Y difference between you and them need to be in order to swap their icon.")
      .add()
      .<String>append(new KeyedCodec<>("BelowIcon", Codec.STRING), (config, o) -> config.belowIcon = o, config -> config.belowIcon)
      .documentation("The icon when a player is below you. Find Player.png in the assets for the folder where to see/add available icons.")
      .add()
      .<String>append(new KeyedCodec<>("AboveIcon", Codec.STRING), (config, o) -> config.aboveIcon = o, config -> config.aboveIcon)
      .documentation("The icon when a player is above you. Find Player.png in the assets for the folder where to see/add available icons.")
      .add()
      .build();
   private int iconSwapHeightDelta = 12;
   private String belowIcon = "PlayerBelow.png";
   private String aboveIcon = "PlayerAbove.png";

   public int getIconSwapHeightDelta() {
      return this.iconSwapHeightDelta;
   }

   public String getBelowIcon() {
      return this.belowIcon;
   }

   public String getAboveIcon() {
      return this.aboveIcon;
   }
}
