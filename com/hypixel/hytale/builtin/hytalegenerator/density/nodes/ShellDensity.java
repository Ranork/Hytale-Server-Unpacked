package com.hypixel.hytale.builtin.hytalegenerator.density.nodes;

import com.hypixel.hytale.builtin.hytalegenerator.VectorUtil;
import com.hypixel.hytale.builtin.hytalegenerator.density.Density;
import com.hypixel.hytale.builtin.hytalegenerator.framework.math.Calculator;
import com.hypixel.hytale.math.vector.Vector3d;
import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import javax.annotation.Nonnull;

public class ShellDensity extends Density {
   public static final double ZERO_DELTA = 1.0E-9;
   @Nonnull
   private final Double2DoubleFunction angleCurve;
   @Nonnull
   private final Double2DoubleFunction distanceCurve;
   @Nonnull
   private final Vector3d axis;
   private final boolean isMirrored;
   @Nonnull
   private final Vector3d rRadialVector;

   public ShellDensity(@Nonnull Double2DoubleFunction angleCurve, @Nonnull Double2DoubleFunction distanceCurve, @Nonnull Vector3d axis, boolean isMirrored) {
      this.angleCurve = angleCurve;
      this.distanceCurve = distanceCurve;
      this.axis = axis;
      this.isMirrored = isMirrored;
      this.rRadialVector = new Vector3d();
   }

   @Override
   public double process(@Nonnull Density.Context context) {
      double distance = Calculator.distance(context.position, Vector3d.ZERO);
      if (this.axis.length() == 0.0) {
         return 0.0;
      } else {
         this.rRadialVector.assign(context.position);
         double amplitude = this.distanceCurve.applyAsDouble(distance);
         if (amplitude == 0.0) {
            return 0.0;
         } else if (this.rRadialVector.length() <= 1.0E-9) {
            return amplitude;
         } else {
            double angle = VectorUtil.angle(this.rRadialVector, this.axis);
            angle /= Math.PI;
            angle *= 180.0;
            if (this.isMirrored && angle > 90.0) {
               angle = 180.0 - angle;
            }

            return amplitude * this.angleCurve.applyAsDouble(angle);
         }
      }
   }
}
