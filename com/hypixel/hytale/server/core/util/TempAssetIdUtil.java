package com.hypixel.hytale.server.core.util;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import java.util.logging.Level;

@Deprecated(forRemoval = true)
public class TempAssetIdUtil {
   public static final String SOIL_GRASS = "Soil_Grass";
   public static final String SOUND_EVENT_ITEM_REPAIR = "SFX_Item_Repair";
   public static final String SOUND_EVENT_PLAYER_PICKUP_ITEM = "SFX_Player_Pickup_Item";
   public static final String SOUND_EVENT_ITEM_BREAK = "SFX_Item_Break";
   public static final String SOUND_EVENT_PLAYER_CRAFT_ITEM_INVENTORY = "SFX_Player_Craft_Item_Inventory";
   public static final String SOUND_EVENT_PLAYER_DROP_ITEM = "SFX_Player_Drop_Item";
   public static final String PARTICLE_EXAMPLE_SIMPLE = "Example_Simple";
   public static final String PARTICLE_SPLASH = "Splash";
   public static final String DEFAULT_PLAYER_MODEL_NAME = "Player";

   public static int getSoundEventIndex(String soundEventId) {
      int soundEventIndex = SoundEvent.getAssetMap().getIndex(soundEventId);
      if (soundEventIndex == Integer.MIN_VALUE) {
         HytaleLogger.getLogger().at(Level.WARNING).log("Attempted to play an invalid sound event %s", soundEventId);
         soundEventIndex = 0;
      }

      return soundEventIndex;
   }
}
