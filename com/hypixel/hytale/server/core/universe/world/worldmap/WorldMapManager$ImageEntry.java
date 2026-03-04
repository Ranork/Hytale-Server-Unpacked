package com.hypixel.hytale.server.core.universe.world.worldmap;

import com.hypixel.hytale.protocol.packets.worldmap.MapImage;
import java.util.concurrent.atomic.AtomicInteger;

public class WorldMapManager$ImageEntry {
    private final AtomicInteger keepAlive = new AtomicInteger();
    private final MapImage image;

    public WorldMapManager$ImageEntry(MapImage image) {
        this.image = image;
    }
}