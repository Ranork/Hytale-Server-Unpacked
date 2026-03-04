package com.hypixel.hytale.server.core.universe.world.worldmap;

import com.hypixel.hytale.codec.lookup.CodecMapCodec;

public interface WorldMapManager$MarkerReference {
    CodecMapCodec<WorldMapManager$MarkerReference> CODEC = new CodecMapCodec();

    String getMarkerId();

    void remove();
}