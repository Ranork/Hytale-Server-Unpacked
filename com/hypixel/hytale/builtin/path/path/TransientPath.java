package com.hypixel.hytale.builtin.path.path;

import com.hypixel.hytale.builtin.path.waypoint.RelativeWaypointDefinition;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.path.IPath;
import com.hypixel.hytale.server.core.universe.world.path.SimplePathWaypoint;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TransientPath implements IPath<SimplePathWaypoint> {
   protected final List<SimplePathWaypoint> waypoints = new ObjectArrayList();

   public void addWaypoint(@Nonnull Vector3d position, @Nonnull Vector3f rotation) {
      this.waypoints
         .add(
            new SimplePathWaypoint(
               (short)this.waypoints.size(), new Transform(position.x, position.y, position.z, rotation.getPitch(), rotation.getYaw(), rotation.getRoll())
            )
         );
   }

   @Nullable
   @Override
   public UUID getId() {
      return null;
   }

   @Nullable
   @Override
   public String getName() {
      return null;
   }

   @Nonnull
   @Override
   public List<SimplePathWaypoint> getPathWaypoints() {
      return Collections.unmodifiableList(this.waypoints);
   }

   @Override
   public int length() {
      return this.waypoints.size();
   }

   public SimplePathWaypoint get(int index) {
      return this.waypoints.get(index);
   }

   @Nonnull
   public static IPath<SimplePathWaypoint> buildPath(
      @Nonnull Vector3d origin, @Nonnull Vector3f rotation, @Nonnull Queue<RelativeWaypointDefinition> instructions, double scale
   ) {
      TransientPath path = new TransientPath();
      path.addWaypoint(origin, rotation);
      Vector3d position = new Vector3d(origin);
      Vector3d directionVector = new Vector3d();
      Vector3f rotationVector = new Vector3f(rotation);

      while (!instructions.isEmpty()) {
         RelativeWaypointDefinition instruction = instructions.poll();
         rotationVector.addYaw(instruction.getRotation());
         directionVector.assign(PhysicsMath.headingX(rotationVector.getYaw()), 0.0, PhysicsMath.headingZ(rotationVector.getYaw()));
         directionVector.setLength(instruction.getDistance() * scale);
         position.add(directionVector);
         path.addWaypoint(position, rotationVector);
      }

      return path;
   }
}
