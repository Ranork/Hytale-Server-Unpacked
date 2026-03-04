package com.hypixel.hytale.server.core.universe.world.chunk.state;

import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import javax.annotation.Nullable;

public interface TickableBlockState {
   void tick(float var1, int var2, ArchetypeChunk<ChunkStore> var3, Store<ChunkStore> var4, CommandBuffer<ChunkStore> var5);

   Vector3i getPosition();

   Vector3i getBlockPosition();

   @Nullable
   WorldChunk getChunk();

   void invalidate();
}
