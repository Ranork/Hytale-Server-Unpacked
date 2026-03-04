package com.hypixel.hytale.math.shape;

import com.hypixel.hytale.function.predicate.TriIntObjPredicate;
import com.hypixel.hytale.function.predicate.TriIntPredicate;
import com.hypixel.hytale.math.block.BlockCylinderUtil;
import com.hypixel.hytale.math.util.MathUtil;
import javax.annotation.Nonnull;

public class Cylinder implements Shape {
   public double height;
   public double radiusX;
   public double radiusZ;

   public Cylinder() {
   }

   public Cylinder(double height, double radiusX, double radiusZ) {
      this.height = height;
      this.radiusX = radiusX;
      this.radiusZ = radiusZ;
   }

   public double getRadiusX() {
      return this.radiusX;
   }

   public double getRadiusZ() {
      return this.radiusZ;
   }

   public double getHeight() {
      return this.height;
   }

   @Nonnull
   public Cylinder assign(double radius) {
      this.radiusX = radius;
      this.radiusZ = radius;
      return this;
   }

   @Override
   public boolean containsPosition(double x, double y, double z) {
      if (!(y > this.height) && !(y < 0.0)) {
         double result = x * x / (this.radiusX * this.radiusX) + z * z / (this.radiusZ * this.radiusZ);
         return result <= 1.0;
      } else {
         return false;
      }
   }

   @Override
   public boolean forEachBlock(double x, double y, double z, double epsilon, @Nonnull TriIntPredicate consumer) {
      return BlockCylinderUtil.forEachBlock(
         MathUtil.floor(x),
         MathUtil.floor(y),
         MathUtil.floor(z),
         MathUtil.floor(this.radiusX + epsilon),
         MathUtil.floor(this.height + epsilon),
         MathUtil.floor(this.radiusZ + epsilon),
         null,
         (_x, _y, _z, aVoid) -> consumer.test(_x, _y, _z)
      );
   }

   @Override
   public <T> boolean forEachBlock(double x, double y, double z, double epsilon, T t, @Nonnull TriIntObjPredicate<T> consumer) {
      return BlockCylinderUtil.forEachBlock(
         MathUtil.floor(x),
         MathUtil.floor(y),
         MathUtil.floor(z),
         MathUtil.floor(this.radiusX + epsilon),
         MathUtil.floor(this.height + epsilon),
         MathUtil.floor(this.radiusZ + epsilon),
         t,
         consumer
      );
   }

   @Override
   public void expand(double radius) {
      this.radiusX += radius;
      this.radiusZ += radius;
   }

   @Nonnull
   @Override
   public Box getBox(double x, double y, double z) {
      double biggestRadius = Math.max(this.radiusX, this.radiusZ);
      Box boundingBox = new Box();
      boundingBox.min.assign(x - biggestRadius, y, z - biggestRadius);
      boundingBox.max.assign(x + biggestRadius, y + this.height, z + biggestRadius);
      return boundingBox;
   }

   @Nonnull
   protected Cylinder clone() {
      return new Cylinder(this.height, this.radiusX, this.radiusZ);
   }

   @Nonnull
   @Override
   public String toString() {
      return "Cylinder{height=" + this.height + ", radiusX=" + this.radiusX + ", radiusZ=" + this.radiusZ + "}";
   }
}
