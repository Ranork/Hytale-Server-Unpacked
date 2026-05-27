package com.hypixel.hytale.server.npc.navigation;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.modules.physics.util.PhysicsMath;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.NPCPlugin;
import com.hypixel.hytale.server.npc.movement.Steering;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.movement.controllers.ProbeMoveData;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import java.util.logging.Level;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;

public class PathFollower {
   @Nullable
   protected IWaypoint currentWaypoint;
   protected double currentWaypointDistanceSquared;
   protected PathFollower.FrozenWaypoint frozenWaypoint;
   protected boolean isWaypointFrozen;
   protected final Vector3d lastWaypointPosition = new Vector3d();
   protected final Vector3d direction = new Vector3d();
   protected final Vector3d tempVector = new Vector3d();
   protected final Vector3d tempPath = new Vector3d();
   protected final Vector3d projection = new Vector3d();
   protected final Vector3d rejection = new Vector3d();
   protected int pathSmoothing;
   protected double blendHeading;
   protected double relativeSpeed;
   protected double relativeSpeedWaypoint;
   protected double waypointRadius;
   protected double rejectionWeight = 3.0;
   protected double waypointRadiusSquared;
   protected boolean debugNodes = false;
   protected boolean shouldSmoothPath = true;

   public void setPathSmoothing(int pathSmoothing) {
      this.pathSmoothing = pathSmoothing;
   }

   public double getRelativeSpeed() {
      return this.relativeSpeed;
   }

   public void setRelativeSpeed(double relativeSpeed) {
      this.relativeSpeed = relativeSpeed;
   }

   public void setRelativeSpeedWaypoint(double relativeSpeedWaypoint) {
      this.relativeSpeedWaypoint = relativeSpeedWaypoint;
   }

   public void setWaypointRadius(double waypointRadius) {
      this.waypointRadius = waypointRadius;
      this.waypointRadiusSquared = waypointRadius * waypointRadius;
   }

   public void setDebugNodes(boolean debugNodes) {
      this.debugNodes = debugNodes;
   }

   public boolean shouldSmoothPath() {
      return this.shouldSmoothPath;
   }

   public void setRejectionWeight(double rejectionWeight) {
      this.rejectionWeight = rejectionWeight;
   }

   public void setBlendHeading(double blendHeading) {
      if (blendHeading < 0.0 || blendHeading > 1.0) {
         blendHeading = 0.0;
      }

      this.blendHeading = blendHeading;
   }

   @Nullable
   public IWaypoint getCurrentWaypoint() {
      return this.currentWaypoint;
   }

   @Nullable
   public Vector3d getCurrentWaypointPosition() {
      return this.currentWaypoint == null ? null : this.currentWaypoint.getPosition();
   }

   @Nullable
   public IWaypoint getNextWaypoint() {
      return this.currentWaypoint == null ? null : this.currentWaypoint.next();
   }

   @Nullable
   public Vector3d getNextWaypointPosition() {
      IWaypoint waypoint = this.getNextWaypoint();
      return waypoint == null ? null : waypoint.getPosition();
   }

   public void setPath(IWaypoint firstWaypoint, @Nonnull Vector3d startPosition) {
      this.currentWaypoint = firstWaypoint;
      this.lastWaypointPosition.set(startPosition);
      this.currentWaypointDistanceSquared = Double.MAX_VALUE;
      this.shouldSmoothPath = true;
      this.isWaypointFrozen = false;
   }

   public void clearPath() {
      this.currentWaypoint = null;
      this.isWaypointFrozen = false;
   }

   public boolean pathInFinalStage() {
      if (this.currentWaypoint == null) {
         return true;
      } else if (this.currentWaypoint.next() != null) {
         return false;
      } else {
         this.freezeWaypoint();
         return true;
      }
   }

   public boolean freezeWaypoint() {
      if (this.currentWaypoint == null) {
         return false;
      } else {
         if (this.frozenWaypoint == null) {
            this.frozenWaypoint = new PathFollower.FrozenWaypoint();
         }

         if (this.currentWaypoint == this.frozenWaypoint) {
            return true;
         } else {
            this.frozenWaypoint.position.set(this.currentWaypoint.getPosition());
            this.currentWaypoint = this.frozenWaypoint;
            this.isWaypointFrozen = true;
            return true;
         }
      }
   }

   public boolean isWaypointFrozen() {
      return this.isWaypointFrozen;
   }

   public void setWaypointFrozen(boolean waypointFrozen) {
      this.isWaypointFrozen = waypointFrozen;
   }

   public void executePath(@Nonnull Vector3d currentPosition, @Nonnull MotionController activeMotionController, @Nonnull Steering desiredSteering) {
      Vector3d target = this.getCurrentWaypointPosition();
      if (target != null) {
         this.tempVector.set(target).sub(currentPosition);
         double length = this.tempVector.length();
         desiredSteering.setMaxDistance(length);
         if (length > this.waypointRadius) {
            this.direction.set(this.tempVector);
            this.computeRejection(currentPosition, target, activeMotionController);
            this.direction.sub(this.rejection);
            desiredSteering.setTranslation(this.direction.mul(this.relativeSpeed / length));
         } else {
            if (length > 0.1) {
               this.direction.set(this.tempVector);
            }

            desiredSteering.setTranslation(this.direction.mul(this.relativeSpeedWaypoint / length));
         }
      }
   }

   public void computeRejection(@Nonnull Vector3d currentPosition, @Nonnull Vector3d target, @Nonnull MotionController activeMotionController) {
      this.tempPath.set(target).sub(this.lastWaypointPosition).mul(activeMotionController.getComponentSelector());
      this.tempVector.set(currentPosition).sub(this.lastWaypointPosition).mul(activeMotionController.getComponentSelector());
      double dotDD = this.tempPath.lengthSquared();
      double dotDP = this.tempPath.dot(this.tempVector);
      this.projection.set(this.tempPath).mul(dotDP / dotDD);
      this.rejection.set(this.tempVector).sub(this.projection).mul(this.rejectionWeight);
   }

