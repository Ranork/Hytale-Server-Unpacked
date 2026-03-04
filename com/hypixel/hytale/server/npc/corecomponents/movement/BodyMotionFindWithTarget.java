package com.hypixel.hytale.server.npc.corecomponents.movement;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.entity.Entity;
import com.hypixel.hytale.server.core.entity.EntityUtils;
import com.hypixel.hytale.server.core.entity.LivingEntity;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.movement.builders.BuilderBodyMotionFindWithTarget;
import com.hypixel.hytale.server.npc.entities.NPCEntity;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.navigation.AStarBase;
import com.hypixel.hytale.server.npc.navigation.AStarWithTarget;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.IPositionProvider;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BodyMotionFindWithTarget extends BodyMotionFindBase<AStarWithTarget> {
   protected final double minMoveDistanceWait;
   protected final double minMoveDistanceWaitSquared;
   protected final double minMoveDistanceRecompute;
   protected final double minMoveDistanceRecomputeSquared;
   protected final float cosHalfRecomputeConeAngle;
   protected final double minMoveDistanceReproject;
   protected final double minMoveDistanceReprojectSquared;
   protected final boolean adjustRangeByHitboxSize;
   protected final Vector3d lastPathedPosition = new Vector3d();
   protected final Vector3d conePosition = new Vector3d();
   protected final Vector3d coneDirection = new Vector3d();
   @Nullable
   protected Box targetBoundingBox;
   protected Box selfBoundingBox;
   protected boolean waitForTargetMovement = false;
   private final Vector3d lastTargetPosition = new Vector3d();
   private final Vector3d lastAccessibleTargetPosition = new Vector3d();
   private boolean haveValidTargetPosition;
   private boolean haveAccessibleTargetPosition;
   private boolean lastAccessibleTargetPositionIsCurrent;
   protected String self;
   protected String other;

   public BodyMotionFindWithTarget(@Nonnull BuilderBodyMotionFindWithTarget builderMotionFindWithTarget, @Nonnull BuilderSupport support) {
      super(builderMotionFindWithTarget, support, new AStarWithTarget());
      this.adjustRangeByHitboxSize = builderMotionFindWithTarget.isAdjustRangeByHitboxSize(support);
      this.minMoveDistanceWait = builderMotionFindWithTarget.getMinMoveDistanceWait(support);
      this.minMoveDistanceWaitSquared = this.minMoveDistanceWait * this.minMoveDistanceWait;
      this.minMoveDistanceRecompute = builderMotionFindWithTarget.getMinMoveDistanceRecompute(support);
      this.minMoveDistanceRecomputeSquared = this.minMoveDistanceRecompute * this.minMoveDistanceRecompute;
      this.minMoveDistanceReproject = builderMotionFindWithTarget.getMinMoveDistanceReproject(support);
      this.minMoveDistanceReprojectSquared = this.minMoveDistanceReproject * this.minMoveDistanceReproject;
      float cosine = TrigMathUtil.cos(builderMotionFindWithTarget.getRecomputeConeAngle(support) / 2.0);
      this.cosHalfRecomputeConeAngle = cosine == -1.0F ? 1.0F : cosine;
   }

   @Override
   public void activate(@Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      super.activate(ref, role, componentAccessor);
      this.haveValidTargetPosition = false;
      this.haveAccessibleTargetPosition = false;
      this.waitForTargetMovement = false;
      this.targetBoundingBox = null;
      this.lastPathedPosition.assign(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);
      this.self = role.getRoleName();
   }

   @Override
   public boolean canComputeMotion(
      @Nonnull Ref<EntityStore> ref, @Nonnull Role role, @Nullable InfoProvider infoProvider, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      BoundingBox boundingBoxComponent = componentAccessor.getComponent(ref, BoundingBox.getComponentType());

      assert boundingBoxComponent != null;

      this.targetBoundingBox = null;
      this.selfBoundingBox = boundingBoxComponent.getBoundingBox();
      this.lastAccessibleTargetPositionIsCurrent = false;
      if (infoProvider != null && infoProvider.hasPosition()) {
         IPositionProvider positionProvider = infoProvider.getPositionProvider();
         if (positionProvider.providePosition(this.lastTargetPosition)) {
            this.haveValidTargetPosition = true;
            Ref<EntityStore> targetEntityReference = positionProvider.getTarget();
            if (targetEntityReference != null) {
               BoundingBox targetEntityBoundingBoxComponent = componentAccessor.getComponent(targetEntityReference, BoundingBox.getComponentType());

               assert targetEntityBoundingBoxComponent != null;

               NPCEntity npcEntityComponent = componentAccessor.getComponent(targetEntityReference, NPCEntity.getComponentType());
               this.other = npcEntityComponent != null ? npcEntityComponent.getRoleName() : null;
               this.targetBoundingBox = targetEntityBoundingBoxComponent.getBoundingBox();
               MovementStatesComponent movementStatesComponent = componentAccessor.getComponent(
                  targetEntityReference, MovementStatesComponent.getComponentType()
               );
               Entity entity = EntityUtils.getEntity(targetEntityReference, targetEntityReference.getStore());
               if (entity instanceof LivingEntity && movementStatesComponent != null && movementStatesComponent.getMovementStates().onGround) {
                  this.lastAccessibleTargetPosition.assign(this.lastTargetPosition);
                  this.haveAccessibleTargetPosition = true;
                  this.lastAccessibleTargetPositionIsCurrent = true;
               }
            } else {
               this.targetBoundingBox = null;
            }
         }
      }

      this.targetDeltaSquared = this.haveValidTargetPosition
         ? role.getActiveMotionController().waypointDistanceSquared(this.getLastTargetPosition(), this.lastPathedPosition)
         : Double.MAX_VALUE;
      return this.haveValidTargetPosition;
   }

   @Override
   public boolean mustRecomputePath(@Nonnull MotionController activeMotionController) {
      if (super.mustRecomputePath(activeMotionController)) {
         return true;
      } else if (this.minMoveDistanceRecomputeSquared > 0.0 && this.targetDeltaSquared > this.minMoveDistanceRecomputeSquared) {
         if (this.dbgStatus) {
            NPCPlugin.get().getLogger().at(Level.INFO).log("Recomputing Path - Target moved");
         }

         this.resetThrottleCount();
         return true;
      } else {
         if (this.cosHalfRecomputeConeAngle < 1.0F) {
            if (activeMotionController.is2D()) {
               if (NPCPhysicsMath.isInViewCone(
                  this.conePosition,
                  this.coneDirection,
                  this.cosHalfRecomputeConeAngle,
                  this.getLastTargetPosition(),
                  activeMotionController.getComponentSelector()
               )) {
                  if (this.dbgStatus) {
                     NPCPlugin.get().getLogger().at(Level.INFO).log("Recomputing Path - Target left 2D cone");
                  }

                  return true;
               }
            } else if (NPCPhysicsMath.isInViewCone(this.conePosition, this.coneDirection, this.cosHalfRecomputeConeAngle, this.getLastTargetPosition())) {
               if (this.dbgStatus) {
                  NPCPlugin.get().getLogger().at(Level.INFO).log("Recomputing Path - Target left cone");
               }

               return true;
            }
         }

         return false;
      }
   }

   @Override
   public void forceRecomputePath(MotionController activeMotionController) {
      super.forceRecomputePath(activeMotionController);
   }

   @Override
   public boolean shouldDeferPathComputation(
      @Nonnull MotionController motionController, Vector3d position, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.throttleCount > this.throttleIgnoreCount) {
         double distanceSquared = this.getLastTargetPosition().distanceSquaredTo(this.lastPathedPosition);
         if (distanceSquared < 1.0000000000000002E-10 || this.waitForTargetMovement && distanceSquared < this.minMoveDistanceWaitSquared) {
            return true;
         }
      }

      this.waitForTargetMovement = false;
      this.lastPathedPosition.assign(this.getLastAccessibleTargetPosition(motionController, false, componentAccessor));
      return false;
   }

   @Override
   protected boolean mustAbortThrottling(MotionController motionController, Ref<EntityStore> ref) {
      if (super.mustAbortThrottling(motionController, ref)) {
         return true;
      } else if (this.minMoveDistanceRecomputeSquared > 0.0 && this.targetDeltaSquared > this.minMoveDistanceRecomputeSquared) {
         if (this.dbgMotionState) {
            NPCPlugin.get().getLogger().at(Level.INFO).log("MotionFindWithTarget: Aborting throttling - Target moved");
         }

         return true;
      } else {
         return false;
      }
   }

   @Override
   public boolean isGoalReached(
      Ref<EntityStore> ref, MotionController activeMotionController, Vector3d position, ComponentAccessor<EntityStore> componentAccessor
   ) {
      return this.isGoalReached(ref, activeMotionController, position, this.getLastTargetPosition(), componentAccessor);
   }

   @Override
   public AStarBase.Progress startComputePath(
      @Nonnull Ref<EntityStore> ref,
      Role role,
      @Nonnull MotionController activeMotionController,
      @Nonnull Vector3d position,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (this.cosHalfRecomputeConeAngle < 1.0F) {
         this.conePosition.assign(position);
         this.coneDirection.assign(this.lastPathedPosition).subtract(position);
      }

      return this.aStar
         .initComputePath(
            ref, position, this.lastPathedPosition, this, activeMotionController, this.probeMoveData, this.sharedNodePoolProvider, componentAccessor
         );
   }

   @Override
   public void onBlockedPath() {
      super.onBlockedPath();
   }

   @Override
   public void onNoPathFound(MotionController motionController) {
      super.onNoPathFound(motionController);
      this.waitForTargetMovement = true;
   }

   @Override
   protected void onSteering(MotionController activeMotionController, @Nonnull Ref<EntityStore> ref, @Nonnull ComponentAccessor<EntityStore> componentAccessor) {
      TransformComponent transformComponent = componentAccessor.getComponent(ref, TransformComponent.getComponentType());

      assert transformComponent != null;

      this.lastPathedPosition.assign(transformComponent.getPosition());
   }

   @Override
   protected void decorateDebugString(@Nonnull StringBuilder dbgString) {
      dbgString.append("D:").append(MathUtil.round(Math.sqrt(this.targetDeltaSquared), 1));
   }

   protected abstract boolean isGoalReached(Ref<EntityStore> var1, MotionController var2, Vector3d var3, Vector3d var4, ComponentAccessor<EntityStore> var5);

   protected Vector3d getLastTargetPosition() {
      return this.lastTargetPosition;
   }

   protected Vector3d getLastAccessibleTargetPosition(
      @Nonnull MotionController motionController, boolean approximate, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (!this.lastAccessibleTargetPositionIsCurrent
         && (
            !approximate
               || !this.haveAccessibleTargetPosition
               || !(motionController.waypointDistanceSquared(this.lastTargetPosition, this.lastAccessibleTargetPosition) < this.minMoveDistanceReprojectSquared)
         )) {
         if (this.dbgMotionState && motionController.is2D()) {
            NPCPlugin.get().getLogger().at(Level.INFO).log("MotionFindWithTarget: Reprojecting %s -> %s", this.self, this.other);
         }

         this.lastAccessibleTargetPosition.assign(this.lastTargetPosition);
         motionController.translateToAccessiblePosition(this.lastAccessibleTargetPosition, this.targetBoundingBox, 0.0, 320.0, componentAccessor);
         this.haveAccessibleTargetPosition = true;
         this.lastAccessibleTargetPositionIsCurrent = true;
         return this.lastAccessibleTargetPosition;
      } else {
         return this.lastAccessibleTargetPosition;
      }
   }
}
