package com.hypixel.hytale.server.core.universe.world;

import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;

@FunctionalInterface
public interface World$GenericBlockBulkUpdater<T> {
    void apply(World var1, T var2, long var3, WorldChunk var5, int var6, int var7, int var8, int var9, int var10, int var11);
}