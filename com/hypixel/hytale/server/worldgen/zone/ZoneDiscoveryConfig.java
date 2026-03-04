package com.hypixel.hytale.server.worldgen.zone;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record ZoneDiscoveryConfig(
   boolean display,
   String zone,
   @Nullable String soundEventId,
   @Nullable String icon,
   boolean major,
   float duration,
   float fadeInDuration,
   float fadeOutDuration
) {
   @Nonnull
   public static final ZoneDiscoveryConfig DEFAULT = new ZoneDiscoveryConfig(false, "Void", null, null, true, 4.0F, 1.5F, 1.5F);

   @Nonnull
   public static ZoneDiscoveryConfig of(
      @Nullable Boolean display,
      @Nullable String zone,
      @Nullable String soundEventId,
      @Nullable String icon,
      @Nullable Boolean major,
      @Nullable Float duration,
      @Nullable Float fadeInDuration,
      @Nullable Float fadeOutDuration
   ) {
      return new ZoneDiscoveryConfig(
         display != null ? display : false,
         zone != null ? zone : "Void",
         soundEventId,
         icon,
         major != null ? major : true,
         duration != null ? duration : 4.0F,
         fadeInDuration != null ? fadeInDuration : 1.5F,
         fadeOutDuration != null ? fadeOutDuration : 1.5F
      );
   }
}
