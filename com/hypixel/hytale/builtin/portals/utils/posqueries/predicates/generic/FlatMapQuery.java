package com.hypixel.hytale.builtin.portals.utils.posqueries.predicates.generic;

import com.hypixel.hytale.builtin.portals.utils.posqueries.SpatialQuery;
import com.hypixel.hytale.builtin.portals.utils.posqueries.SpatialQueryDebug;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FlatMapQuery implements SpatialQuery {
   @Nonnull
   private final SpatialQuery generator;
   @Nonnull
   private final SpatialQuery expand;

   public FlatMapQuery(@Nonnull SpatialQuery generator, @Nonnull SpatialQuery expand) {
      this.generator = generator;
      this.expand = expand;
   }

   @Nonnull
   @Override
   public Stream<Vector3d> createCandidates(@Nonnull World world, @Nonnull Vector3d origin, @Nullable SpatialQueryDebug debug) {
      return this.generator.createCandidates(world, origin, debug).flatMap(candidate -> {
         Stream<Vector3d> candidates = this.expand.createCandidates(world, candidate, debug);
         if (debug != null) {
            debug.indent("Flat-map expand from " + SpatialQueryDebug.fmt(candidate) + ":");
            return Stream.concat(candidates, Stream.of((Vector3d)null).peek(x -> debug.unindent()).flatMap(x -> Stream.empty()));
         } else {
            return candidates;
         }
      });
   }
}
