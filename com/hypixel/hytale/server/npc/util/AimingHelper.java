package com.hypixel.hytale.server.npc.util;

import com.hypixel.hytale.math.util.TrigMathUtil;
import javax.annotation.Nonnull;

public class AimingHelper {
   public static final int MIN_GRAVITY_FOR_PARABOLA = 3;

   public static double ensurePossibleThrowSpeed(double distance, double y, double gravity, double throwSpeed) {
      double x2 = distance * distance;
      if (x2 < 1.0000000000000002E-10) {
         double t = y * gravity;
         if (t <= 0.0) {
            return throwSpeed;
         } else {
            double minThrowSpeed = Math.sqrt(2.0 * t);
            return Math.max(minThrowSpeed, throwSpeed);
         }
      } else {
         double c = (Math.sqrt(y * y + x2) - y) / x2;
         double minThrowSpeed = Math.sqrt(gravity / c);
         return Math.max(minThrowSpeed, throwSpeed);
      }
   }

   public static boolean computePitch(double distance, double height, double velocity, double gravity, @Nonnull float[] resultingPitch) {
      if (distance * distance < 1.0000000000000002E-10) {
         double k = height * gravity;
         if (k <= 0.0) {
            resultingPitch[0] = height > 0.0 ? (float) (Math.PI / 2) : (float) (-Math.PI / 2);
            resultingPitch[1] = -resultingPitch[0];
            return true;
         } else {
            double peak = 0.5 * velocity * velocity / gravity;
            float pitch;
            if (height > 0.0) {
               if (peak < height - 1.0E-5) {
                  return false;
               }

               pitch = (float) (Math.PI / 2);
            } else {
               if (peak > height + 1.0E-5) {
                  return false;
               }

               pitch = (float) (-Math.PI / 2);
            }

            resultingPitch[0] = resultingPitch[1] = pitch;
            return true;
         }
      } else if (gravity < 3.0) {
         if (height == 0.0 && distance == 0.0) {
            return false;
         } else {
            resultingPitch[0] = resultingPitch[1] = TrigMathUtil.atan2(height, distance);
            return true;
         }
      } else if (resultingPitch.length != 2) {
         throw new IllegalArgumentException("computePitch requires a result array of size 2 for storing the resulting pitch");
      } else {
         double c = gravity / (velocity * velocity);
         double cx = c * distance - 1.0E-5;
         double cy = c * height - 1.0E-5;
         double k = 1.0 - cx * cx - 2.0 * cy;
         if (k < 0.0) {
            return false;
         } else {
            k = Math.sqrt(k);
            resultingPitch[0] = TrigMathUtil.atan((1.0 - k) / cx);
            resultingPitch[1] = TrigMathUtil.atan((1.0 + k) / cx);
            return true;
         }
      }
   }
}
