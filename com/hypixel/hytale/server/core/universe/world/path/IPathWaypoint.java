package com.hypixel.hytale.server.core.universe.world.path;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public interface IPathWaypoint {
   int getOrder();

   Vector3d getWaypointPosition(@Nonnull ComponentAccessor<EntityStore> var1);

   Vector3f getWaypointRotation(@Nonnull ComponentAccessor<EntityStore> var1);

   double getPauseTime();

   float getObservationAngle();
}
