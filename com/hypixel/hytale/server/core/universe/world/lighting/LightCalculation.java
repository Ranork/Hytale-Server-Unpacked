package com.hypixel.hytale.server.core.universe.world.lighting;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import javax.annotation.Nonnull;

public interface LightCalculation {
   void init(@Nonnull WorldChunk var1);

   @Nonnull
   CalculationResult calculateLight(@Nonnull Vector3i var1);

   boolean invalidateLightAtBlock(@Nonnull WorldChunk var1, int var2, int var3, int var4, @Nonnull BlockType var5, int var6, int var7);

   boolean invalidateLightInChunkSections(@Nonnull WorldChunk var1, int var2, int var3);
}
