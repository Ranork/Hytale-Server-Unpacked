package com.hypixel.hytale.server.core.modules.collision;

import com.hypixel.hytale.math.vector.Vector3i;

public interface IBlockTracker {
   Vector3i getPosition(int var1);

   int getCount();

   boolean track(int var1, int var2, int var3);

   void trackNew(int var1, int var2, int var3);

   boolean isTracked(int var1, int var2, int var3);

   void untrack(int var1, int var2, int var3);
}
