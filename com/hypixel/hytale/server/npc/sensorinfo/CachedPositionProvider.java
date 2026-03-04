package com.hypixel.hytale.server.npc.sensorinfo;

public class CachedPositionProvider extends PositionProvider {
   private boolean fromCache;

   public void setIsFromCache(boolean status) {
      this.fromCache = status;
   }

   public boolean isFromCache() {
      return this.fromCache;
   }
}
