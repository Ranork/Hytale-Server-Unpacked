package com.hypixel.hytale.server.core.universe.world;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record WorldMapTracker$ZoneDiscoveryInfo(
    @Nonnull String zoneName,
    @Nonnull String regionName,
    boolean display,
    @Nullable String discoverySoundEventId,
    @Nullable String icon,
    boolean major,
    float duration,
    float fadeInDuration,
    float fadeOutDuration
) {
    @Nonnull
    public WorldMapTracker$ZoneDiscoveryInfo clone() {
        return new WorldMapTracker$ZoneDiscoveryInfo(
            this.zoneName,
            this.regionName,
            this.display,
            this.discoverySoundEventId,
            this.icon,
            this.major,
            this.duration,
            this.fadeInDuration,
            this.fadeOutDuration
        );
    }
}