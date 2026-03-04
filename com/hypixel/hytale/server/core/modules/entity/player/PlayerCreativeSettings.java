package com.hypixel.hytale.server.core.modules.entity.player;

import javax.annotation.Nonnull;

public record PlayerCreativeSettings(boolean allowNPCDetection, boolean respondToHit) {
   public PlayerCreativeSettings() {
      this(false, false);
   }

   @Nonnull
   public PlayerCreativeSettings clone() {
      return new PlayerCreativeSettings(this.allowNPCDetection, this.respondToHit);
   }
}
