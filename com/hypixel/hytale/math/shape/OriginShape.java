package com.hypixel.hytale.math.shape;

import com.hypixel.hytale.function.predicate.TriIntObjPredicate;
import com.hypixel.hytale.function.predicate.TriIntPredicate;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

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
      return this.shape.getBox(x + this.origin.x(), y + this.origin.y(), z + this.origin.z());
   }

   @Override
   public boolean containsPosition(double x, double y, double z) {
      return this.shape.containsPosition(x - this.origin.x(), y - this.origin.y(), z - this.origin.z());
   }

   @Override
   public void expand(double radius) {
      this.shape.expand(radius);
   }

   @Override
   public boolean forEachBlock(double x, double y, double z, double epsilon, TriIntPredicate consumer) {
      return this.shape.forEachBlock(x + this.origin.x(), y + this.origin.y(), z + this.origin.z(), epsilon, consumer);
   }

   @Override
   public <T> boolean forEachBlock(double x, double y, double z, double epsilon, T t, TriIntObjPredicate<T> consumer) {
      return this.shape.forEachBlock(x + this.origin.x(), y + this.origin.y(), z + this.origin.z(), epsilon, t, consumer);
   }

   @Nonnull
   @Override
   public String toString() {
      return "OriginShape{origin=" + this.origin + ", shape=" + this.shape + "}";
   }
}
