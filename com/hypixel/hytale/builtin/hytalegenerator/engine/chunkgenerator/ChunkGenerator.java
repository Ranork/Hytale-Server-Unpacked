package com.hypixel.hytale.builtin.hytalegenerator.engine.chunkgenerator;

import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedChunk;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ChunkGenerator {
   @Nullable
   GeneratedChunk generate(@Nonnull ChunkRequest.Arguments var1);

   @Nonnull
   PositionProvider getSpawnPositions();
}
