package com.hypixel.hytale.server.npc.corecomponents.movement;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionFind;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.movement.steeringforces.SteeringForcePursue;
import com.hypixel.hytale.server.npc.navigation.AStarBase;
import com.hypixel.hytale.server.npc.navigation.AStarNode;
import com.hypixel.hytale.server.npc.navigation.AStarWithTarget;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BodyMotionFind extends BodyMotionFindWithTarget {
   protected final double distance;
   protected final double distanceSquared;
   protected final boolean reachable;
   protected final double heightDifferenceMin;
   protected final double heightDifferenceMax;
   protected final double abortDistance;
   protected final double abortDistanceSquared;
   protected final double switchToSteeringDistance;
   protected final double switchToSteeringDistanceSquared;
   protected final SteeringForcePursue seek = new SteeringForcePursue();
   protected final Vector3d tempDirectionVector = new Vector3d();
   protected double effectiveDistanceSquared;

   public BodyMotionFind(@Nonnull BuilderBodyMotionFind builderMotionFind, @Nonnull BuilderSupport support) {
      super(builderMotionFind, support);
      this.reachable = builderMotionFind.getReachable(support);
      this.distance = builderMotionFind.getStopDistance(support);
      this.distanceSquared = this.distance * this.distance;
      double[] heightDifference = builderMotionFind.getHeightDifference(support);
      this.heightDifferenceMin = heightDifference[0];
      this.heightDifferenceMax = heightDifference[1];
      this.seek.setDistances(builderMotionFind.getSlowDownDistance(support), builderMotionFind.getStopDistance(support));
      this.seek.setFalloff(builderMotionFind.getFalloff(support));
      this.abortDistance = builderMotionFind.getAbortDistance(support);
      this.abortDistanceSquared = this.abortDistance * this.abortDistance;
      this.switchToSteeringDistance = builderMotionFind.getSwitchToSteeringDistance(support);
      this.switchToSteeringDistanceSquared = this.switchToSteeringDistance * this.switchToSteeringDistance;
   }

   @Override
   protected boolean canSwitchToSteering(
      @Nonnull Ref<EntityStore> ref, @Nonnull MotionController motionController, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      if (motionController.waypointDistanceSquared(position, this.getLastTargetPosition()) > this.switchToSteeringDistanceSquared) {
         return false;
      } else {
         if (this.dbgMotionState) {
            ((HytaleLogger.Api)NPCPlugin.get().getLogger().at(Level.INFO).every(100)).log("MotionFind: computing canSwitchToSteering");
         }

         return this.canReachTarget(
            ref, motionController, position, this.getLastAccessibleTargetPosition(motionController, true, componentAccessor), componentAccessor
         );
      }
   }

   @Override
   protected boolean shouldSkipSteering(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull MotionController activeMotionController,
      @Nonnull Vector3d position,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      Vector3d targetPosition = this.getLastAccessibleTargetPosition(activeMotionController, true, componentAccessor);
      this.probeMoveData.setPosition(position).setTargetPosition(targetPosition);
      activeMotionController.probeMove(ref, this.probeMoveData, componentAccessor);
      return !this.isGoalReached(ref, activeMotionController, this.probeMoveData.probePosition, targetPosition, componentAccessor);
   }

   @Override
   protected boolean computeSteering(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      @Nonnull Vector3d position,
      @Nonnull Steering desiredSteering,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.seek.setPositions(position, this.getLastTargetPosition());
      MotionController motionController = role.getActiveMotionController();
      this.seek.setComponentSelector(motionController.getComponentSelector());
      double desiredAltitudeWeight = this.desiredAltitudeWeight >= 0.0 ? this.desiredAltitudeWeight : motionController.getDesiredAltitudeWeight();
      return this.scaleSteering(ref, role, this.seek, desiredSteering, desiredAltitudeWeight, componentAccessor);
   }

   @Override
   public boolean canComputeMotion(
      @Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider infoProvider, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (super.canComputeMotion(ref, role, infoProvider, componentAccessor)
         && (
            !(this.abortDistance > 0.0)
               || !(role.getActiveMotionController().waypointDistanceSquared(ref, this.getLastTargetPosition(), componentAccessor) >= this.abortDistanceSquared)
         )) {
         if (this.selfBoundingBox != null && this.adjustRangeByHitboxSize) {
            double effectiveDistance = this.distance + Math.max(this.selfBoundingBox.width(), this.selfBoundingBox.depth());
            if (this.targetBoundingBox != null) {
               effectiveDistance += Math.max(this.targetBoundingBox.width(), this.targetBoundingBox.depth());
            }

            this.effectiveDistanceSquared = effectiveDistance * effectiveDistance;
         } else {
            this.effectiveDistanceSquared = this.distanceSquared;
         }

         return this.selfBoundingBox != null;
      } else {
         return false;
      }
   }

   @Override
   protected boolean isGoalReached(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull MotionController motionController,
      @Nonnull Vector3d position,
      @Nonnull Vector3d targetPosition,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      double differenceY = targetPosition.y - position.y;
      if (!(differenceY < this.heightDifferenceMin) && !(differenceY > this.heightDifferenceMax)) {
         return motionController.waypointDistanceSquared(position, targetPosition) > this.effectiveDistanceSquared
            ? false
            : !this.reachable || this.canReachTarget(ref, motionController, position, targetPosition, componentAccessor);
      } else {
         return false;
      }
   }

   @Override
   public boolean isGoalReached(
      @Nonnull Ref<EntityStore> ref,
      AStarBase aStarBase,
      @Nonnull AStarNode aStarNode,
      @Nonnull MotionController motionController,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      AStarWithTarget aStarWithTarget = (AStarWithTarget)aStarBase;
      return this.isGoalReached(ref, motionController, aStarNode.getPosition(), aStarWithTarget.getTargetPosition(), componentAccessor);
   }

   @Override
   public float estimateToGoal(@Nonnull AStarBase aStarBase, @Nonnull Vector3d fromPosition, MotionController motionController) {
      return (float)((AStarWithTarget)aStarBase).getTargetPosition().distanceTo(fromPosition);
   }

   @Override
   public void findBestPath(@Nonnull AStarBase aStarBase, MotionController controller) {
      aStarBase.buildBestPath(AStarNode::getEstimateToGoal, (oldV, v) -> v < oldV, Float.MAX_VALUE);
   }

   @Override
   protected void onThrottling(
      MotionController motionController, @Nonnull Ref<EntityStore> ref, @Nonnull Steering steering, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      super.onThrottling(motionController, ref, steering, componentAccessor);
      this.lookAtTarget(ref, steering, componentAccessor);
   }

   @Override
   protected void onDeferring(
      MotionController motionController, @Nonnull Ref<EntityStore> ref, @Nonnull Steering steering, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      super.onDeferring(motionController, ref, steering, componentAccessor);
      this.lookAtTarget(ref, steering, componentAccessor);
   }

   protected void lookAtTarget(@Nonnull Ref<EntityStore> ref, @Nonnull Steering steering, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d position = transformComponent.getPosition();
      Vector3f bodyRotation = transformComponent.getRotation();
      this.tempDirectionVector.assign(this.getLastTargetPosition()).subtract(position);
      steering.setYaw(NPCPhysicsMath.headingFromDirection(this.tempDirectionVector.x, this.tempDirectionVector.z, bodyRotation.getYaw()));
      steering.setPitch(
         NPCPhysicsMath.pitchFromDirection(this.tempDirectionVector.x, this.tempDirectionVector.y, this.tempDirectionVector.z, bodyRotation.getPitch())
      );
   }

   protected boolean canReachTarget(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull MotionController motionController,
      @Nonnull Vector3d position,
      @Nonnull Vector3d targetPosition,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.isBoundingBoxesOverlapping(position, targetPosition)) {
         return true;
      } else {
         Vector3d direction = this.tempDirectionVector.assign(targetPosition).subtract(position);
         motionController.probeMove(ref, position, direction, this.probeMoveData, componentAccessor);
         return this.isBoundingBoxesOverlapping(this.probeMoveData.probePosition, targetPosition);
      }
   }

   protected boolean isBoundingBoxesOverlapping(@Nonnull Vector3d position, @Nonnull Vector3d endPosition) {
      return this.targetBoundingBox == null
         ? this.containsPosition(position, endPosition)
         : containsPosition(
               position.x, this.selfBoundingBox.min.x - this.targetBoundingBox.max.x, this.selfBoundingBox.max.x - this.targetBoundingBox.min.x, endPosition.x
            )
            && containsPosition(
               position.y, this.selfBoundingBox.min.y - this.targetBoundingBox.max.y, this.selfBoundingBox.max.y - this.targetBoundingBox.min.y, endPosition.y
            )
            && containsPosition(
               position.z, this.selfBoundingBox.min.z - this.targetBoundingBox.max.z, this.selfBoundingBox.max.z - this.targetBoundingBox.min.z, endPosition.z
            );
   }

   protected boolean containsPosition(@Nonnull Vector3d position, @Nonnull Vector3d endPosition) {
      return containsPosition(position.x, this.selfBoundingBox.min.x, this.selfBoundingBox.max.x, endPosition.x)
         && containsPosition(position.y, this.selfBoundingBox.min.y, this.selfBoundingBox.max.y, endPosition.y)
         && containsPosition(position.z, this.selfBoundingBox.min.z, this.selfBoundingBox.max.z, endPosition.z);
   }

   protected static boolean containsPosition(double p, double min, double max, double v) {
      v -= p;
      return v >= min && v < max;
   }
}
