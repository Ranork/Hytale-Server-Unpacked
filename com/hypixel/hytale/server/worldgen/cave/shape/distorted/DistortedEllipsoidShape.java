package com.hypixel.hytale.server.worldgen.cave.shape.distorted;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.procedurallib.logic.GeneralNoise;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import com.hypixel.hytale.server.worldgen.cave.shape.CaveNodeShapeUtils;
import javax.annotation.Nonnull;

public class DistortedEllipsoidShape extends AbstractDistortedBody {
   private final double radiusX;
   private final double radiusY;
   private final double radiusZ;
   private final double radiusX2;
   private final double radiusY2;
   private final double radiusZ2;
   private final double invRadiusX2;
   private final double invRadiusZ2;
   private final GeneralNoise.InterpolationFunction interpolation;

   public DistortedEllipsoidShape(
      @Nonnull Vector3d o,
      Vector3d d,
      double yaw,
      double pitch,
      double radiusX,
      double radiusY,
      double radiusZ,
      GeneralNoise.InterpolationFunction interpolation
   ) {
      super(o, d, yaw, pitch, radiusX, radiusY, radiusZ);
      this.radiusX = radiusX;
      this.radiusY = radiusY;
      this.radiusZ = radiusZ;
      this.radiusX2 = radiusX * radiusX;
      this.radiusY2 = radiusY * radiusY;
      this.radiusZ2 = radiusZ * radiusZ;
      this.invRadiusX2 = 1.0 / this.radiusX2;
      this.invRadiusZ2 = 1.0 / this.radiusZ2;
      this.interpolation = interpolation;
   }

   @Nonnull
   @Override
   public Vector3d getAnchor(@Nonnull Vector3d vector, double tx, double ty, double tz) {
      return CaveNodeShapeUtils.getSphereAnchor(vector, this.o, this.radiusX, this.radiusY, this.radiusZ, tx, ty, tz);
   }

   @Override
   public double getProjection(double x, double z) {
      return 0.0;
   }

   @Override
   public boolean isValidProjection(double t) {
      return true;
   }

   @Override
   public double getYAt(double t) {
      return this.o.y;
   }

   @Override
   public double getWidthAt(double t) {
      return Math.min(this.radiusX, this.radiusZ);
   }

   @Override
   public double getHeightAt(double t) {
      return this.radiusY;
   }

   @Override
   protected double getHeight(int seed, double x, double z, double t, double centerY, CaveType caveType, @Nonnull ShapeDistortion distortion) {
      double dx = x - this.o.x;
      double dz = z - this.o.z;
      double dx2 = dx * dx;
      if (dx2 > this.radiusX2) {
         return 0.0;
      } else {
         double dz2 = dz * dz;
         if (dz2 > this.radiusZ2) {
            return 0.0;
         } else {
            double qx = this.interpolation.interpolate(dx2 * this.invRadiusX2);
            double qz = this.interpolation.interpolate(dz2 * this.invRadiusZ2);
            double qh = qx + qz;
            double noise = distortion.getWidthFactor(seed, x, z);
            if (noise > 0.0) {
               qh /= noise;
            }

            double y2 = (1.0 - qh) * this.radiusY2;
            return y2 <= 0.0 ? 0.0 : Math.sqrt(y2);
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "DistortedEllipsoidShape{origin="
         + this.o
         + ", direction="
         + this.v
         + ", radiusX="
         + this.radiusX
         + ", radiusY="
         + this.radiusY
         + ", radiusZ="
         + this.radiusZ
         + "}";
   }

   private static double wrapPitch(double pitch, double radiusY, double radiusZ) {
      double min = Math.min(radiusY, radiusZ);
      double max = Math.max(radiusY, radiusZ);
      double aspect = 1.0 - min / max;
      if (pitch < (float) (-Math.PI / 4)) {
         double alpha = 1.0 - Math.abs((pitch + (float) (Math.PI / 4)) / (float) (Math.PI / 4));
         return (float) (-Math.PI / 2) * aspect * alpha;
      } else if (pitch > (float) (Math.PI / 4)) {
         double alpha = 1.0 - Math.abs((pitch - (float) (Math.PI / 4)) / (float) (Math.PI / 4));
         return (float) (Math.PI / 2) * aspect * alpha;
      } else {
         return pitch;
      }
   }

   public static class Factory extends AbstractDistortedBody.Factory {
      @Nonnull
      @Override
      protected DistortedShape createShape(
         @Nonnull Vector3d origin,
         Vector3d direction,
         double yaw,
         double pitch,
         double radiusX,
         double radiusY,
         double radiusZ,
         GeneralNoise.InterpolationFunction interpolation
      ) {
         if (pitch < (float) (-Math.PI / 4)) {
            radiusY = radiusZ;
            radiusZ = radiusY;
            pitch = DistortedEllipsoidShape.wrapPitch(pitch, radiusZ, radiusY);
         } else if (pitch > (float) (Math.PI / 4)) {
            radiusY = radiusZ;
            radiusZ = radiusY;
            pitch = DistortedEllipsoidShape.wrapPitch(pitch, radiusZ, radiusY);
         } else {
            double alpha = 1.0 - Math.abs(pitch / (float) (Math.PI / 2));
            radiusY /= alpha;
            radiusZ *= alpha;
         }

         return new DistortedEllipsoidShape(origin, direction, yaw, pitch, radiusX, radiusY, radiusZ, interpolation);
      }
   }
}
