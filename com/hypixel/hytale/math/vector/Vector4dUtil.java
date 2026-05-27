package com.hypixel.hytale.math.vector;

import org.joml.Vector4d;

public final class Vector4dUtil {
   public static void perspectiveTransform(Vector4d vec) {
      double invW = 1.0 / vec.w;
      vec.x *= invW;
      vec.y *= invW;
      vec.z *= invW;
      vec.w = 1.0;
   }

   public static boolean isInsideFrustum(Vector4d vec) {
      return Math.abs(vec.x) <= Math.abs(vec.w) && Math.abs(vec.y) <= Math.abs(vec.w) && Math.abs(vec.z) <= Math.abs(vec.w);
   }

   private Vector4dUtil() {
   }
}
