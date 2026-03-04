package com.hypixel.hytale.math.shape;

import com.hypixel.hytale.function.predicate.TriIntObjPredicate;
import com.hypixel.hytale.function.predicate.TriIntPredicate;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class OriginShape<S extends Shape> implements Shape {
   public final Vector3d origin;
   public S shape;

   public OriginShape() {
      this.origin = new Vector3d();
   }

   public OriginShape(Vector3d origin, S shape) {
      this.origin = origin;
      this.shape = shape;
   }

   public Vector3d getOrigin() {
      return this.origin;
   }

   public S getShape() {
      return this.shape;
   }

   @Override
   public Box getBox(double x, double y, double z) {
      return this.shape.getBox(x + this.origin.getX(), y + this.origin.getY(), z + this.origin.getZ());
   }

   @Override
   public boolean containsPosition(double x, double y, double z) {
      return this.shape.containsPosition(x - this.origin.getX(), y - this.origin.getY(), z - this.origin.getZ());
   }

   @Override
   public void expand(double radius) {
      this.shape.expand(radius);
   }

   @Override
   public boolean forEachBlock(double x, double y, double z, double epsilon, TriIntPredicate consumer) {
      return this.shape.forEachBlock(x + this.origin.getX(), y + this.origin.getY(), z + this.origin.getZ(), epsilon, consumer);
   }

   @Override
   public <T> boolean forEachBlock(double x, double y, double z, double epsilon, T t, TriIntObjPredicate<T> consumer) {
      return this.shape.forEachBlock(x + this.origin.getX(), y + this.origin.getY(), z + this.origin.getZ(), epsilon, t, consumer);
   }

   @Nonnull
   @Override
   public String toString() {
      return "OriginShape{origin=" + this.origin + ", shape=" + this.shape + "}";
   }
}
