package com.hypixel.hytale.server.npc.corecomponents.movement;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.random.RandomExtra;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionMoveAway;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.movement.steeringforces.SteeringForceEvade;
import com.hypixel.hytale.server.npc.navigation.AStarBase;
import com.hypixel.hytale.server.npc.navigation.AStarNode;
import com.hypixel.hytale.server.npc.navigation.AStarWithTarget;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BodyMotionMoveAway extends BodyMotionFindWithTarget {
   protected final double stopDistance;
   protected final double stopDistanceSquared;
   protected final double[] holdDirectionDurationRange;
   protected final float changeDirectionViewSector;
   protected final float jitterAngle;
   protected final double erraticDistanceSquared;
   protected final float erraticJitter;
   protected final double erraticChangeDurationMultiplier;
   protected final SteeringForceEvade evade = new SteeringForceEvade();
   protected float fleeDirection;
   protected double holdDirectionTimeRemaining;

   public BodyMotionMoveAway(@Nonnull BuilderBodyMotionMoveAway builderMotionFind, @Nonnull BuilderSupport support) {
      super(builderMotionFind, support);
      this.stopDistance = builderMotionFind.getStopDistance(support);
      this.stopDistanceSquared = this.stopDistance * this.stopDistance;
      this.holdDirectionDurationRange = builderMotionFind.getHoldDirectionDurationRange(support);
      this.changeDirectionViewSector = builderMotionFind.getChangeDirectionViewSectorRadians(support);
      this.jitterAngle = builderMotionFind.getDirectionJitterRadians(support);
      double erraticDistance = builderMotionFind.getErraticDistance(support);
      this.erraticDistanceSquared = erraticDistance * erraticDistance;
      float erraticExtraJitter = builderMotionFind.getErraticExtraJitterRadians(support);
      this.erraticJitter = MathUtil.clamp(this.jitterAngle + erraticExtraJitter, 0.0F, (float) Math.PI);
      this.erraticChangeDurationMultiplier = builderMotionFind.getErraticChangeDurationMultiplier(support);
      this.evade.setDistances(builderMotionFind.getSlowdownDistance(support), this.stopDistance);
      this.evade.setFalloff(builderMotionFind.getFalloff(support));
      this.evade.setAdhereToDirectionHint(true);
   }

   @Override
   public void activate(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      super.activate(ref, role, componentAccessor);
      this.holdDirectionTimeRemaining = 0.0;
   }

   @Override
   public boolean computeSteering(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      @Nullable InfoProvider infoProvider,
      double dt,
      @Nonnull Steering desiredSteering,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      NPCEntity npcComponent = componentAccessor.getComponent(ref, NPCEntity.getComponentType());

      assert npcComponent != null;

      float speedMultiplier = npcComponent.getCurrentHorizontalSpeedMultiplier(ref, componentAccessor);
      if (speedMultiplier == 0.0F) {
         desiredSteering.clear();
         return true;
      } else {
         this.holdDirectionTimeRemaining -= dt * speedMultiplier;
         return super.computeSteering(ref, role, infoProvider, dt, desiredSteering, componentAccessor);
      }
   }

   @Override
   protected boolean computeSteering(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Role role,
      Vector3d position,
      @Nonnull Steering desiredSteering,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      Vector3d selfPosition = transformComponent.getPosition();
      Vector3f bodyRotation = transformComponent.getRotation();
      Vector3d lastTargetPosition = this.getLastTargetPosition();
      if (NPCPhysicsMath.inViewSector(
         selfPosition.x, selfPosition.z, bodyRotation.getYaw(), this.changeDirectionViewSector, lastTargetPosition.x, lastTargetPosition.z
      )) {
         this.holdDirectionTimeRemaining = 0.0;
      }

      if (this.holdDirectionTimeRemaining <= 0.0) {
         boolean inErraticRange = selfPosition.distanceSquaredTo(lastTargetPosition) < this.erraticDistanceSquared;
         float jitter = inErraticRange ? this.erraticJitter : this.jitterAngle;
         this.fleeDirection = PhysicsMath.headingFromDirection(selfPosition.x - lastTargetPosition.x, selfPosition.z - lastTargetPosition.z)
            + RandomExtra.randomRange(-jitter, jitter);
         this.holdDirectionTimeRemaining = RandomExtra.randomRange(this.holdDirectionDurationRange);
         if (inErraticRange) {
            this.holdDirectionTimeRemaining = this.holdDirectionTimeRemaining * this.erraticChangeDurationMultiplier;
         }
      }

      this.evade.setPositions(selfPosition, lastTargetPosition);
      this.evade.setDirectionHint(this.fleeDirection);
      MotionController motionController = role.getActiveMotionController();
      double desiredAltitudeWeight = this.desiredAltitudeWeight >= 0.0 ? this.desiredAltitudeWeight : motionController.getDesiredAltitudeWeight();
      return this.scaleSteering(ref, role, this.evade, desiredSteering, desiredAltitudeWeight, componentAccessor);
   }

   @Override
   public boolean isGoalReached(
      Ref<EntityStore> ref,
      AStarBase aStarBase,
      @Nonnull AStarNode aStarNode,
      MotionController motionController,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      return aStarNode.getEstimateToGoal() <= 0.0F;
   }

   @Override
   protected boolean isGoalReached(
      Ref<EntityStore> ref,
      @Nonnull MotionController motionController,
      Vector3d position,
      Vector3d lastTestedPosition,
      ComponentAccessor<EntityStore> componentAccessor
   ) {
      return motionController.waypointDistanceSquared(position, lastTestedPosition) >= this.stopDistanceSquared;
   }

   @Override
   public float estimateToGoal(@Nonnull AStarBase aStarBase, Vector3d fromPosition, @Nonnull MotionController motionController) {
      return Math.max(0.0F, (float)(this.stopDistance - motionController.waypointDistance(fromPosition, ((AStarWithTarget)aStarBase).getTargetPosition())));
   }

   @Override
   public void findBestPath(@Nonnull AStarBase aStarBase, MotionController controller) {
      aStarBase.buildBestPath(AStarNode::getEstimateToGoal, (oldV, v) -> v < oldV, Float.MAX_VALUE);
   }
}
