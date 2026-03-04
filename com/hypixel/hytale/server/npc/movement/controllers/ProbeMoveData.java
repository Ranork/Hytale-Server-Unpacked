package com.hypixel.hytale.server.npc.movement.controllers;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ProbeMoveData {
   @Nonnull
   public final Vector3d probePosition;
   @Nonnull
   public final Vector3d probeDirection;
   @Nonnull
   public final Vector3d initialPosition;
   @Nonnull
   public final Vector3d targetPosition;
   @Nonnull
   public final Vector3d directionComponentSelector;
   public boolean isAvoidingBlockDamage = true;
   public boolean isRelaxedMoveConstraints = false;
   public boolean onGround;
   public boolean isSavingSegments = false;
   public int segmentCount = 0;
   @Nullable
   public ProbeMoveData.Segment[] segments = null;

   public ProbeMoveData() {
      this.probeDirection = new Vector3d();
      this.probePosition = new Vector3d();
      this.initialPosition = new Vector3d();
      this.targetPosition = new Vector3d();
      this.directionComponentSelector = new Vector3d();
   }

   public void setSaveSegments(boolean saveSegments) {
      this.isSavingSegments = saveSegments;
      if (this.isSavingSegments && this.segments == null) {
         this.segments = new ProbeMoveData.Segment[6];

         for (int i = 0; i < this.segments.length; i++) {
            this.segments[i] = new ProbeMoveData.Segment();
         }
      }
   }

   public boolean isAvoidingBlockDamage() {
      return this.isAvoidingBlockDamage;
   }

   public void setAvoidingBlockDamage(boolean avoid) {
      this.isAvoidingBlockDamage = avoid;
   }

   public boolean isRelaxedMoveConstraints() {
      return this.isRelaxedMoveConstraints;
   }

   public void setRelaxedMoveConstraints(boolean relaxedMoveConstraints) {
      this.isRelaxedMoveConstraints = relaxedMoveConstraints;
   }

   @Nonnull
   public ProbeMoveData setPosition(@Nonnull Vector3d position) {
      this.probePosition.assign(position);
      this.initialPosition.assign(position);
      return this;
   }

   @Nonnull
   public ProbeMoveData setDirection(@Nonnull Vector3d direction) {
      this.probeDirection.assign(direction);
      this.targetPosition.assign(this.probePosition).add(this.probeDirection);
      return this;
   }

   @Nonnull
   public ProbeMoveData setTargetPosition(@Nonnull Vector3d targetPosition) {
      this.targetPosition.assign(targetPosition);
      this.probeDirection.assign(targetPosition).subtract(this.probePosition);
      return this;
   }

   public boolean canAdvance(
      @Nonnull Ref<EntityStore> ref, @Nonnull MotionController motionController, double threshold, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      double requiredDistance = threshold * this.probeDirection.length();
      return this.canAdvanceAbs(ref, motionController, requiredDistance, componentAccessor);
   }

   public boolean canAdvanceAbs(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull MotionController motionController,
      double requiredDistance,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      double distance = motionController.probeMove(ref, this, componentAccessor);
      return distance >= requiredDistance;
   }

   public boolean canMoveTo(
      @Nonnull Ref<EntityStore> ref,
      @Nonnull MotionController motionController,
      double maxDistance,
      double maxDistanceY,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      if (!this.canMoveTo(ref, motionController, maxDistance, componentAccessor)) {
         return false;
      } else if (!motionController.is2D()) {
         return true;
      } else {
         double dy = NPCPhysicsMath.getProjectedDifference(this.targetPosition, this.probePosition, motionController.getComponentSelector());
         return Math.abs(dy) <= maxDistanceY;
      }
   }

   public boolean canMoveTo(
      @Nonnull Ref<EntityStore> ref, @Nonnull MotionController motionController, double maxDistance, @Nonnull ComponentAccessor<EntityStore> componentAccessor
   ) {
      motionController.probeMove(ref, this, componentAccessor);
      return motionController.waypointDistanceSquared(this.targetPosition, this.probePosition) <= maxDistance * maxDistance;
   }

   public boolean computePosition(double distance, @Nonnull Vector3d result) {
      if (this.segmentCount < 2) {
         return false;
      } else if (distance <= 0.0) {
         result.assign(this.segments[0].position);
         return true;
      } else {
         int index = 1;
         ProbeMoveData.Segment segment = this.segments[0];

         ProbeMoveData.Segment prevSegment;
         for (prevSegment = null; index < this.segmentCount; index++) {
            prevSegment = segment;
            segment = this.segments[index];
            if (segment.distance >= distance) {
               break;
            }
         }

         if (segment.distance <= distance) {
            result.assign(segment.position);
            return true;
         } else if (segment.type.canInterpolate()) {
            double lambda = (distance - prevSegment.distance) / (segment.distance - prevSegment.distance);
            NPCPhysicsMath.lerp(prevSegment.position, segment.position, lambda, result);
            return true;
         } else {
            result.assign(prevSegment.position);
            return true;
         }
      }
   }

   public boolean startProbing() {
      if (this.isSavingSegments) {
         this.segmentCount = 0;
      }

      return this.isSavingSegments;
   }

   public void addStartSegment(@Nonnull Vector3d position, boolean onGround) {
      this.newSegment().initAsStartSegment(position, onGround);
   }

   public void addEndSegment(@Nonnull Vector3d position, boolean onGround, double distance) {
      this.newSegment().initAsEndSegment(position, onGround, distance);
   }

   public void addBlockedGroundSegment(@Nonnull Vector3d position, double distance, @Nonnull Vector3d normal, int blockId) {
      this.newSegment().initAsBlockedGroundSegment(position, distance, normal, blockId);
   }

   public void addHitGroundSegment(@Nonnull Vector3d position, double distance, @Nonnull Vector3d normal, int blockId) {
      this.newSegment().initAsHitGroundSegment(position, distance, normal, blockId);
   }

   public void addHitWallSegment(@Nonnull Vector3d position, boolean onGround, double distance, @Nonnull Vector3d normal, int blockId) {
      this.newSegment().initAsHitWallSegment(position, onGround, distance, normal, blockId);
   }

   public void addMoveSegment(@Nonnull Vector3d position, boolean onGround, double distance) {
      this.newSegment().initAsMoveSegment(position, onGround, distance);
   }

   public void addClimbSegment(@Nonnull Vector3d position, double distance, int blockId) {
      this.newSegment().initAsClimbSegment(position, distance, blockId);
   }

   public void addHitEdgeSegment(@Nonnull Vector3d position, double distance) {
      this.newSegment().initAsHitEdgeSegment(position, distance);
   }

   public void addDropSegment(@Nonnull Vector3d position, double distance) {
      this.newSegment().initAsDropSegment(position, distance);
   }

   public void addBlockedDropSegment(@Nonnull Vector3d position, double distance) {
      this.newSegment().initAsBlockedDropSegment(position, distance);
   }

   public void changeSegmentToBlockedWall() {
      this.segments[this.segmentCount - 1].type = ProbeMoveData.Segment.Type.BLOCKED_WALL;
   }

   public void changeSegmentToBlockedEdge() {
      this.segments[this.segmentCount - 1].type = ProbeMoveData.Segment.Type.BLOCKED_EDGE;
   }

   public double getLastDistance() {
      return this.segments[this.segmentCount - 1].distance;
   }

   protected ProbeMoveData.Segment newSegment() {
      if (this.segmentCount == this.segments.length) {
         this.segments = Arrays.copyOf(this.segments, this.segmentCount + 4);

         for (int i = this.segmentCount; i < this.segments.length; i++) {
            this.segments[i] = new ProbeMoveData.Segment();
         }
      }

      return this.segments[this.segmentCount++];
   }

   public static class Segment {
      public ProbeMoveData.Segment.Type type;
      public final Vector3d position = new Vector3d();
      public final Vector3d normal = new Vector3d();
      public double distance;
      public boolean onGround;
      public int blockId;

      public void initAsStartSegment(@Nonnull Vector3d position, boolean onGround) {
         this.type = ProbeMoveData.Segment.Type.START;
         this.position.assign(position);
         this.normal.assign(Vector3d.ZERO);
         this.distance = 0.0;
         this.onGround = onGround;
         this.blockId = Integer.MIN_VALUE;
      }

      public void initAsEndSegment(@Nonnull Vector3d position, boolean onGround, double distance) {
         this.type = ProbeMoveData.Segment.Type.END;
         this.position.assign(position);
         this.normal.assign(Vector3d.ZERO);
         this.distance = distance;
         this.onGround = onGround;
         this.blockId = Integer.MIN_VALUE;
      }

      public void initAsBlockedGroundSegment(@Nonnull Vector3d position, double distance, @Nonnull Vector3d normal, int blockId) {
         this.type = ProbeMoveData.Segment.Type.BLOCKED_GROUND;
         this.position.assign(position);
         this.normal.assign(normal);
         this.distance = distance;
         this.onGround = true;
         this.blockId = blockId;
      }

      public void initAsHitGroundSegment(@Nonnull Vector3d position, double distance, @Nonnull Vector3d normal, int blockId) {
         this.type = ProbeMoveData.Segment.Type.HIT_GROUND;
         this.position.assign(position);
         this.normal.assign(normal);
         this.distance = distance;
         this.onGround = true;
         this.blockId = blockId;
      }

      public void initAsHitWallSegment(@Nonnull Vector3d position, boolean onGround, double distance, @Nonnull Vector3d normal, int blockId) {
         this.type = ProbeMoveData.Segment.Type.HIT_WALL;
         this.position.assign(position);
         this.normal.assign(normal);
         this.distance = distance;
         this.onGround = onGround;
         this.blockId = blockId;
      }

      public void initAsClimbSegment(@Nonnull Vector3d position, double distance, int blockId) {
         this.type = ProbeMoveData.Segment.Type.CLIMB;
         this.position.assign(position);
         this.normal.assign(Vector3d.ZERO);
         this.distance = distance;
         this.onGround = true;
         this.blockId = blockId;
      }

      public void initAsMoveSegment(@Nonnull Vector3d position, boolean onGround, double distance) {
         this.type = ProbeMoveData.Segment.Type.MOVE;
         this.position.assign(position);
         this.normal.assign(Vector3d.ZERO);
         this.distance = distance;
         this.onGround = onGround;
         this.blockId = Integer.MIN_VALUE;
      }

      public void initAsDropSegment(@Nonnull Vector3d position, double distance) {
         this.type = ProbeMoveData.Segment.Type.DROP;
         this.position.assign(position);
         this.normal.assign(Vector3d.ZERO);
         this.distance = distance;
         this.onGround = true;
         this.blockId = Integer.MIN_VALUE;
      }

      public void initAsBlockedDropSegment(@Nonnull Vector3d position, double distance) {
         this.type = ProbeMoveData.Segment.Type.BLOCKED_DROP;
         this.position.assign(position);
         this.normal.assign(Vector3d.ZERO);
         this.distance = distance;
         this.onGround = false;
         this.blockId = Integer.MIN_VALUE;
      }

      public void initAsHitEdgeSegment(@Nonnull Vector3d position, double distance) {
         this.type = ProbeMoveData.Segment.Type.HIT_EDGE;
         this.position.assign(position);
         this.normal.assign(Vector3d.ZERO);
         this.distance = distance;
         this.onGround = true;
         this.blockId = Integer.MIN_VALUE;
      }

      public static enum Type {
         START(false, false),
         HIT_GROUND(false, true),
         MOVE(false, true),
         BLOCKED_GROUND(true, true),
         HIT_WALL(false, true),
         BLOCKED_WALL(true, true),
         CLIMB(false, false),
         HIT_EDGE(false, true),
         BLOCKED_EDGE(true, true),
         DROP(false, false),
         BLOCKED_DROP(true, false),
         END(false, true);

         protected final boolean isBlocked;
         protected final boolean canInterpolate;

         public boolean isBlocked() {
            return this.isBlocked;
         }

         public boolean canInterpolate() {
            return this.canInterpolate;
         }

         private Type(boolean isBlocked, boolean canInterpolate) {
            this.isBlocked = isBlocked;
            this.canInterpolate = canInterpolate;
         }
      }
   }
}
