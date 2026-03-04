package com.hypixel.hytale.server.npc.navigation;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;

public interface AStarEvaluator {
   boolean isGoalReached(Ref<EntityStore> var1, AStarBase var2, AStarNode var3, MotionController var4, ComponentAccessor<EntityStore> var5);

   float estimateToGoal(AStarBase var1, Vector3d var2, MotionController var3);
}
