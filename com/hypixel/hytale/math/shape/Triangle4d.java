package com.hypixel.hytale.math.shape;

import com.hypixel.hytale.math.vector.Vector4dUtil;
import java.util.Random;
import javax.annotation.Nonnull;
import org.joml.Matrix4d;
import org.joml.Vector4d;

public class Triangle4d {
   private Vector4d a;
   private Vector4d b;
   private Vector4d c;

   public Triangle4d(Vector4d a, Vector4d b, Vector4d c) {
      this.a = a;
      this.b = b;
      this.c = c;
   }

   public Triangle4d() {
      this(new Vector4d().zero(), new Vector4d().zero(), new Vector4d().zero());
   }

   public Triangle4d(@Nonnull Vector4d[] points) {
      this(points, 0, 1, 2);
   }

   public Triangle4d(@Nonnull Vector4d[] points, int a, int b, int c) {
      this(points[a], points[b], points[c]);
   }

   public Vector4d getA() {
      return this.a;
   }

   public Vector4d getB() {
      return this.b;
   }

   public Vector4d getC() {
      return this.c;
   }

   public double getMin(int component) {
      double min = this.a.get(component);
      if (min > this.b.get(component)) {
         min = this.b.get(component);
      }

      if (min > this.c.get(component)) {
         min = this.c.get(component);
      }

      return min;
   }

   public double getMax(int component) {
      double max = this.a.get(component);
      if (max < this.b.get(component)) {
         max = this.b.get(component);
      }

      if (max < this.c.get(component)) {
         max = this.c.get(component);
      }

      return max;
   }

   @Nonnull
   public Triangle4d assign(@Nonnull Vector4d v1, @Nonnull Vector4d v2, @Nonnull Vector4d v3) {
      this.a.set(v1);
      this.b.set(v2);
      this.c.set(v3);
      return this;
   }

   @Nonnull
   public Vector4d getRandom(@Nonnull Random random) {
      return this.getRandom(random, new Vector4d().zero());
   }

   @Nonnull
   public Vector4d getRandom(@Nonnull Random random, @Nonnull Vector4d vec) {
      double p = random.nextDouble();
      double q = random.nextDouble() * (1.0 - p);
      double pq = 1.0 - p - q;
      vec.set(
         this.a.x * pq + this.b.x * p + this.c.x * q,
         this.a.y * pq + this.b.y * p + this.c.y * q,
         this.a.z * pq + this.b.z * p + this.c.z * q,
         this.a.w * pq + this.b.w * p + this.c.w * q
      );
      return vec;
   }

   @Nonnull
   public Triangle4d multiply(@Nonnull Matrix4d matrix) {
      return this.multiply(matrix, this);
   }

   @Nonnull
   public Triangle4d multiply(@Nonnull Matrix4d matrix, @Nonnull Triangle4d target) {
      matrix.transform(this.a, target.a);
      matrix.transform(this.b, target.b);
      matrix.transform(this.c, target.c);
      return target;
   }

   @Nonnull
   public Triangle2d to2d(@Nonnull Triangle2d target) {
      target.getA().set(this.a.x, this.a.y);
      target.getB().set(this.b.x, this.b.y);
      target.getC().set(this.c.x, this.c.y);
      return target;
   }

   @Nonnull
   public Triangle4d perspectiveTransform() {
      Vector4dUtil.perspectiveTransform(this.a);
      Vector4dUtil.perspectiveTransform(this.b);
      Vector4dUtil.perspectiveTransform(this.c);
      return this;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Triangle4d{a=" + this.a + ", b=" + this.b + ", c=" + this.c + "}";
   }
}
