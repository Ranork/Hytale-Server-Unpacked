package com.hypixel.hytale.builtin.portals.utils.posqueries;

import com.hypixel.hytale.builtin.portals.utils.posqueries.predicates.generic.FilterQuery;
import com.hypixel.hytale.builtin.portals.utils.posqueries.predicates.generic.FlatMapQuery;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.World;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface SpatialQuery {
   @Nonnull
   Stream<Vector3d> createCandidates(@Nonnull World var1, @Nonnull Vector3d var2, @Nullable SpatialQueryDebug var3);

   @Nonnull
   default SpatialQuery then(@Nonnull SpatialQuery expand) {
      return new FlatMapQuery(this, expand);
   }

   @Nonnull
   default SpatialQuery filter(@Nonnull PositionPredicate predicate) {
      return new FilterQuery(this, predicate);
   }

   @Nonnull
   default Optional<Vector3d> execute(@Nonnull World world, @Nonnull Vector3d origin) {
      return this.createCandidates(world, origin, null).findFirst();
   }

   @Nonnull
   default Optional<Vector3d> debug(@Nonnull World world, @Nonnull Vector3d origin) {
      try {
         SpatialQueryDebug debug = new SpatialQueryDebug();
         Optional<Vector3d> output = this.createCandidates(world, origin, debug).findFirst();
         debug.appendLine("-> OUTPUT: " + output.map(SpatialQueryDebug::fmt).orElse("<null>"));
         HytaleLogger.getLogger().at(Level.INFO).log(debug.toString());
         return output;
      } catch (Throwable var5) {
         ((HytaleLogger.Api)HytaleLogger.getLogger().at(Level.SEVERE).withCause(var5)).log("Error in SpatialQuery");
         throw new RuntimeException("Error in SpatialQuery", var5);
      }
   }
}
