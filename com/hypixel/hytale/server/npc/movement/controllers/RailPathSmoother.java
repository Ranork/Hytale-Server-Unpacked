package com.hypixel.hytale.server.npc.movement.controllers;

import java.util.Arrays;
import javax.annotation.Nonnull;
import org.joml.Vector3d;
import org.joml.Vector3dc;

public final class RailPathSmoother {
   private static final int DEFAULT_INITIAL_CAPACITY = 8;
   private static final int GROWTH_INCREMENT = 4;
   private static final double VERTICAL_EPSILON = 1.0E-6;
   private static final double SAME_S_EPSILON = 1.0E-6;
   private static final double OVERLAP_EPSILON = 1.0E-6;
   private Vector3d[] waypoints;
   private int waypointCount;
   private double[] segmentS;
   private double[] runStart;
   private double[] runEnd;
   private double[] runY;
   private int runCount;
   private final Vector3d horizDir = new Vector3d();
   private final Vector3d startPos = new Vector3d();

   public RailPathSmoother() {
      this(8);
   }

   public RailPathSmoother(int initialCapacity) {
      if (initialCapacity < 1) {
         initialCapacity = 1;
      }

      this.waypoints = new Vector3d[initialCapacity];

      for (int i = 0; i < this.waypoints.length; i++) {
         this.waypoints[i] = new Vector3d();
      }

      this.segmentS = new double[initialCapacity];
      this.runStart = new double[initialCapacity];
      this.runEnd = new double[initialCapacity];
      this.runY = new double[initialCapacity];
   }

   public void reset() {
      this.waypointCount = 0;
      this.runCount = 0;
   }

   @Nonnull
   public Vector3d[] getWaypoints() {
      return this.waypoints;
   }

   public int getWaypointCount() {
      return this.waypointCount;
   }

   public void smooth(@Nonnull ProbeMoveData data, @Nonnull MotionController motionController, @Nonnull RailPathSmoother.Config config) {
      this.reset();
      ProbeMoveData.Segment[] segments = data.segments;
      int count = data.segmentCount;
      if (segments != null && count > 0) {
         if (count == 1) {
            this.nextWaypoint().set(segments[0].position);
         } else {
            Vector3d compSel = motionController.getComponentSelector();
            this.horizDir
               .set(
                  (data.targetPosition.x - data.initialPosition.x) * compSel.x,
                  (data.targetPosition.y - data.initialPosition.y) * compSel.y,
                  (data.targetPosition.z - data.initialPosition.z) * compSel.z
               );
            double horizLen = this.horizDir.length();
            boolean canSmooth = motionController.is2D() && horizLen > 1.0E-6 && (config.climbSlope > 0.0 || config.dropSlope > 0.0);
            if (!canSmooth) {
               for (int i = 0; i < count; i++) {
                  this.nextWaypoint().set(segments[i].position);
               }
            } else {
               this.horizDir.div(horizLen);
               this.startPos.set(segments[0].position);
               this.ensureSegmentCapacity(count);

               for (int i = 0; i < count; i++) {
                  this.segmentS[i] = this.projectToPathS(segments[i].position);
               }

               this.buildRuns(segments, count);
               if (this.runCount <= 1) {
                  this.nextWaypoint().set(segments[0].position);
                  this.nextWaypoint().set(segments[count - 1].position);
               } else {
                  double pathLengthS = this.segmentS[count - 1];
                  double maxClimbHeight = motionController.getMaxClimbHeight();
                  double maxDropHeight = motionController.getMaxDropHeight();
                  this.emitWaypoint(0.0, segments[0].position.y);
                  int cursor = 0;

                  while (cursor < this.runCount - 1) {
                     double yFrom = this.runY[cursor];
                     double yTo = this.runY[cursor + 1];
                     double sEdge = this.runEnd[cursor];
                     if (yFrom > yTo) {
                        cursor = this.processDrop(cursor, sEdge, config, maxClimbHeight, maxDropHeight);
                     } else {
                        cursor = this.processClimb(cursor, sEdge, config, maxClimbHeight);
                     }
                  }

                  this.emitWaypoint(pathLengthS, segments[count - 1].position.y);
                  if (this.waypointCount > 0) {
                     this.waypoints[0].set(segments[0].position);
                  }

                  if (this.waypointCount > 1) {
                     this.waypoints[this.waypointCount - 1].set(segments[count - 1].position);
                  }
               }
            }
         }
      }
   }

