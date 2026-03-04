package com.hypixel.hytale.builtin.portals.utils.posqueries.generators;

import com.hypixel.hytale.builtin.portals.utils.posqueries.SpatialQuery;
import com.hypixel.hytale.builtin.portals.utils.posqueries.SpatialQueryDebug;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SearchCircular implements SpatialQuery {
   private final double minRadius;
   private final double maxRadius;
   private final int attempts;

   public SearchCircular(double radius, int attempts) {
      this(radius, radius, attempts);
   }

   public SearchCircular(double minRadius, double maxRadius, int attempts) {
      this.minRadius = minRadius;
      this.maxRadius = maxRadius;
      this.attempts = attempts;
   }

   @Nonnull
   @Override
   public Stream<Vector3d> createCandidates(@Nonnull World world, @Nonnull Vector3d origin, @Nullable SpatialQueryDebug debug) {
      if (debug != null) {
         String radiusFmt = this.minRadius == this.maxRadius
            ? String.format("%.1f", this.minRadius)
            : String.format("%.1f", this.minRadius) + "-" + String.format("%.1f", this.maxRadius);
         debug.appendLine("Searching in a " + radiusFmt + " radius circle around " + SpatialQueryDebug.fmt(origin) + ":");
      }

      return Stream.<Vector3d>generate(() -> {
         ThreadLocalRandom rand = ThreadLocalRandom.current();
         double rad = rand.nextDouble() * Math.PI * 2.0;
         double radius = this.minRadius + rand.nextDouble() * (this.maxRadius - this.minRadius);
         return origin.clone().add(Math.cos(rad) * radius, 0.0, Math.sin(rad) * radius);
      }).limit(this.attempts);
   }
}
