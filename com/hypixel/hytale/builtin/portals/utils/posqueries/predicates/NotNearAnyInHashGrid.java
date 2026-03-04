package com.hypixel.hytale.builtin.portals.utils.posqueries.predicates;

import com.hypixel.hytale.builtin.portals.utils.posqueries.PositionPredicate;
import com.hypixel.hytale.builtin.portals.utils.spatial.SpatialHashGrid;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.World;
import javax.annotation.Nonnull;

public record NotNearAnyInHashGrid(SpatialHashGrid<?> hashGrid, double radius) implements PositionPredicate {
   @Override
   public boolean test(@Nonnull World world, @Nonnull Vector3d point) {
      return !this.hashGrid.hasAnyWithin(point, this.radius);
   }
}
