package com.hypixel.hytale.server.worldgen.cave.shape.distorted;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.procedurallib.logic.GeneralNoise;
import com.hypixel.hytale.procedurallib.random.CoordinateRotator;
import com.hypixel.hytale.server.worldgen.cave.CaveType;
import javax.annotation.Nonnull;

public abstract class AbstractDistortedBody extends AbstractDistortedShape {
   @Nonnull
   protected final Vector3d o;
   protected final Vector3d v;
   @Nonnull
   protected final CoordinateRotator rotation;

   public AbstractDistortedBody(@Nonnull Vector3d o, Vector3d v, double yaw, double pitch, double radiusX, double radiusY, double radiusZ) {
      this(o, v, new CoordinateRotator(pitch, yaw), radiusX, radiusY, radiusZ);
   }

   private AbstractDistortedBody(@Nonnull Vector3d o, Vector3d v, @Nonnull CoordinateRotator rotation, double radiusX, double radiusY, double radiusZ) {
      super(o, maxX(rotation, radiusX, radiusY, radiusZ), maxY(rotation, radiusX, radiusY, radiusZ), maxZ(rotation, radiusX, radiusY, radiusZ));
      this.o = o;
      this.v = v;
      this.rotation = rotation;
   }

   protected abstract double getHeight(int var1, double var2, double var4, double var6, double var8, CaveType var10, ShapeDistortion var11);

   @Nonnull
   @Override
   public Vector3d getStart() {
      return new Vector3d(this.o.x, this.getHighBoundY(), this.o.z);
   }

   @Nonnull
   @Override
   public Vector3d getEnd() {
      return new Vector3d(this.o.x, this.getLowBoundY(), this.o.z);
   }

   @Override
   public double getHeightAtProjection(int caveSeed, double x, double z, double t, double centerY, CaveType caveType, ShapeDistortion distortion) {
      double dx = x - this.o.x;
      double dz = z - this.o.z;
      x = this.o.x + this.rotation.rotateX(dx, 0.0, dz);
      z = this.o.z + this.rotation.rotateZ(dx, 0.0, dz);
      return this.getHeight(caveSeed, x, z, t, centerY, caveType, distortion);
   }

   @Override
   public double getFloor(double x, double z, double centerY, double height) {
      double dx = x - this.o.x;
      double dz = z - this.o.z;
      double dy = this.rotation.rotateY(dx, -height, dz);
      return centerY + dy;
   }

   @Override
   public double getCeiling(double x, double z, double centerY, double height) {
      double dx = x - this.o.x;
      double dz = z - this.o.z;
      double dy = this.rotation.rotateY(dx, height, dz);
      return centerY + dy;
   }

   private static double maxX(@Nonnull CoordinateRotator rotation, double radiusX, double radiusY, double radiusZ) {
      double x1 = Math.abs(rotation.rotateX(radiusX, radiusY, radiusZ));
      double x2 = Math.abs(rotation.rotateX(-radiusX, radiusY, radiusZ));
      return MathUtil.maxValue(x1, x2);
   }

   private static double maxY(@Nonnull CoordinateRotator rotation, double radiusX, double radiusY, double radiusZ) {
      double y1 = Math.abs(rotation.rotateY(radiusX, radiusY, radiusZ));
      double y2 = Math.abs(rotation.rotateY(radiusX, -radiusY, radiusZ));
      return Math.max(y1, y2);
   }

   private static double maxZ(@Nonnull CoordinateRotator rotation, double radiusX, double radiusY, double radiusZ) {
      double z1 = Math.abs(rotation.rotateZ(radiusX, radiusY, radiusZ));
      double z2 = Math.abs(rotation.rotateZ(radiusX, radiusY, -radiusZ));
      return MathUtil.maxValue(z1, z2);
   }

   protected abstract static class Factory implements DistortedShape.Factory {
      @Override
      public DistortedShape create(
         Vector3d origin,
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
         double scale = 1.0 / direction.length();
         double nx = direction.x * scale;
         double ny = direction.y * scale;
         double nz = direction.z * scale;
         double yaw = TrigMathUtil.atan2(nx, nz);
         double pitch = TrigMathUtil.asin(-ny);
         return this.createShape(origin, direction, yaw, pitch, startWidth, startHeight, length, interpolation);
      }

      protected abstract DistortedShape createShape(
         Vector3d var1, Vector3d var2, double var3, double var5, double var7, double var9, double var11, GeneralNoise.InterpolationFunction var13
      );
   }
}
