package com.hypixel.hytale.server.npc.navigation;

import javax.annotation.Nullable;
import org.joml.Vector3d;

public interface IWaypoint {
   int getLength();

   Vector3d getPosition();

   @Nullable
   IWaypoint advance(int var1);

   @Nullable
   IWaypoint next();
}
