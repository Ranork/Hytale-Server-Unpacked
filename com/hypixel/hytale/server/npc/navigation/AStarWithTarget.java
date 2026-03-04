package com.hypixel.hytale.server.npc.navigation;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.movement.controllers.ProbeMoveData;
import javax.annotation.Nonnull;

public class AStarWithTarget extends AStarBase {
   @Nonnull
   protected Vector3d targetPosition = new Vector3d();
   protected long targetPositionIndex;

   @Nonnull
   public Vector3d getTargetPosition() {
      return this.targetPosition;
   }

   public long getTargetPositionIndex() {
      return this.targetPositionIndex;
   }

   @Nonnull
   public AStarDebugWithTarget createDebugHelper(@Nonnull HytaleLogger logger) {
      return new AStarDebugWithTarget(this, logger);
   }

   public AStarBase.Progress initComputePath(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Vector3d start,
      @Nonnull Vector3d end,
      AStarEvaluator evaluator,
      @Nonnull MotionController motionController,
      @Nonnull ProbeMoveData probeMoveData,
      @Nonnull AStarNodePoolProvider nodePoolProvider,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      AStarBase.Progress progress = super.initComputePath(ref, start, evaluator, motionController, probeMoveData, nodePoolProvider, componentAccessor);
      this.targetPosition.assign(end);
      this.targetPositionIndex = this.positionToIndex(this.targetPosition);
      return progress;
   }

   public float findClosestPath() {
      if (this.path != null) {
         return Float.MAX_VALUE;
      } else {
         AStarNode node = this.findBestVisitedNode(AStarNode::getEstimateToGoal, (oldV, v) -> v < oldV, Float.MAX_VALUE);
         if (node == null) {
            return Float.MAX_VALUE;
         } else {
            this.buildPath(node);
            return node.getEstimateToGoal();
         }
      }
   }
}
