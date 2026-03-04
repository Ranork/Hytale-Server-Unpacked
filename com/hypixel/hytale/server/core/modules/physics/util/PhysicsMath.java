package com.hypixel.hytale.server.core.modules.physics.util;

import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.TrigMathUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class PhysicsMath {
   public static final double DENSITY_AIR = 1.2;
   public static final double DENSITY_WATER = 998.0;
   public static final double AIR_DENSITY = 0.001225;
   public static final float HEADING_DIRECTION = 1.0F;

   public static double getAcceleration(double speed, double terminalSpeed) {
      double ratio = Math.abs(speed / terminalSpeed);
      return 32.0 * (1.0 - ratio * ratio * ratio);
   }

   public static double getTerminalVelocity(double mass, double density, double areaMillimetersSquared, double dragCoefficient) {
      double massGrams = mass * 1000.0;
      double areaMetersSquared = areaMillimetersSquared * 1000000.0;
      return Math.sqrt(64.0 * massGrams / (density * areaMetersSquared * dragCoefficient));
   }

   public static double getRelativeDensity(Vector3d position, Box boundingBox) {
      return 0.001225;
   }

   public static double computeDragCoefficient(double terminalSpeed, double area, double mass, double gravity) {
      return mass * gravity / (area * terminalSpeed * terminalSpeed);
   }

   public static double computeTerminalSpeed(double dragCoefficient, double area, double mass, double gravity) {
      return Math.sqrt(mass * gravity / (area * dragCoefficient));
   }

   public static double computeProjectedArea(double x, double y, double z, @Nonnull Box box) {
      double area = 0.0;
      if (x != 0.0) {
         area += Math.abs(x) * box.depth() * box.height();
      }

      if (y != 0.0) {
         area += Math.abs(y) * box.depth() * box.width();
      }

      if (z != 0.0) {
         area += Math.abs(z) * box.width() * box.height();
      }

      return area;
   }

   public static double computeProjectedArea(@Nonnull Vector3d direction, @Nonnull Box box) {
      return computeProjectedArea(direction.x, direction.y, direction.z, box);
   }

   public static double volumeOfIntersection(@Nonnull Box a, @Nonnull Vector3d posA, @Nonnull Box b, @Nonnull Vector3d posB) {
      return volumeOfIntersection(a, posA, b, posB.x, posB.y, posB.z);
   }

   public static double volumeOfIntersection(@Nonnull Box a, @Nonnull Vector3d posA, @Nonnull Box b, double posBX, double posBY, double posBZ) {
      posBX -= posA.x;
      posBY -= posA.y;
      posBZ -= posA.z;
      return lengthOfIntersection(a.min.x, a.max.x, posBX + b.min.x, posBX + b.max.x)
         * lengthOfIntersection(a.min.y, a.max.y, posBY + b.min.y, posBY + b.max.y)
         * lengthOfIntersection(a.min.z, a.max.z, posBZ + b.min.z, posBZ + b.max.z);
   }

   public static double lengthOfIntersection(double aMin, double aMax, double bMin, double bMax) {
      double left = Math.max(aMin, bMin);
      double right = Math.min(aMax, bMax);
      return Math.max(0.0, right - left);
   }

   public static float headingFromDirection(double x, double z) {
      return 1.0F * TrigMathUtil.atan2(-x, -z);
   }

   public static float normalizeAngle(float rad) {
      rad %= (float) (Math.PI * 2);
      if (rad < 0.0F) {
         rad += (float) (Math.PI * 2);
      }

      return rad;
   }

   public static float normalizeTurnAngle(float rad) {
      rad = normalizeAngle(rad);
      if (rad >= (float) Math.PI) {
         rad -= (float) (Math.PI * 2);
      }

      return rad;
   }

   public static float pitchFromDirection(double x, double y, double z) {
      return TrigMathUtil.atan2(y, Math.sqrt(x * x + z * z));
   }

   @Nonnull
   public static Vector3d vectorFromAngles(float heading, float pitch, @Nonnull Vector3d outDirection) {
      float sx = pitchX(pitch);
      outDirection.y = pitchY(pitch);
      outDirection.x = headingX(heading) * sx;
      outDirection.z = headingZ(heading) * sx;
      return outDirection;
   }

   public static float pitchX(float pitch) {
      return TrigMathUtil.cos(pitch);
   }

   public static float pitchY(float pitch) {
      return TrigMathUtil.sin(pitch);
   }

   public static float headingX(float heading) {
      return -TrigMathUtil.sin(1.0F * heading);
   }

   public static float headingZ(float heading) {
      return -TrigMathUtil.cos(1.0F * heading);
   }
}
