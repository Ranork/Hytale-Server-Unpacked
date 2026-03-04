package com.hypixel.hytale.server.npc.navigation;

public interface AStarNodePool {
   AStarNode allocate();

   void deallocate(AStarNode var1);
}
