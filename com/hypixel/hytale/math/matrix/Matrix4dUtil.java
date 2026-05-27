package com.hypixel.hytale.math.matrix;

import org.joml.Matrix4d;

public final class Matrix4dUtil {
   public static float[] asFloatData(Matrix4d m) {
      return new float[]{
         (float)m.m00(),
         (float)m.m01(),
         (float)m.m02(),
         (float)m.m03(),
         (float)m.m10(),
         (float)m.m11(),
         (float)m.m12(),
         (float)m.m13(),
         (float)m.m20(),
         (float)m.m21(),
         (float)m.m22(),
         (float)m.m23(),
         (float)m.m30(),
         (float)m.m31(),
         (float)m.m32(),
         (float)m.m33()
      };
   }

   private Matrix4dUtil() {
   }
}
