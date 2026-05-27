package com.hypixel.hytale.server.core.universe.world.path;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.vector.Rotation3f;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public interface IPathWaypoint {
   int getOrder();

   Vector3d getWaypointPosition(@Nonnull ComponentAccessor<EntityStore> var1);

   Rotation3f getWaypointRotation(@Nonnull ComponentAccessor<EntityStore> var1);

   double getPauseTime();

   float getObservationAngle();
}
