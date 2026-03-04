package com.hypixel.hytale.math.shape;

import com.hypixel.hytale.function.predicate.TriIntObjPredicate;
import com.hypixel.hytale.function.predicate.TriIntPredicate;
import com.hypixel.hytale.math.block.BlockSphereUtil;
import com.hypixel.hytale.math.util.MathUtil;
import javax.annotation.Nonnull;

public class Ellipsoid implements Shape {
   public double radiusX;
   public double radiusY;
   public double radiusZ;

   public Ellipsoid() {
   }

   public Ellipsoid(double radius) {
      this(radius, radius, radius);
   }

   public Ellipsoid(double radiusX, double radiusY, double radiusZ) {
      this.radiusX = radiusX;
      this.radiusY = radiusY;
      this.radiusZ = radiusZ;
   }

   @Nonnull
   public Ellipsoid assign(double radius) {
      this.radiusX = radius;
      this.radiusY = radius;
      this.radiusZ = radius;
      return this;
   }

   @Nonnull
   @Override
   public Box getBox(double x, double y, double z) {
      Box boundingBox = new Box();
      boundingBox.min.assign(x - this.radiusX, y - this.radiusY, z - this.radiusZ);
      boundingBox.max.assign(x + this.radiusX, y + this.radiusY, z + this.radiusZ);
      return boundingBox;
   }

   @Override
   public boolean containsPosition(double x, double y, double z) {
      double xRatio = x / this.radiusX;
      double yRatio = y / this.radiusY;
      double zRatio = z / this.radiusZ;
      return xRatio * xRatio + yRatio * yRatio + zRatio * zRatio <= 1.0;
   }

   @Override
   public void expand(double radius) {
      this.radiusX += radius;
      this.radiusY += radius;
      this.radiusZ += radius;
   }

   @Override
   public boolean forEachBlock(double x, double y, double z, double epsilon, @Nonnull TriIntPredicate consumer) {
      return BlockSphereUtil.forEachBlock(
         MathUtil.floor(x),
         MathUtil.floor(y),
         MathUtil.floor(z),
         MathUtil.floor(this.radiusX + epsilon),
         MathUtil.floor(this.radiusY + epsilon),
         MathUtil.floor(this.radiusZ + epsilon),
         null,
         (_x, _y, _z, aVoid) -> consumer.test(_x, _y, _z)
      );
   }

   @Override
   public <T> boolean forEachBlock(double x, double y, double z, double epsilon, T t, @Nonnull TriIntObjPredicate<T> consumer) {
      return BlockSphereUtil.forEachBlock(
         MathUtil.floor(x),
         MathUtil.floor(y),
         MathUtil.floor(z),
         MathUtil.floor(this.radiusX + epsilon),
         MathUtil.floor(this.radiusY + epsilon),
         MathUtil.floor(this.radiusZ + epsilon),
         t,
         consumer
      );
   }

   @Nonnull
   @Override
   public String toString() {
      return "Ellipsoid{radiusX=" + this.radiusX + ", radiusY=" + this.radiusY + ", radiusZ=" + this.radiusZ + "}";
   }
}
