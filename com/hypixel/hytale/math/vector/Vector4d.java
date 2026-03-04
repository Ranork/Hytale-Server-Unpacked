package com.hypixel.hytale.math.vector;

import javax.annotation.Nonnull;

public class Vector4d {
   public static final int COMPONENT_X = 0;
   public static final int COMPONENT_Y = 1;
   public static final int COMPONENT_Z = 2;
   public static final int COMPONENT_W = 3;
   public double x;
   public double y;
   public double z;
   public double w;

   public Vector4d() {
      this(0.0, 0.0, 0.0, 0.0);
   }

   public Vector4d(double x, double y, double z, double w) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.w = w;
   }

   @Nonnull
   public static Vector4d newPosition(double x, double y, double z) {
      return new Vector4d(x, y, z, 1.0);
   }

   @Nonnull
   public static Vector4d newPosition(@Nonnull Vector3d v) {
      return new Vector4d(v.x, v.y, v.z, 1.0);
   }

   @Nonnull
   public static Vector4d newDirection(double x, double y, double z) {
      return new Vector4d(x, y, z, 0.0);
   }

   @Nonnull
   public Vector4d setDirection() {
      this.w = 0.0;
      return this;
   }

   @Nonnull
   public Vector4d setPosition() {
      this.w = 1.0;
      return this;
   }

   @Nonnull
   public Vector4d assign(@Nonnull Vector4d v) {
      return this.assign(v.x, v.y, v.z, v.w);
   }

   @Nonnull
   public Vector4d assign(double x, double y, double z, double w) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.w = w;
      return this;
   }

   @Nonnull
   public Vector4d lerp(@Nonnull Vector4d dest, double lerpFactor, @Nonnull Vector4d target) {
      target.assign(
         (dest.x - this.x) * lerpFactor + this.x,
         (dest.y - this.y) * lerpFactor + this.y,
         (dest.z - this.z) * lerpFactor + this.z,
         (dest.w - this.w) * lerpFactor + this.w
      );
      return target;
   }

   public void perspectiveTransform() {
      double invW = 1.0 / this.w;
      this.x *= invW;
      this.y *= invW;
      this.z *= invW;
      this.w = 1.0;
   }

   public boolean isInsideFrustum() {
      return Math.abs(this.x) <= Math.abs(this.w) && Math.abs(this.y) <= Math.abs(this.w) && Math.abs(this.z) <= Math.abs(this.w);
   }

   public double get(int component) {
      return switch (component) {
         case 0 -> this.x;
         case 1 -> this.y;
         case 2 -> this.z;
         case 3 -> this.w;
         default -> throw new IllegalArgumentException("Invalid component: " + component);
      };
   }

   @Nonnull
   @Override
   public String toString() {
      return "Vector4d{x=" + this.x + ", y=" + this.y + ", z=" + this.z + ", w=" + this.w + "}";
   }
}
