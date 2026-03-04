package com.hypixel.hytale.server.core.universe.world.worldgen;

import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.server.core.universe.world.spawn.FitToHeightMapSpawnProvider;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import com.hypixel.hytale.server.core.universe.world.spawn.IndividualSpawnProvider;
import java.util.concurrent.CompletableFuture;
import java.util.function.LongPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IWorldGen {
   @Nullable
   WorldGenTimingsCollector getTimings();

   CompletableFuture<GeneratedChunk> generate(int var1, long var2, int var4, int var5, LongPredicate var6);

   @Deprecated
   Transform[] getSpawnPoints(int var1);

   @Nonnull
   default ISpawnProvider getDefaultSpawnProvider(int seed) {
      return new FitToHeightMapSpawnProvider(new IndividualSpawnProvider(this.getSpawnPoints(seed)));
   }

   default void shutdown() {
   }
}
