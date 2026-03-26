package com.hypixel.hytale.builtin.hytalegenerator.engine.chunkgenerator;

import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.EmptyPositionProvider;
import com.hypixel.hytale.builtin.hytalegenerator.positionproviders.PositionProvider;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedBlockChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedBlockStateChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedEntityChunk;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class FallbackGenerator implements ChunkGenerator {
   @Nonnull
   public static final FallbackGenerator INSTANCE = new FallbackGenerator();

   @Nullable
   @Override
   public GeneratedChunk generate(@Nonnull ChunkRequest.Arguments arguments) {
      return new GeneratedChunk(
         new GeneratedBlockChunk(arguments.index(), arguments.x(), arguments.z()),
         new GeneratedBlockStateChunk(),
         new GeneratedEntityChunk(),
         GeneratedChunk.makeSections()
      );
   }

   @NonNullDecl
   @Override
   public PositionProvider getSpawnPositions() {
      return EmptyPositionProvider.INSTANCE;
   }
}
