package com.hypixel.hytale.server.npc.movement.controllers;

import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.modules.collision.CollisionConfig;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.movement.constraints.RelaxedConstraint;
import com.hypixel.hytale.server.npc.util.NPCPhysicsMath;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;

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
   private final EnumSet<RelaxedConstraint> relaxedConstraints = EnumSet.noneOf(RelaxedConstraint.class);
   @Nullable
   private Predicate<CollisionConfig> blockCollisionFilter;
   public boolean edgeBlocked;
   public boolean isSavingSegments;
   public int segmentCount;
   @Nullable
   public ProbeMoveData.Segment[] segments;
   public boolean debugCollision;

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

   @Nonnull
   public EnumSet<RelaxedConstraint> getRelaxedConstraints() {
      return this.relaxedConstraints;
   }

   public void setRelaxedConstraints(@Nonnull EnumSet<RelaxedConstraint> constraints) {
      this.relaxedConstraints.clear();
      this.relaxedConstraints.addAll(constraints);
   }

   @Nullable
   public Predicate<CollisionConfig> getBlockCollisionFilter() {
      return this.blockCollisionFilter;
   }

   public void setBlockCollisionFilter(@Nullable Predicate<CollisionConfig> filter) {
      this.blockCollisionFilter = filter;
   }

   @Nonnull
   public ProbeMoveData setPosition(@Nonnull Vector3dc position) {
      this.probePosition.set(position);
      this.initialPosition.set(position);
      return this;
   }

   @Nonnull
   public ProbeMoveData setDirection(@Nonnull Vector3dc direction) {
      this.probeDirection.set(direction);
      this.targetPosition.set(this.probePosition).add(this.probeDirection);
      return this;
   }

   @Nonnull
   public ProbeMoveData setTargetPosition(@Nonnull Vector3dc targetPosition) {
      this.targetPosition.set(targetPosition);
      this.probeDirection.set(targetPosition).sub(this.probePosition);
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
         result.set(this.segments[0].position);
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
            result.set(segment.position);
            return true;
         } else if (segment.type.canInterpolate()) {
            double lambda = (distance - prevSegment.distance) / (segment.distance - prevSegment.distance);
            NPCPhysicsMath.lerp(prevSegment.position, segment.position, lambda, result);
            return true;
         } else {
            result.set(prevSegment.position);
            return true;
         }
      }
   }

   public boolean startProbing() {
      this.edgeBlocked = false;
      if (this.isSavingSegments) {
         this.segmentCount = 0;
      }

      return this.isSavingSegments;
   }

   public void addStartSegment(@Nonnull Vector3dc position, boolean onGround) {
      this.newSegment().initAsStartSegment(position, onGround);
   }

   public void addEndSegment(@Nonnull Vector3dc position, boolean onGround, double distance) {
      this.newSegment().initAsEndSegment(position, onGround, distance);
   }

   public void addBlockedGroundSegment(@Nonnull Vector3dc position, double distance, @Nonnull Vector3dc normal, int blockId) {
      this.newSegment().initAsBlockedGroundSegment(position, distance, normal, blockId);
   }

   public void addHitGroundSegment(@Nonnull Vector3dc position, double distance, @Nonnull Vector3dc normal, int blockId) {
      this.newSegment().initAsHitGroundSegment(position, distance, normal, blockId);
   }

   public void addHitWallSegment(@Nonnull Vector3dc position, boolean onGround, double distance, @Nonnull Vector3dc normal, int blockId) {
      this.newSegment().initAsHitWallSegment(position, onGround, distance, normal, blockId);
   }

   public void addMoveSegment(@Nonnull Vector3dc position, boolean onGround, double distance) {
      this.newSegment().initAsMoveSegment(position, onGround, distance);
   }

   public void addClimbSegment(@Nonnull Vector3dc position, double distance, int blockId) {
      this.newSegment().initAsClimbSegment(position, distance, blockId);
   }

   public void addHitEdgeSegment(@Nonnull Vector3dc position, double distance) {
      this.newSegment().initAsHitEdgeSegment(position, distance);
   }

   public void addDropSegment(@Nonnull Vector3d position, double distance) {
      this.newSegment().initAsDropSegment(position, distance);
   }

   public void addBlockedDropSegment(@Nonnull Vector3d position, double distance) {
      this.edgeBlocked = true;
      this.newSegment().initAsBlockedDropSegment(position, distance);
   }

   public void changeSegmentToBlockedWall() {
      this.segments[this.segmentCount - 1].type = ProbeMoveData.Segment.Type.BLOCKED_WALL;
   }

   public void changeSegmentToBlockedEdge() {
      this.edgeBlocked = true;
      this.segments[this.segmentCount - 1].type = ProbeMoveData.Segment.Type.BLOCKED_EDGE;
   }

   public double getLastDistance() {
      return this.segments[this.segmentCount - 1].distance;
   }

   @Nonnull
   public String dump() {
      StringBuilder sb = new StringBuilder();
      sb.append("ProbeMoveData")
         .append("\nprobePosition=")
         .append(Vector3dUtil.formatShortString(this.probePosition))
         .append(", probeDirection=")
         .append(Vector3dUtil.formatShortString(this.probeDirection))
         .append(", initialPosition=")
         .append(Vector3dUtil.formatShortString(this.initialPosition))
         .append(", targetPosition=")
         .append(Vector3dUtil.formatShortString(this.targetPosition))
         .append("\ndirectionComponentSelector=")
         .append(Vector3dUtil.formatShortString(this.directionComponentSelector))
         .append(", edgeBlocked=")
         .append(this.edgeBlocked)
         .append(", isSavingSegments=")
         .append(this.isSavingSegments)
         .append(", segmentCount=")
         .append(this.segmentCount)
         .append("\nrelaxedConstraints=")
         .append(this.relaxedConstraints)
         .append(", blockCollisionFilter=")
         .append(this.blockCollisionFilter != null ? "set" : "none");
      if (this.segments != null && this.segmentCount > 0) {
         int maxIndex = Math.min(this.segmentCount, this.segments.length);
         if (maxIndex <= 0) {
            sb.append("\nsegments=none");
            return sb.toString();
         } else {
            sb.append("\nsegments:");

            for (int i = 0; i < maxIndex; i++) {
               ProbeMoveData.Segment segment = this.segments[i];
               if (segment == null) {
                  sb.append("\nsegment[").append(i).append("]=null");
               } else {
                  sb.append("\nsegment[")
                     .append(i)
                     .append("]: type=")
                     .append(segment.type)
                     .append(", distance=")
                     .append(segment.distance)
                     .append(", onGround=")
                     .append(segment.onGround)
                     .append(", blockId=")
                     .append(segment.blockId)
                     .append(", position=")
                     .append(Vector3dUtil.formatShortString(segment.position))
                     .append(", normal=")
                     .append(Vector3dUtil.formatShortString(segment.normal));
                  if (segment.type == ProbeMoveData.Segment.Type.CLIMB
                     || segment.type == ProbeMoveData.Segment.Type.DROP
                     || segment.type == ProbeMoveData.Segment.Type.BLOCKED_DROP) {
                     ProbeMoveData.Segment previousSegment = i > 0 ? this.segments[i - 1] : null;
                     double previousY = previousSegment != null ? previousSegment.position.y : this.initialPosition.y;
                     sb.append(", dy=").append(segment.position.y - previousY);
                  }
               }
            }

            return sb.toString();
         }
      } else {
         sb.append("\nsegments=none");
         return sb.toString();
      }
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

      public void initAsStartSegment(@Nonnull Vector3dc position, boolean onGround) {
         this.type = ProbeMoveData.Segment.Type.START;
         this.position.set(position);
         this.normal.zero();
         this.distance = 0.0;
         this.onGround = onGround;
         this.blockId = Integer.MIN_VALUE;
      }

      public void initAsEndSegment(@Nonnull Vector3dc position, boolean onGround, double distance) {
         this.type = ProbeMoveData.Segment.Type.END;
         this.position.set(position);
         this.normal.zero();
         this.distance = distance;
         this.onGround = onGround;
         this.blockId = Integer.MIN_VALUE;
      }

      public void initAsBlockedGroundSegment(@Nonnull Vector3dc position, double distance, @Nonnull Vector3dc normal, int blockId) {
         this.type = ProbeMoveData.Segment.Type.BLOCKED_GROUND;
         this.position.set(position);
         this.normal.set(normal);
         this.distance = distance;
         this.onGround = true;
         this.blockId = blockId;
      }

      public void initAsHitGroundSegment(@Nonnull Vector3dc position, double distance, @Nonnull Vector3dc normal, int blockId) {
         this.type = ProbeMoveData.Segment.Type.HIT_GROUND;
         this.position.set(position);
         this.normal.set(normal);
         this.distance = distance;
         this.onGround = true;
         this.blockId = blockId;
      }

      public void initAsHitWallSegment(@Nonnull Vector3dc position, boolean onGround, double distance, @Nonnull Vector3dc normal, int blockId) {
         this.type = ProbeMoveData.Segment.Type.HIT_WALL;
         this.position.set(position);
         this.normal.set(normal);
         this.distance = distance;
         this.onGround = onGround;
         this.blockId = blockId;
      }

      public void initAsClimbSegment(@Nonnull Vector3dc position, double distance, int blockId) {
         this.type = ProbeMoveData.Segment.Type.CLIMB;
         this.position.set(position);
         this.normal.zero();
         this.distance = distance;
         this.onGround = true;
         this.blockId = blockId;
      }

      public void initAsMoveSegment(@Nonnull Vector3dc position, boolean onGround, double distance) {
         this.type = ProbeMoveData.Segment.Type.MOVE;
         this.position.set(position);
         this.normal.zero();
         this.distance = distance;
         this.onGround = onGround;
         this.blockId = Integer.MIN_VALUE;
      }

      public void initAsDropSegment(@Nonnull Vector3dc position, double distance) {
         this.type = ProbeMoveData.Segment.Type.DROP;
         this.position.set(position);
         this.normal.zero();
         this.distance = distance;
         this.onGround = true;
         this.blockId = Integer.MIN_VALUE;
      }

      public void initAsBlockedDropSegment(@Nonnull Vector3dc position, double distance) {
         this.type = ProbeMoveData.Segment.Type.BLOCKED_DROP;
         this.position.set(position);
         this.normal.zero();
         this.distance = distance;
         this.onGround = false;
         this.blockId = Integer.MIN_VALUE;
      }

      public void initAsHitEdgeSegment(@Nonnull Vector3dc position, double distance) {
         this.type = ProbeMoveData.Segment.Type.HIT_EDGE;
         this.position.set(position);
         this.normal.zero();
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
