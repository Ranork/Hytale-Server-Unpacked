package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.VectorUtil;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.math.vector.Vector3d;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import javax.annotation.Nonnull;

public class AxisDensity extends Density {
   public static final double ZERO_DELTA = 1.0E-9;
   @Nonnull
   private static final Vector3d ZERO_VECTOR = new Vector3d();
   @Nonnull
   private final Double2DoubleFunction distanceCurve;
   @Nonnull
   private final Vector3d axis;
   private final boolean isAnchored;
   @Nonnull
   private final Vector3d rPosition;
   @Nonnull
   private final Vector3d r0;
   @Nonnull
   private final Vector3d r1;
   @Nonnull
   private final Vector3d r2;
   @Nonnull
   private final Vector3d r3;
   @Nonnull
   private final Vector3d r4;

   public AxisDensity(@Nonnull Double2DoubleFunction distanceCurve, @Nonnull Vector3d axis, boolean isAnchored) {
      this.distanceCurve = distanceCurve;
      this.axis = axis;
      this.isAnchored = isAnchored;
      this.rPosition = new Vector3d();
      this.r0 = new Vector3d();
      this.r1 = new Vector3d();
      this.r2 = new Vector3d();
      this.r3 = new Vector3d();
      this.r4 = new Vector3d();
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      if (this.axis.length() == 0.0) {
         return 0.0;
      } else if (this.isAnchored) {
         return this.processAnchored(context);
      } else {
         double distance = VectorUtil.distanceToLine3d(context.position, ZERO_VECTOR, this.axis, this.r0, this.r1, this.r2, this.r3, this.r4);
         return this.distanceCurve.get(distance);
      }
   }

   private double processAnchored(@Nonnull Density.Context context) {
      if (context == null) {
         return 0.0;
      } else {
         Vector3d anchor = context.densityAnchor;
         if (anchor == null) {
            return 0.0;
         } else {
            this.rPosition.assign(context.position).subtract(anchor);
            double distance = VectorUtil.distanceToLine3d(this.rPosition, ZERO_VECTOR, this.axis, this.r0, this.r1, this.r2, this.r3, this.r4);
            return this.distanceCurve.get(distance);
         }
      }
   }
}
