package com.hypixel.hytale.server.core.modules.migrations;

import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;

public interface Migration {
   void run(WorldChunk var1);
}
