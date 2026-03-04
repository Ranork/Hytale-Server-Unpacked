package com.hypixel.hytale.builtin.portals.utils.posqueries.generators;

import com.hypixel.hytale.builtin.portals.utils.posqueries.SpatialQuery;
import com.hypixel.hytale.builtin.portals.utils.posqueries.SpatialQueryDebug;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SearchBelow implements SpatialQuery {
   private final int height;

   public SearchBelow(int height) {
      this.height = height;
   }

   @Nonnull
   @Override
   public Stream<Vector3d> createCandidates(@Nonnull World world, @Nonnull Vector3d origin, @Nullable SpatialQueryDebug debug) {
      if (debug != null) {
         debug.appendLine("Searching up to " + this.height + " blocks below " + SpatialQueryDebug.fmt(origin) + ":");
      }

      return IntStream.rangeClosed(0, this.height).mapToObj(dy -> origin.clone().add(0.0, -dy, 0.0));
   }
}
