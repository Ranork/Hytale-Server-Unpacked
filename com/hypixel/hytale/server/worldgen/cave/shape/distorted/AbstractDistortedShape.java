package com.hypixel.hytale.server.worldgen.cave.shape.distorted;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public abstract class AbstractDistortedShape implements DistortedShape {
   private static final double PITCH_MIN = Math.toRadians(5.0);
   private static final double PITCH_MAX = Math.toRadians(175.0);
   private final int lowBoundX;
   private final int lowBoundY;
   private final int lowBoundZ;
   private final int highBoundX;
   private final int highBoundY;
   private final int highBoundZ;

   public AbstractDistortedShape(@Nonnull Vector3d o, double radiusX, double radiusY, double radiusZ) {
      this.lowBoundX = MathUtil.floor(o.x - radiusX);
      this.lowBoundY = MathUtil.floor(o.y - radiusY);
      this.lowBoundZ = MathUtil.floor(o.z - radiusZ);
      this.highBoundX = MathUtil.ceil(o.x + radiusX);
      this.highBoundY = MathUtil.ceil(o.y + radiusY);
      this.highBoundZ = MathUtil.ceil(o.z + radiusZ);
   }

   public AbstractDistortedShape(@Nonnull Vector3d o, @Nonnull Vector3d v, double width, double height) {
      this.lowBoundX = MathUtil.floor(Math.min(o.x, o.x + v.x) - width);
      this.lowBoundY = MathUtil.floor(Math.min(o.y, o.y + v.y) - height);
      this.lowBoundZ = MathUtil.floor(Math.min(o.z, o.z + v.z) - width);
      this.highBoundX = MathUtil.ceil(Math.max(o.x, o.x + v.x) + width);
      this.highBoundY = MathUtil.ceil(Math.max(o.y, o.y + v.y) + height);
      this.highBoundZ = MathUtil.ceil(Math.max(o.z, o.z + v.z) + width);
   }

   @Override
   public int getLowBoundX() {
      return this.lowBoundX;
   }

   @Override
   public int getLowBoundZ() {
      return this.lowBoundZ;
   }

   @Override
   public int getHighBoundX() {
      return this.highBoundX;
   }

   @Override
   public int getHighBoundZ() {
      return this.highBoundZ;
   }

   @Override
   public int getLowBoundY() {
      return this.lowBoundY;
   }

   @Override
   public int getHighBoundY() {
      return this.highBoundY;
   }

   public static double clampPitch(double pitch) {
      return MathUtil.clamp(pitch, PITCH_MIN, PITCH_MAX);
   }
}