   private int processDrop(int cursor, double sEdge, RailPathSmoother.Config config, double maxClimbHeight, double maxDropHeight) {
      double yTop = this.runY[cursor];
      double yBot = this.runY[cursor + 1];
      double dy = yTop - yBot;
      if (cursor + 2 < this.runCount && this.runY[cursor + 1] < this.runY[cursor + 2] - 1.0E-6 && Math.abs(this.runY[cursor + 2] - yTop) <= 1.0E-6) {
         double lowerRunLen = this.runEnd[cursor + 1] - this.runStart[cursor + 1];
         if (lowerRunLen <= config.horizontalSkipGapWidth + 1.0E-6) {
            return cursor + 2;
         }
      }

      int target = -1;
      double targetS = 0.0;
      if (config.dropSlope > 0.0) {
         for (int k = cursor + 1; k < this.runCount; k++) {
            double yK = this.runY[k];
            if (yK > yTop + 1.0E-6) {
               break;
            }

            double dyK = yTop - yK;
            if (maxDropHeight > 0.0 && dyK > maxDropHeight + 1.0E-6) {
               break;
            }

            double sStart = this.runStart[k];
            double sEnd = this.runEnd[k];
            double yTrajStart = yTop - (sStart - sEdge) / config.dropSlope;
            double yTrajEnd = yTop - (sEnd - sEdge) / config.dropSlope;
            if (yTrajStart < yK - 1.0E-6) {
               break;
            }

            if (!(yTrajEnd >= yK - 1.0E-6)) {
               target = k;
               targetS = sEdge + dyK * config.dropSlope;
               break;
            }
         }
      }

      if (target > cursor + 1) {
         this.emitWaypoint(sEdge, yTop);
         this.emitWaypoint(targetS, this.runY[target]);
         return target;
      } else {
         boolean isPair = cursor + 2 < this.runCount && this.runY[cursor + 1] < this.runY[cursor + 2] - 1.0E-6;
         if (!isPair) {
            this.emitWaypoint(sEdge, yTop);
            if (target == cursor + 1) {
               this.emitWaypoint(targetS, yBot);
            } else {
               double sLanding;
               if (config.dropSlope > 0.0) {
                  double effDy = maxDropHeight > 0.0 ? Math.min(dy, maxDropHeight) : dy;
                  sLanding = sEdge + effDy * config.dropSlope;
                  if (sLanding > this.runEnd[cursor + 1]) {
                     sLanding = this.runEnd[cursor + 1];
                  }
               } else {
                  sLanding = sEdge;
               }

               this.emitWaypoint(sLanding, yBot);
            }

            return cursor + 1;
         } else {
            double yClimbTop = this.runY[cursor + 2];
            double dyClimb = yClimbTop - yBot;
            double sEdgeClimb = this.runEnd[cursor + 1];
            double climbSkipDelta = yClimbTop - yTop;
            if (climbSkipDelta > 1.0E-6 && config.climbSlope > 0.0 && (maxClimbHeight <= 0.0 || climbSkipDelta <= maxClimbHeight + 1.0E-6)) {
               double idealHorizSpan = climbSkipDelta * config.climbSlope;
               double dipSpan = sEdgeClimb - sEdge;
               if (idealHorizSpan >= dipSpan - 1.0E-6) {
                  double sStartIdeal = sEdgeClimb - idealHorizSpan;
                  double sStartx = Math.max(this.runStart[cursor], sStartIdeal);
                  this.emitWaypoint(sStartx, yTop);
                  this.emitWaypoint(sEdgeClimb, yClimbTop);
                  return cursor + 2;
               }
            }

            double effDyDrop = maxDropHeight > 0.0 ? Math.min(dy, maxDropHeight) : dy;
            double effDyClimb = maxClimbHeight > 0.0 ? Math.min(dyClimb, maxClimbHeight) : dyClimb;
            double sDropIdeal = sEdge + effDyDrop * config.dropSlope;
            double sClimbIdeal = sEdgeClimb - effDyClimb * config.climbSlope;
            boolean overlap = sDropIdeal > sClimbIdeal + 1.0E-6;
            this.emitWaypoint(sEdge, yTop);
            if (overlap) {
               double sMid = (sDropIdeal + sClimbIdeal) * 0.5;
               if (sMid < sEdge) {
                  sMid = sEdge;
               }

               if (sMid > sEdgeClimb) {
                  sMid = sEdgeClimb;
               }

               this.emitWaypoint(sMid, yBot);
            } else {
               double sDropLand = config.dropSlope > 0.0 ? sDropIdeal : sEdge;
               if (sDropLand < sEdge) {
                  sDropLand = sEdge;
               }

               if (sDropLand > sEdgeClimb) {
                  sDropLand = sEdgeClimb;
               }

               this.emitWaypoint(sDropLand, yBot);
               double sClimbStart = config.climbSlope > 0.0 ? sClimbIdeal : sEdgeClimb;
               if (sClimbStart < sEdge) {
                  sClimbStart = sEdge;
               }

               if (sClimbStart > sEdgeClimb) {
                  sClimbStart = sEdgeClimb;
               }

               if (sClimbStart > sDropLand + 1.0E-6) {
                  this.emitWaypoint(sClimbStart, yBot);
               }
            }

            this.emitWaypoint(sEdgeClimb, yClimbTop);
            return cursor + 2;
         }
      }
   }

