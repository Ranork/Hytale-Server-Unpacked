package com.hypixel.hytale.server.worldgen.cave.shape.distorted;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.procedurallib.logic.GeneralNoise;
import com.hypixel.hytale.server.worldgen.cave.shape.CaveNodeShapeUtils;
import javax.annotation.Nonnull;

public class DistortedCylinderShape extends AbstractDistortedExtrusion {
   protected static final double PITCH_COMPENSATION_MIN = 1.0;
   protected static final double PITCH_COMPENSATION_RANGE = 3.0;
   @Nonnull
   protected final Vector3d o;
   @Nonnull
   protected final Vector3d v;
   protected final double startWidth;
   protected final double startHeight;
   protected final double midWidth;
   protected final double midHeight;
   protected final double endWidth;
   protected final double endHeight;

   public DistortedCylinderShape(
      @Nonnull Vector3d o,
      @Nonnull Vector3d v,
      double startWidth,
      double startHeight,
      double midWidth,
      double midHeight,
      double endWidth,
      double endHeight,
      GeneralNoise.InterpolationFunction interpolation
   ) {
      this(
         o,
         v,
         startWidth,
         startHeight,
         midWidth,
         midHeight,
         endWidth,
         endHeight,
         MathUtil.maxValue(startWidth, midWidth, endWidth),
         MathUtil.maxValue(startHeight, midHeight, endHeight),
         interpolation
      );
   }

   public DistortedCylinderShape(
      @Nonnull Vector3d o,
      @Nonnull Vector3d v,
      double startWidth,
      double startHeight,
      double midWidth,
      double midHeight,
      double endWidth,
      double endHeight,
      double maxWidth,
      double maxHeight,
      GeneralNoise.InterpolationFunction interpolation
   ) {
      super(o, v, maxWidth, maxHeight, interpolation);
      this.o = o;
      this.v = v;
      this.startWidth = startWidth;
      this.startHeight = startHeight;
      this.midWidth = midWidth;
      this.midHeight = midHeight;
      this.endWidth = endWidth;
      this.endHeight = endHeight;
   }

   @Nonnull
   @Override
   public Vector3d getStart() {
      return this.o.clone();
   }

   @Nonnull
   @Override
   public Vector3d getEnd() {
      double x = this.o.x + this.v.x;
      double y = this.o.y + this.v.y;
      double z = this.o.z + this.v.z;
      return new Vector3d(x, y, z);
   }

   @Nonnull
   @Override
   public Vector3d getAnchor(@Nonnull Vector3d vector, double t, double tv, double th) {
      double radiusY = this.getHeightAt(t);
      double radiusXZ = this.getWidthAt(t);
      return CaveNodeShapeUtils.getPipeAnchor(vector, this.o, this.v, radiusXZ, radiusY, radiusXZ, t, tv, th);
   }

   @Override
   public double getProjection(double x, double z) {
      double t = (x - this.o.x) * this.v.x + (z - this.o.z) * this.v.z;
      return t / (this.v.x * this.v.x + this.v.z * this.v.z);
   }

   @Override
   public boolean isValidProjection(double t) {
      return t > 0.0 && t < 1.0;
   }

   @Override
   public double getYAt(double t) {
      return this.o.y + this.v.y * t;
   }

   @Override
   public double getWidthAt(double t) {
      return getDimAt(t, this.startWidth, this.midWidth, this.endWidth, this.interpolation);
   }

   @Override
   public double getHeightAt(double t) {
      return getDimAt(t, this.startHeight, this.midHeight, this.endHeight, this.interpolation);
   }

   @Override
   public double getDistanceSq(double x, double z, double t) {
      if (t <= 0.0) {
         x -= this.o.x;
         z -= this.o.z;
      } else if (t >= 1.0) {
         x -= this.o.x + this.v.x;
         z -= this.o.z + this.v.z;
      } else {
         x -= this.o.x + this.v.x * t;
         z -= this.o.z + this.v.z * t;
      }

      return x * x + z * z;
   }

   @Override
   public double getHeightComponent(double width, double width2, double dist2) {
      return Math.sqrt(width2 - dist2) / width;
   }

   @Nonnull
   @Override
   public String toString() {
      return "DistortedCylinderShape{origin="
         + this.o
         + ", direction="
         + this.v
         + ", startWidth="
         + this.startWidth
         + ", startHeight="
         + this.startHeight
         + ", midWidth="
         + this.midWidth
         + ", midHeight="
         + this.midHeight
         + ", endWidth="
         + this.endWidth
         + ", endHeight="
         + this.endHeight
         + "}";
   }

   protected static double getDimAt(double t, double startDim, double midDim, double endDim, @Nonnull GeneralNoise.InterpolationFunction interpolation) {
      if (t <= 0.0) {
         return startDim;
      } else if (t >= 1.0) {
         return endDim;
      } else if (t <= 0.5) {
         t = interpolation.interpolate(t * 2.0);
         return MathUtil.lerpUnclamped(startDim, midDim, t);
      } else {
         t = interpolation.interpolate((t - 0.5) * 2.0);
         return MathUtil.lerpUnclamped(midDim, endDim, t);
      }
   }

   protected static double getCompensationFactor(@Nonnull Vector3d direction) {
      double ny = direction.y / direction.length();
      double pitch = TrigMathUtil.asin(-ny);
      return Math.abs(pitch) / (float) (Math.PI / 2);
   }

   protected static double getHeightCompensation(double factor) {
      return 1.0 + factor * factor * factor * 3.0;
   }

   public static class Factory implements DistortedShape.Factory {
      @Nonnull
      @Override
      public DistortedShape create(
         @Nonnull Vector3d origin,
         @Nonnull Vector3d direction,
         double length,
         double startWidth,
         double startHeight,
         double midWidth,
         double midHeight,
         double endWidth,
         double endHeight,
         GeneralNoise.InterpolationFunction interpolation
      ) {
         double comp = DistortedCylinderShape.getCompensationFactor(direction);
         double scale = DistortedCylinderShape.getHeightCompensation(comp);
         startHeight *= scale;
         midHeight *= scale;
         endHeight *= scale;
         return new DistortedCylinderShape(origin, direction, startWidth, startHeight, midWidth, midHeight, endWidth, endHeight, interpolation);
      }
   }
}
