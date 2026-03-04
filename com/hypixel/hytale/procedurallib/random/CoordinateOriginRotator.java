package com.hypixel.hytale.procedurallib.random;

import javax.annotation.Nonnull;

public class CoordinateOriginRotator extends CoordinateRotator {
   private final double originX;
   private final double originY;
   private final double originZ;

   public CoordinateOriginRotator(double pitch, double yaw, double originX, double originY, double originZ) {
      super(pitch, yaw);
      this.originX = originX;
      this.originY = originY;
      this.originZ = originZ;
   }

   @Override
   public double randomDoubleX(int seed, double x, double y) {
      x -= this.originX;
      y -= this.originY;
      return this.originX + this.rotateX(x, y);
   }

   @Override
   public double randomDoubleY(int seed, double x, double y) {
      x -= this.originX;
      y -= this.originY;
      return this.originY + this.rotateY(x, y);
   }

   @Override
   public double randomDoubleX(int seed, double x, double y, double z) {
      x -= this.originX;
      y -= this.originY;
      z -= this.originZ;
      return this.originX + this.rotateX(x, y, z);
   }

   @Override
   public double randomDoubleY(int seed, double x, double y, double z) {
      x -= this.originX;
      y -= this.originY;
      z -= this.originZ;
      return this.originY + this.rotateY(x, y, z);
   }

   @Override
   public double randomDoubleZ(int seed, double x, double y, double z) {
      x -= this.originX;
      y -= this.originY;
      z -= this.originZ;
      return this.originZ + this.rotateZ(x, y, z);
   }

   @Nonnull
   @Override
   public String toString() {
      return "CoordinateOriginRotator{pitch="
         + this.pitch
         + ", yaw="
         + this.yaw
         + ", originX="
         + this.originX
         + ", originY="
         + this.originY
         + ", originZ="
         + this.originZ
         + "}";
   }
}
