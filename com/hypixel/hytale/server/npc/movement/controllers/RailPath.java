package com.hypixel.hytale.server.npc.movement.controllers;

import java.util.Arrays;
import javax.annotation.Nonnull;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public final class RailPath {
   private static final double MIN_WAYPOINT_OFFSET_SQ = 1.0E-12;
   private static final double CARRY_OVER_FACTOR = 2.0;
   private static final double COLLINEARITY_EPSILON = 1.0E-9;
   private static final double SNAP_SLACK = 1.0E-9;
   private static final int DEFAULT_INITIAL_CAPACITY = 6;
   private static final int GROWTH_INCREMENT = 4;
   private Vector3d[] waypoints;
   private int waypointCount;
   private int cursor;
   private double segmentProgress;
   private double carryOverDistance;
   private final Vector3d segDirScratch = new Vector3d();
   private final Vector3d referenceDirScratch = new Vector3d();

   public RailPath() {
      this(6);
   }

   public RailPath(int initialCapacity) {
      if (initialCapacity < 1) {
         initialCapacity = 1;
      }

      this.waypoints = new Vector3d[initialCapacity];

      for (int i = 0; i < this.waypoints.length; i++) {
         this.waypoints[i] = new Vector3d();
      }
   }

   public void reset() {
      this.waypointCount = 0;
      this.cursor = 0;
      this.segmentProgress = 0.0;
      this.carryOverDistance = 0.0;
   }

   public void capture(@Nonnull ProbeMoveData data) {
      this.reset();
      ProbeMoveData.Segment[] segments = data.segments;
      int count = data.segmentCount;
      if (segments != null && count > 0) {
         this.nextWaypoint().set(segments[0].position);

         for (int i = 1; i < count; i++) {
            this.appendIfDistinct(segments[i].position);
         }
      }
   }

   public void capture(@Nonnull Vector3d[] waypoints, int count) {
      this.reset();
      if (count > 0) {
         this.nextWaypoint().set(waypoints[0]);

         for (int i = 1; i < count; i++) {
            this.appendIfDistinct(waypoints[i]);
         }
      }
   }

   private void appendIfDistinct(@Nonnull Vector3dc candidate) {
      Vector3d last = this.waypoints[this.waypointCount - 1];
      if (!(last.distanceSquared(candidate) <= 1.0E-12)) {
         this.nextWaypoint().set(candidate);
      }
   }

   public boolean isEmpty() {
      return this.waypointCount <= 1;
   }

   public boolean isFinished() {
      return this.isEmpty() || this.cursor >= this.waypointCount - 1;
   }

   public int getWaypointCount() {
      return this.waypointCount;
   }

   @Nonnull
   public Vector3dc getWaypoint(int index) {
      if (index >= 0 && index < this.waypointCount) {
         return this.waypoints[index];
      } else {
         throw new IndexOutOfBoundsException(index);
      }
   }

   public int getCursor() {
      return this.cursor;
   }

   public double getSegmentProgress() {
      return this.segmentProgress;
   }

   public void advance(@Nonnull Vector3dc currentPosition, double stepDistance, @Nonnull Vector3d out) {
      out.zero();
      if (this.waypointCount > 1 && !(stepDistance <= 0.0)) {
         double budget = stepDistance + this.carryOverDistance;
         this.carryOverDistance = 0.0;
         this.skipDegenerateSegments();
         if (this.cursor < this.waypointCount - 1) {
            this.referenceDirScratch.set(this.waypoints[this.cursor + 1]).sub(this.waypoints[this.cursor]);
            double referenceDirLen = this.referenceDirScratch.length();
            boolean anchored = false;

            do {
               Vector3d curr = this.waypoints[this.cursor];
               Vector3d next = this.waypoints[this.cursor + 1];
               double segLen = curr.distance(next);
               double leftOnSegment = segLen * (1.0 - this.segmentProgress);
               if (budget + 1.0E-9 < leftOnSegment) {
                  double fraction = budget / segLen;
                  this.segDirScratch.set(next).sub(curr);
                  out.fma(fraction, this.segDirScratch);
                  this.segmentProgress += fraction;
                  return;
               }

               if (!anchored) {
                  out.set(next).sub(currentPosition);
                  anchored = true;
               } else {
                  this.segDirScratch.set(next).sub(curr);
                  out.fma(1.0 - this.segmentProgress, this.segDirScratch);
               }

               this.cursor++;
               this.segmentProgress = 0.0;
               budget -= leftOnSegment;
               this.skipDegenerateSegments();
               if (this.cursor >= this.waypointCount - 1 || budget <= 1.0E-9) {
                  this.carryOverDistance = Math.min(Math.max(budget, 0.0), stepDistance * 2.0);
                  return;
               }
            } while (this.collinearWithReference(this.waypoints[this.cursor], this.waypoints[this.cursor + 1], referenceDirLen));

            this.carryOverDistance = Math.min(budget, stepDistance * 2.0);
         }
      }
   }

   private void skipDegenerateSegments() {
      while (this.cursor < this.waypointCount - 1 && this.waypoints[this.cursor].distanceSquared(this.waypoints[this.cursor + 1]) <= 1.0E-12) {
         this.cursor++;
         this.segmentProgress = 0.0;
      }
   }

   private boolean collinearWithReference(@Nonnull Vector3dc candStart, @Nonnull Vector3dc candEnd, double referenceDirLen) {
      this.segDirScratch.set(candEnd).sub(candStart);
      double candLen = this.segDirScratch.length();
      if (candLen <= 1.0E-6) {
         return false;
      } else {
         double cosTheta = this.referenceDirScratch.dot(this.segDirScratch) / (referenceDirLen * candLen);
         return cosTheta >= 0.999999999;
      }
   }

   public void snapY(@Nonnull Vector3d position) {
      if (this.cursor < this.waypointCount - 1) {
         Vector3d curr = this.waypoints[this.cursor];
         Vector3d next = this.waypoints[this.cursor + 1];
         double expectedY = curr.y + (next.y - curr.y) * this.segmentProgress;
         if (Math.abs(position.y - expectedY) > 1.0E-6) {
            position.y = expectedY;
         }
      }
   }

   private Vector3d nextWaypoint() {
      if (this.waypointCount == this.waypoints.length) {
         int oldLen = this.waypoints.length;
         this.waypoints = Arrays.copyOf(this.waypoints, oldLen + 4);

         for (int i = oldLen; i < this.waypoints.length; i++) {
            this.waypoints[i] = new Vector3d();
         }
      }

      return this.waypoints[this.waypointCount++];
   }
}
