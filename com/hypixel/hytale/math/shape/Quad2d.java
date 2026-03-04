package com.hypixel.hytale.math.shape;

import com.hypixel.hytale.math.vector.Vector2d;
import java.util.Random;
import javax.annotation.Nonnull;

public class Quad2d {
   private Vector2d a;
   private Vector2d b;
   private Vector2d c;
   private Vector2d d;

   public Quad2d(Vector2d a, Vector2d b, Vector2d c, Vector2d d) {
      this.a = a;
      this.b = b;
      this.c = c;
      this.d = d;
   }

   public Quad2d() {
      this(new Vector2d(), new Vector2d(), new Vector2d(), new Vector2d());
   }

   public Quad2d(@Nonnull Vector2d[] points) {
      this(points, 0, 1, 2, 3);
   }

   public Quad2d(@Nonnull Vector2d[] points, int a, int b, int c, int d) {
      this(points[a], points[b], points[c], points[d]);
   }

   public Vector2d getA() {
      return this.a;
   }

   public Vector2d getB() {
      return this.b;
   }

   public Vector2d getC() {
      return this.c;
   }

   public Vector2d getD() {
      return this.d;
   }

   public double getMinX() {
      double min = this.a.x;
      if (min > this.b.x) {
         min = this.b.x;
      }

      if (min > this.c.x) {
         min = this.c.x;
      }

      if (min > this.d.x) {
         min = this.d.x;
      }

      return min;
   }

   public double getMinY() {
      double min = this.a.y;
      if (min > this.b.y) {
         min = this.b.y;
      }

      if (min > this.c.y) {
         min = this.c.y;
      }

      if (min > this.d.y) {
         min = this.d.y;
      }

      return min;
   }

   public double getMaxX() {
      double max = this.a.x;
      if (max < this.b.x) {
         max = this.b.x;
      }

      if (max < this.c.x) {
         max = this.c.x;
      }

      if (max < this.d.x) {
         max = this.d.x;
      }

      return max;
   }

   public double getMaxY() {
      double max = this.a.y;
      if (max < this.b.y) {
         max = this.b.y;
      }

      if (max < this.c.y) {
         max = this.c.y;
      }

      if (max < this.d.y) {
         max = this.d.y;
      }

      return max;
   }

   @Nonnull
   public Vector2d getCenter() {
      return this.getCenter(new Vector2d());
   }

   @Nonnull
   public Vector2d getCenter(@Nonnull Vector2d target) {
      return target.assign((this.a.x + this.c.x) * 0.5, (this.a.y + this.c.y) * 0.5);
   }

   @Nonnull
   public Vector2d getRandom(@Nonnull Random random) {
      return this.getRandom(random, new Vector2d());
   }

   @Nonnull
   public Vector2d getRandom(@Nonnull Random random, @Nonnull Vector2d vec) {
      double p = random.nextDouble();
      double q = random.nextDouble();
      if (p + q > 1.0) {
         p = 1.0 - p;
         q = 1.0 - q;
      }

      double pq = 1.0 - p - q;
      if (random.nextBoolean()) {
         vec.assign(-this.a.x * pq + this.b.x * p + this.c.x * q, -this.a.y * pq + this.b.y * p + this.c.y * q);
      } else {
         vec.assign(-this.a.x * pq + this.c.x * p + this.d.x * q, -this.a.y * pq + this.c.y * p + this.d.y * q);
      }

      return vec;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Quad2d{a=" + this.a + ", b=" + this.b + ", c=" + this.c + "}";
   }
}
