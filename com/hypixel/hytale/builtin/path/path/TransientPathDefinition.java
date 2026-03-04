package com.hypixel.hytale.builtin.path.path;

import com.hypixel.hytale.builtin.path.waypoint.RelativeWaypointDefinition;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.world.path.IPath;
import com.hypixel.hytale.server.core.universe.world.path.SimplePathWaypoint;
import java.util.ArrayDeque;
import java.util.List;
import javax.annotation.Nonnull;

public class TransientPathDefinition {
   protected final List<RelativeWaypointDefinition> waypointDefinitions;
   protected final double scale;

   public TransientPathDefinition(List<RelativeWaypointDefinition> waypointDefinitions, double scale) {
      this.waypointDefinitions = waypointDefinitions;
      this.scale = scale;
   }

   @Nonnull
   public IPath<SimplePathWaypoint> buildPath(@Nonnull Vector3d position, @Nonnull Vector3f rotation) {
      ArrayDeque<RelativeWaypointDefinition> queue = new ArrayDeque<>(this.waypointDefinitions);
      return TransientPath.buildPath(position, rotation, queue, this.scale);
   }
}
