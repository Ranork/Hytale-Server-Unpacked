package com.hypixel.hytale.math.shape;

import com.hypixel.hytale.math.vector.Vector2d;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Rectangle {
   private Vector2d min;
   private Vector2d max;

   public Rectangle() {
      this(new Vector2d(), new Vector2d());
   }

   public Rectangle(double minX, double minY, double maxX, double maxY) {
      this(new Vector2d(minX, minY), new Vector2d(maxX, maxY));
   }

   public Rectangle(Vector2d min, Vector2d max) {
      this.min = min;
      this.max = max;
   }

   public Rectangle(@Nonnull Rectangle other) {
      this(other.getMinX(), other.getMinY(), other.getMaxX(), other.getMaxY());
   }

   public Vector2d getMin() {
      return this.min;
   }

   public Vector2d getMax() {
      return this.max;
   }

   public double getMinX() {
      return this.min.x;
   }

   public double getMinY() {
      return this.min.y;
   }

   public double getMaxX() {
      return this.max.x;
   }

   public double getMaxY() {
      return this.max.y;
   }

   @Nonnull
   public Rectangle assign(double minX, double minY, double maxX, double maxY) {
      this.min.x = minX;
      this.min.y = minY;
      this.max.x = maxX;
      this.max.y = maxY;
      return this;
   }

   public boolean hasArea() {
      return this.min.x < this.max.x && this.min.y < this.max.y;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Rectangle that = (Rectangle)o;
         if (this.min != null ? this.min.equals(that.min) : that.min == null) {
            return this.max != null ? this.max.equals(that.max) : that.max == null;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.min != null ? this.min.hashCode() : 0;
      return 31 * result + (this.max != null ? this.max.hashCode() : 0);
   }

   @Nonnull
   @Override
   public String toString() {
      return "Rectangle2d{min=" + this.min + ", max=" + this.max + "}";
   }
}
