package com.hypixel.hytale.builtin.path.path;

import com.hypixel.hytale.builtin.path.waypoint.IPrefabPathWaypoint;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.path.IPath;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public interface IPrefabPath extends IPath<IPrefabPathWaypoint> {
   short registerNewWaypoint(@Nonnull IPrefabPathWaypoint var1, int var2);

   void registerNewWaypointAt(int var1, @Nonnull IPrefabPathWaypoint var2, int var3);

   void addLoadedWaypoint(@Nonnull IPrefabPathWaypoint var1, int var2, int var3, int var4);

   void removeWaypoint(int var1, int var2);

   void unloadWaypoint(int var1);

   boolean hasLoadedWaypoints();

   boolean isFullyLoaded();

   int loadedWaypointCount();

   int getWorldGenId();

   Vector3d getNearestWaypointPosition(@Nonnull Vector3d var1, @Nonnull ComponentAccessor<EntityStore> var2);

   void mergeInto(@Nonnull IPrefabPath var1, int var2, @Nonnull ComponentAccessor<EntityStore> var3);

   void compact(int var1);
}
