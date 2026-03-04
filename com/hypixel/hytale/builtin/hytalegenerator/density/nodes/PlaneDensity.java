package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.VectorUtil;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.math.vector.Vector3d;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlaneDensity extends Density {
   public static final double ZERO_DELTA = 1.0E-9;
   @Nonnull
   private static final Vector3d ZERO_VECTOR = new Vector3d();
   @Nonnull
   private final Double2DoubleFunction distanceCurve;
   @Nonnull
   private final Vector3d planeNormal;
   private final boolean isPlaneHorizontal;
   private final boolean isAnchored;
   @Nonnull
   private final Vector3d rNearestPoint;
   @Nonnull
   private final Vector3d rPosition;
   @Nonnull
   private final Vector3d rVectorFromPlane;
   @Nonnull
   private final Vector3d r0;
   @Nonnull
   private final Vector3d r1;
   @Nonnull
   private final Vector3d r2;
   @Nonnull
   private final Vector3d r3;

   public PlaneDensity(@Nonnull Double2DoubleFunction distanceCurve, @Nonnull Vector3d planeNormal, boolean isAnchored) {
      this.distanceCurve = distanceCurve;
      this.planeNormal = planeNormal;
      this.isPlaneHorizontal = planeNormal.x == 0.0 && planeNormal.z == 0.0;
      this.isAnchored = isAnchored;
      this.rNearestPoint = new Vector3d();
      this.rPosition = new Vector3d();
      this.rVectorFromPlane = new Vector3d();
      this.r0 = new Vector3d();
      this.r1 = new Vector3d();
      this.r2 = new Vector3d();
      this.r3 = new Vector3d();
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      if (this.planeNormal.length() == 0.0) {
         return 0.0;
      } else if (this.isAnchored) {
         return this.processAnchored(context.position.x, context.position.y, context.position.z, context);
      } else {
         double distance = 0.0;
         if (this.isPlaneHorizontal) {
            distance = context.position.y;
         } else {
            VectorUtil.nearestPointOnLine3d(context.position, ZERO_VECTOR, this.planeNormal, this.rNearestPoint, this.r0, this.r1, this.r2, this.r3);
            distance = this.rNearestPoint.length();
         }

         return this.distanceCurve.get(distance);
      }
   }

   private double processAnchored(double x, double y, double z, @Nullable Density.Context context) {
      if (context == null) {
         return 0.0;
      } else {
         this.rPosition.assign(x, y, z);
         Vector3d p0 = context.densityAnchor;
         if (p0 == null) {
            return 0.0;
         } else {
            double distance = 0.0;
            if (this.isPlaneHorizontal) {
               distance = Math.abs(p0.y - this.rPosition.y);
            }

            this.rPosition.subtract(p0);
            VectorUtil.nearestPointOnLine3d(this.rPosition, ZERO_VECTOR, this.planeNormal, this.rVectorFromPlane, this.r0, this.r1, this.r2, this.r3);
            distance = this.rVectorFromPlane.length();
            return this.distanceCurve.get(distance);
         }
      }
   }
}