   public boolean updateCurrentTarget(@Nonnull Vector3d entityPosition, @Nonnull MotionController motionController) {
      if (this.currentWaypoint == null) {
         return false;
      } else {
         Vector3d waypointPosition = this.currentWaypoint.getPosition();
         double distanceSquared = motionController.waypointDistanceSquared(waypointPosition, entityPosition);
         double projectionLength = 0.0;
         boolean reachedWaypoint;
         if (!(distanceSquared <= 1.0000000000000002E-10) && (!(distanceSquared < 0.01) || !(distanceSquared > this.currentWaypointDistanceSquared))) {
            projectionLength = NPCPhysicsMath.dotProduct(waypointPosition, this.lastWaypointPosition, entityPosition, motionController.getComponentSelector());
            reachedWaypoint = projectionLength < 0.0;
         } else {
            reachedWaypoint = true;
         }

         this.currentWaypointDistanceSquared = distanceSquared;
         if (this.debugNodes) {
            NPCPlugin.get()
               .getLogger()
               .at(Level.INFO)
               .log(
                  "=== Target len=%s before=%s targetdist=%s  proj=%s pos=%s tgt=%s",
                  this.currentWaypoint.getLength(),
                  !reachedWaypoint,
                  Math.sqrt(distanceSquared),
                  projectionLength,
                  Vector3dUtil.formatShortString(entityPosition),
                  Vector3dUtil.formatShortString(waypointPosition)
               );
         }

         if (reachedWaypoint) {
            this.lastWaypointPosition.set(waypointPosition);
            this.currentWaypoint = this.currentWaypoint.next();
            if (this.currentWaypoint == null) {
               this.isWaypointFrozen = false;
               return false;
            }

            this.currentWaypointDistanceSquared = Double.MAX_VALUE;
            this.shouldSmoothPath = true;
            waypointPosition = this.currentWaypoint.getPosition();
            if (this.blendHeading > 0.0) {
               distanceSquared = motionController.waypointDistanceSquared(waypointPosition, entityPosition);
            }
         }

         motionController.requirePreciseMovement(waypointPosition);
         if (this.blendHeading <= 0.0) {
            return true;
         } else {
            motionController.enableHeadingBlending();
            if (distanceSquared > this.waypointRadiusSquared) {
               return true;
            } else {
               IWaypoint nextWaypoint = this.getNextWaypoint();
               if (nextWaypoint == null) {
                  return true;
               } else {
                  this.tempVector.set(nextWaypoint.getPosition()).sub(waypointPosition);
                  distanceSquared = NPCPhysicsMath.projectedLengthSquared(this.tempVector, motionController.getComponentSelector());
                  if (distanceSquared < 0.001) {
                     return true;
                  } else {
                     float yaw = PhysicsMath.headingFromDirection(this.tempVector.x, this.tempVector.z);
                     motionController.enableHeadingBlending(yaw, waypointPosition, this.blendHeading);
                     return true;
                  }
               }
            }
         }
      }
   }

   public void smoothPath(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull Vector3d position,
      @Nonnull MotionController motionController,
      @Nonnull ProbeMoveData probeMoveData,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      this.shouldSmoothPath = false;
      if (this.pathSmoothing > 0) {
         IWaypoint node = this.currentWaypoint;
         if (node != null) {
            int startLength = node.getLength();

            IWaypoint startNode;
            int skip;
            do {
               startNode = node;
               int length = node.getLength();
               if (length == 1) {
                  skip = 0;
                  break;
               }

               skip = Math.min(this.pathSmoothing, length - 1);
               node = node.advance(skip);
            } while (this.canMoveTo(ref, motionController, position, node.getPosition(), probeMoveData, componentAccessor));

            while (skip > 1) {
               int middleSkip = skip / 2;
               IWaypoint middleNode = startNode.advance(middleSkip);
               if (this.canMoveTo(ref, motionController, position, middleNode.getPosition(), probeMoveData, componentAccessor)) {
                  startNode = middleNode;
                  skip -= middleSkip;
               } else {
                  skip = middleSkip;
               }
            }

            if (this.debugNodes) {
               int l = startNode.getLength();
               NPCPlugin.get()
                  .getLogger()
                  .at(Level.INFO)
                  .log(
                     "=== New Target len=%s skipped=%s pos=%s tgt=%s dist=%s",
                     l,
                     startLength - l,
                     Vector3dUtil.formatShortString(position),
                     Vector3dUtil.formatShortString(startNode.getPosition()),
                     position.distance(startNode.getPosition())
                  );
            }

            this.currentWaypoint = startNode;
            this.currentWaypointDistanceSquared = Double.MAX_VALUE;
         }
      }
   }

   protected boolean canMoveTo(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull MotionController motionController,
      @Nonnull Vector3d position,
      @Nonnull Vector3d targetPosition,
      @Nonnull ProbeMoveData probeMoveData,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      probeMoveData.setPosition(position).setTargetPosition(targetPosition);
      return probeMoveData.canMoveTo(ref, motionController, 9.999999994736442E-8, 0.5, componentAccessor);
   }

   private static class FrozenWaypoint implements IWaypoint {
      protected final Vector3d position = new Vector3d();

      @Override
      public int getLength() {
         return 1;
      }

      @Nonnull
      @Override
      public Vector3d getPosition() {
         return this.position;
      }

      @Nullable
      @Override
      public IWaypoint advance(int skip) {
         return null;
      }

      @Nullable
      @Override
      public IWaypoint next() {
         return null;
      }
   }
}