   private int processClimb(int cursor, double sEdge, RailPathSmoother.Config config, double maxClimbHeight) {
      double yBot = this.runY[cursor];
      double yTop = this.runY[cursor + 1];
      double dy = yTop - yBot;
      double sStart;
      if (config.climbSlope > 0.0) {
         double effDy = maxClimbHeight > 0.0 ? Math.min(dy, maxClimbHeight) : dy;
         sStart = sEdge - effDy * config.climbSlope;
         if (sStart < this.runStart[cursor]) {
            sStart = this.runStart[cursor];
         }
      } else {
         sStart = sEdge;
      }

      this.emitWaypoint(sStart, yBot);
      this.emitWaypoint(sEdge, yTop);
      return cursor + 1;
   }

   private void buildRuns(@Nonnull ProbeMoveData.Segment[] segments, int count) {
      this.ensureRunCapacity(count);
      this.runCount = 0;
      int i = 0;

      while (i < count) {
         double yRun = segments[i].position.y;
         int j = i + 1;

         while (j < count && Math.abs(segments[j].position.y - yRun) <= 1.0E-6) {
            j++;
         }

         int lastSegmentIdx = j - 1;
         this.runStart[this.runCount] = this.segmentS[i];
         this.runEnd[this.runCount] = this.segmentS[lastSegmentIdx];
         this.runY[this.runCount] = yRun;
         this.runCount++;
         i = j;
      }
   }

   private void emitWaypoint(double s, double y) {
      Vector3d out = this.nextWaypoint();
      out.x = this.startPos.x + s * this.horizDir.x;
      out.y = y;
      out.z = this.startPos.z + s * this.horizDir.z;
   }

   private double projectToPathS(@Nonnull Vector3dc pos) {
      double dx = pos.x() - this.startPos.x;
      double dy = pos.y() - this.startPos.y;
      double dz = pos.z() - this.startPos.z;
      return dx * this.horizDir.x + dy * this.horizDir.y + dz * this.horizDir.z;
   }

   private void ensureSegmentCapacity(int needed) {
      if (this.segmentS.length < needed) {
         this.segmentS = new double[needed];
      }
   }

   private void ensureRunCapacity(int needed) {
      if (this.runStart.length < needed) {
         this.runStart = new double[needed];
         this.runEnd = new double[needed];
         this.runY = new double[needed];
      }
   }

   @Nonnull
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

   public static final class Config {
      public double climbSlope;
      public double dropSlope;
      public double horizontalSkipGapWidth;

      public void reset() {
         this.climbSlope = 0.0;
         this.dropSlope = 0.0;
         this.horizontalSkipGapWidth = 0.0;
      }
   }
}
