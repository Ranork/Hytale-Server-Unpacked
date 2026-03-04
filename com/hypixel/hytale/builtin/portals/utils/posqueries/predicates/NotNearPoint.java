package com.hypixel.hytale.builtin.portals.utils.posqueries.predicates;

import com.hypixel.hytale.builtin.portals.utils.posqueries.PositionPredicate;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.World;
import javax.annotation.Nonnull;

public final class NotNearPoint implements PositionPredicate {
   @Nonnull
   private final Vector3d point;
   private final double radiusSq;

   public NotNearPoint(@Nonnull Vector3d point, double radius) {
      this.point = point;
      this.radiusSq = radius * radius;
   }

   @Override
   public boolean test(@Nonnull World world, @Nonnull Vector3d origin) {
      return origin.distanceSquaredTo(this.point) >= this.radiusSq;
   }
}
