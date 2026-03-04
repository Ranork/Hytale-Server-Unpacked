package com.hypixel.hytale.server.core.universe.world.path;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.vector.Transform;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public class SimplePathWaypoint implements IPathWaypoint {
   private int order;
   private Transform transform;

   public SimplePathWaypoint(int order, Transform transform) {
      this.order = order;
      this.transform = transform;
   }

   @Override
   public int getOrder() {
      return this.order;
   }

   @Nonnull
   @Override
   public Vector3d getWaypointPosition(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return this.transform.getPosition();
   }

   @Nonnull
   @Override
   public Vector3f getWaypointRotation(@Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      return this.transform.getRotation();
   }

   @Override
   public double getPauseTime() {
      return 0.0;
   }

   @Override
   public float getObservationAngle() {
      return 0.0F;
   }
}
