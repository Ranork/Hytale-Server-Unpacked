package com.hypixel.hytale.server.worldgen.cave.shape.distorted;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.procedurallib.logic.GeneralNoise;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import javax.annotation.Nonnull;

public abstract class AbstractDistortedExtrusion extends AbstractDistortedShape {
   protected final GeneralNoise.InterpolationFunction interpolation;

   public AbstractDistortedExtrusion(@Nonnull Vector3d o, @Nonnull Vector3d v, double width, double height, GeneralNoise.InterpolationFunction interpolation) {
      super(o, v, width, height);
      this.interpolation = interpolation;
   }

   protected abstract double getDistanceSq(double var1, double var3, double var5);

   protected abstract double getHeightComponent(double var1, double var3, double var5);

   @Override
   public double getHeightAtProjection(int seed, double x, double z, double t, double centerY, @Nonnull CaveType caveType, @Nonnull ShapeDistortion distortion) {
      double width = this.getWidthAt(t);
      width *= caveType.getHeightRadiusFactor(seed, x, z, MathUtil.floor(centerY));
      double dist2 = this.getDistanceSq(x, z, t);
      double width2 = width * width;
      if (dist2 > width2) {
         return 0.0;
      } else {
         width *= distortion.getWidthFactor(seed, x, z);
         width2 = width * width;
         if (dist2 > width2) {
            return 0.0;
         } else {
            double height = this.getHeightAt(t);
            if (height == 0.0) {
               return 0.0;
            } else {
               double alpha = this.getHeightComponent(width, width2, dist2);
               alpha = MathUtil.clamp(alpha, 0.0, 1.0);
               alpha = this.interpolation.interpolate(alpha);
               return height * alpha;
            }
         }
      }
   }
}
