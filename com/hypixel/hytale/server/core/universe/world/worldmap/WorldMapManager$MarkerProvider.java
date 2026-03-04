package com.hypixel.hytale.server.core.universe.world.worldmap;

import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.worldmap.markers.MarkersCollector;
import javax.annotation.Nonnull;

public interface WorldMapManager$MarkerProvider {
    void update(@Nonnull World var1, @Nonnull Player var2, @Nonnull MarkersCollector var3);
}