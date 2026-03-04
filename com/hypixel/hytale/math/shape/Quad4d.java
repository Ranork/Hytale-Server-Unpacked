package com.hypixel.hytale.math.shape;

import com.hypixel.hytale.math.matrix.Matrix4d;
import com.hypixel.hytale.math.vector.Vector4d;
import java.util.Random;
import javax.annotation.Nonnull;

public class Quad4d {
   private Vector4d a;
   private Vector4d b;
   private Vector4d c;
   private Vector4d d;

   public Quad4d(Vector4d a, Vector4d b, Vector4d c, Vector4d d) {
      this.a = a;
      this.b = b;
      this.c = c;
      this.d = d;
   }

   public Quad4d() {
      this(new Vector4d(), new Vector4d(), new Vector4d(), new Vector4d());
   }

   public Quad4d(@Nonnull Vector4d[] points) {
      this(points, 0, 1, 2, 3);
   }

   public Quad4d(@Nonnull Vector4d[] points, int a, int b, int c, int d) {
      this(points[a], points[b], points[c], points[d]);
   }

   public boolean isFullyInsideFrustum() {
      return this.a.isInsideFrustum() && this.b.isInsideFrustum() && this.c.isInsideFrustum() && this.d.isInsideFrustum();
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

   public Vector4d getD() {
      return this.d;
   }

   public Vector4d get(int idx) {
      return switch (idx) {
         case 0 -> this.a;
         case 1 -> this.b;
         case 2 -> this.c;
         case 3 -> this.d;
         default -> throw new IllegalArgumentException("Index must be in range of 0 to 3. Given: " + idx);
      };
   }

   public double getMin(int component) {
      double min = this.a.get(component);
      if (min > this.b.get(component)) {
         min = this.b.get(component);
      }

      if (min > this.c.get(component)) {
         min = this.c.get(component);
      }

      if (min > this.d.get(component)) {
         min = this.d.get(component);
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

      if (max < this.d.get(component)) {
         max = this.d.get(component);
      }

      return max;
   }

   @Nonnull
   public Quad4d multiply(@Nonnull Matrix4d matrix) {
      return this.multiply(matrix, this);
   }

   @Nonnull
   public Quad4d multiply(@Nonnull Matrix4d matrix, @Nonnull Quad4d target) {
      matrix.multiply(this.a, target.a);
      matrix.multiply(this.b, target.b);
      matrix.multiply(this.c, target.c);
      matrix.multiply(this.d, target.d);
      return target;
   }

   @Nonnull
   public Quad2d to2d(@Nonnull Quad2d target) {
      target.getA().assign(this.a.x, this.a.y);
      target.getB().assign(this.b.x, this.b.y);
      target.getC().assign(this.c.x, this.c.y);
      target.getD().assign(this.d.x, this.d.y);
      return target;
   }

   @Nonnull
   public Vector4d getCenter() {
      return this.getCenter(new Vector4d());
   }

   @Nonnull
   public Vector4d getCenter(@Nonnull Vector4d target) {
      return target.assign(
         this.a.x + (this.c.x - this.a.x) * 0.5,
         this.b.x + (this.b.x - this.b.x) * 0.5,
         this.c.x + (this.c.x - this.c.x) * 0.5,
         this.d.x + (this.d.x - this.d.x) * 0.5
      );
   }

   public void perspectiveTransform() {
      this.a.perspectiveTransform();
      this.b.perspectiveTransform();
      this.c.perspectiveTransform();
      this.d.perspectiveTransform();
   }

   @Nonnull
   public Vector4d getRandom(@Nonnull Random random) {
      return this.getRandom(random, new Vector4d());
   }

   @Nonnull
   public Vector4d getRandom(@Nonnull Random random, @Nonnull Vector4d target) {
      double p = random.nextDouble();
      double q = random.nextDouble() * (1.0 - p);
      double pq = 1.0 - p - q;
      if (random.nextBoolean()) {
         target.assign(
            this.a.x * pq + this.b.x * p + this.c.x * q,
            this.a.y * pq + this.b.y * p + this.c.y * q,
            this.a.z * pq + this.b.z * p + this.c.z * q,
            this.a.w * pq + this.b.w * p + this.c.w * q
         );
      } else {
         target.assign(
            this.a.x * pq + this.c.x * p + this.d.x * q,
            this.a.y * pq + this.c.y * p + this.d.y * q,
            this.a.z * pq + this.c.z * p + this.d.z * q,
            this.a.w * pq + this.c.w * p + this.d.w * q
         );
      }

      return target;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Quad4d{\na=" + this.a + ",\nb=" + this.b + ",\nc=" + this.c + ",\nd=" + this.d + "\n}";
   }
}
