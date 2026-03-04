package com.hypixel.hytale.server.npc.navigation;

import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nullable;

public interface IWaypoint {
   int getLength();

   Vector3d getPosition();

   @Nullable
   IWaypoint advance(int var1);

   @Nullable
   IWaypoint next();
}
