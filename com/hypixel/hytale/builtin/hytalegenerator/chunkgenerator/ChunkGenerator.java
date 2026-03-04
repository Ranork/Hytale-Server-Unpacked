package com.hypixel.hytale.builtin.hytalegenerator.chunkgenerator;

import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedChunk;
import javax.annotation.Nonnull;

public interface ChunkGenerator {
   @Nonnull
   GeneratedChunk generate(@Nonnull ChunkRequest.Arguments var1);

   @Nonnull
   PositionProvider getSpawnPositions();
}
