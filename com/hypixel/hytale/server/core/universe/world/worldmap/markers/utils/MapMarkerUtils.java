package com.hypixel.hytale.server.core.universe.world.worldmap.markers.utils;

import com.hypixel.hytale.protocol.packets.worldmap.MapMarker;
import com.hypixel.hytale.protocol.packets.worldmap.MapMarkerComponent;
import com.hypixel.hytale.protocol.packets.worldmap.PlacedByMarkerComponent;

public final class MapMarkerUtils {
   private MapMarkerUtils() {
   }

   public static boolean isUserMarker(MapMarker protoMarker) {
      if (protoMarker.components == null) {
         return false;
      } else {
         for (MapMarkerComponent component : protoMarker.components) {
            if (component instanceof PlacedByMarkerComponent) {
               return true;
            }
         }

         return false;
      }
   }
}
