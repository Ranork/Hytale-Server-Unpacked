package com.hypixel.hytale.math.shape;

import com.hypixel.hytale.math.vector.Vector2d;
import java.util.Random;
import javax.annotation.Nonnull;

public class Triangle2d {
   private Vector2d a;
   private Vector2d b;
   private Vector2d c;

   public Triangle2d(Vector2d a, Vector2d b, Vector2d c) {
      this.a = a;
      this.b = b;
      this.c = c;
   }

   public Triangle2d() {
      this(new Vector2d(), new Vector2d(), new Vector2d());
   }

   public Triangle2d(@Nonnull Vector2d[] points) {
      this(points, 0, 1, 2);
   }

   public Triangle2d(@Nonnull Vector2d[] points, int a, int b, int c) {
      this(points[a], points[b], points[c]);
   }

   public Vector2d getA() {
      return this.a;
   }

   public void setA(Vector2d a) {
      this.a = a;
   }

   public Vector2d getB() {
      return this.b;
   }

   public void setB(Vector2d b) {
      this.b = b;
   }

   public Vector2d getC() {
      return this.c;
   }

   public void setC(Vector2d c) {
      this.c = c;
   }

   public double getMinX() {
      double min = this.a.x;
      if (min > this.b.x) {
         min = this.b.x;
      }

      if (min > this.c.x) {
         min = this.c.x;
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

      return max;
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

      vec.assign(-this.a.x * (1.0 - p - q) + this.b.x * p + this.c.x * q, -this.a.y * (1.0 - p - q) + this.b.y * p + this.c.y * q);
      return vec;
   }
}
