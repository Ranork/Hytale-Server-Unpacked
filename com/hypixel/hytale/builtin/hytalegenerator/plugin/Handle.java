package com.hypixel.hytale.builtin.hytalegenerator.plugin;

import com.hypixel.hytale.builtin.hytalegenerator.chunkgenerator.ChunkRequest;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.spawn.ISpawnProvider;
import com.hypixel.hytale.server.core.universe.world.worldgen.GeneratedChunk;
import com.hypixel.hytale.server.core.universe.world.worldgen.IWorldGen;
import com.hypixel.hytale.server.core.universe.world.worldgen.WorldGenTimingsCollector;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.LongPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Handle implements IWorldGen {
   @Nonnull
   private final HytaleGenerator plugin;
   @Nonnull
   private final ChunkRequest.GeneratorProfile profile;
   @Nullable
   private final String seedOverride;

   public Handle(@Nonnull HytaleGenerator plugin, @Nonnull ChunkRequest.GeneratorProfile profile, @Nullable String seedOverride) {
      this.plugin = plugin;
      this.profile = profile;
      this.seedOverride = seedOverride;
   }

   @Nonnull
   @Override
   public CompletableFuture<GeneratedChunk> generate(int seed, long index, int x, int z, LongPredicate stillNeeded) {
      ChunkRequest.Arguments arguments = new ChunkRequest.Arguments(seed, index, x, z, stillNeeded);
      if (this.seedOverride != null) {
         seed = Objects.hash(this.seedOverride);
      }

      this.profile.setSeed(seed);
      ChunkRequest request = new ChunkRequest(this.profile, arguments);
      return this.plugin.submitChunkRequest(request);
   }

   @Nonnull
   public ChunkRequest.GeneratorProfile getProfile() {
      return this.profile;
   }

   @Nonnull
   @Override
   public Transform[] getSpawnPoints(int seed) {
      ChunkRequest.GeneratorProfile seededProfile = this.profile.clone();
      seededProfile.setSeed(seed);
      int MAX_SPAWN_POINTS = 1000000;
      List<Vector3d> positions = this.plugin.getSpawnPositions(seededProfile, 1000000);
      Transform[] positionsArray = new Transform[positions.size()];

      for (int i = 0; i < positions.size(); i++) {
         positionsArray[i] = new Transform(positions.get(i));
      }

      return positions.isEmpty() ? new Transform[]{new Transform(0.0, 140.0, 0.0)} : positionsArray;
   }

   @Nonnull
   @Override
   public ISpawnProvider getDefaultSpawnProvider(int seed) {
      return IWorldGen.super.getDefaultSpawnProvider(seed);
   }

   @Nullable
   @Override
   public WorldGenTimingsCollector getTimings() {
      return null;
   }
}
