package com.hypixel.hytale.server.core.modules.entity.player;

import javax.annotation.Nonnull;

public record PlayerCreativeSettings(
   boolean allowNPCDetection,
   boolean respondToHit,
   @Nonnull String placeMode,
   int creativeInteractionDistance,
   boolean showBuilderToolsNotifications,
   boolean noPhysics
) {
   public PlayerCreativeSettings() {
      this(false, false, "default", 10, true, false);
   }

   @Nonnull
   public PlayerCreativeSettings clone() {
      return new PlayerCreativeSettings(
         this.allowNPCDetection, this.respondToHit, this.placeMode, this.creativeInteractionDistance, this.showBuilderToolsNotifications, this.noPhysics
      );
   }
}
