package com.hypixel.hytale.server.npc.corecomponents.movement;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionLeave;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.navigation.AStarBase;
import com.hypixel.hytale.server.npc.navigation.AStarNode;
import javax.annotation.Nonnull;

public class BodyMotionLeave extends BodyMotionFindBase<AStarBase> {
   protected final double distanceSquared;

   public BodyMotionLeave(@Nonnull BuilderBodyMotionLeave builderMotionLeave, @Nonnull BuilderSupport support) {
      super(builderMotionLeave, support, new AStarBase());
      double distance = builderMotionLeave.getDistance(support);
      this.distanceSquared = distance * distance;
   }

   @Override
   public boolean isGoalReached(Ref<EntityStore> ref, @Nonnull MotionController controller, Vector3d position, ComponentAccessor<EntityStore> componentAccessor) {
      return controller.waypointDistanceSquared(this.aStar.getStartPosition(), position) >= this.distanceSquared;
   }

   @Override
   public boolean isGoalReached(
      Ref<EntityStore> ref,
      @Nonnull AStarBase aStarBase,
      @Nonnull AStarNode aStarNode,
      @Nonnull MotionController controller,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      return controller.waypointDistanceSquared(aStarBase.getStartPosition(), aStarNode.getPosition()) >= this.distanceSquared;
   }

   @Override
   public float estimateToGoal(AStarBase aStarBase, Vector3d fromPosition, MotionController motionController) {
      return 0.0F;
   }

   @Override
   public void findBestPath(@Nonnull AStarBase aStarBase, MotionController controller) {
      aStarBase.buildFurthestPath();
   }
}
