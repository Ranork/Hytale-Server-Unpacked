package com.hypixel.hytale.server.core.universe.world.path;

import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

public interface IPath<Waypoint extends IPathWaypoint> {
   @Nullable
   UUID getId();

   @Nullable
   String getName();

   List<Waypoint> getPathWaypoints();

   int length();

   Waypoint get(int var1);
}
