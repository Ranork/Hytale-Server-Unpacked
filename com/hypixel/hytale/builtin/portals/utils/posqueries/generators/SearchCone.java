package com.hypixel.hytale.builtin.portals.utils.posqueries.generators;

import com.hypixel.hytale.builtin.portals.utils.posqueries.SpatialQuery;
import com.hypixel.hytale.builtin.portals.utils.posqueries.SpatialQueryDebug;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SearchCone implements SpatialQuery {
   private final Vector3d direction;
   private final double minRadius;
   private final double maxRadius;
   private final double maxDegrees;
   private final int attempts;

   public SearchCone(Vector3d direction, double radius, double maxDegrees, int attempts) {
      this(direction, radius, radius, maxDegrees, attempts);
   }

   public SearchCone(Vector3d direction, double minRadius, double maxRadius, double maxDegrees, int attempts) {
      this.direction = direction;
      this.minRadius = minRadius;
      this.maxRadius = maxRadius;
      this.maxDegrees = maxDegrees;
      this.attempts = attempts;
   }

   @Nonnull
   @Override
   public Stream<Vector3d> createCandidates(@Nonnull World world, @Nonnull Vector3d origin, @Nullable SpatialQueryDebug debug) {
      if (debug != null) {
         String radiusFmt = this.minRadius == this.maxRadius
            ? String.format("%.1f", this.minRadius)
            : String.format("%.1f", this.minRadius) + "-" + String.format("%.1f", this.maxRadius);
         debug.appendLine(
            "Searching in a "
               + radiusFmt
               + " radius cone (max "
               + String.format("%.1f", this.maxDegrees)
               + "°) in direction "
               + SpatialQueryDebug.fmt(this.direction)
               + " from "
               + SpatialQueryDebug.fmt(origin)
               + ":"
         );
      }

      double maxRadians = Math.toRadians(this.maxDegrees);
      return Stream.<Vector3d>generate(() -> {
         ThreadLocalRandom random = ThreadLocalRandom.current();
         double distance = this.minRadius + random.nextDouble() * (this.maxRadius - this.minRadius);
         double yawOffset = (random.nextDouble() - 0.5) * maxRadians;
         Vector3d dir = this.direction.clone().rotateY((float)yawOffset).setLength(distance);
         return dir.add(origin);
      }).limit(this.attempts);
   }
}
