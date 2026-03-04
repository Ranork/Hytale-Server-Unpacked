package com.hypixel.hytale.math.util;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import java.util.concurrent.ThreadLocalRandom;
import javax.annotation.Nonnull;

public class MathUtil {
   public static final double EPSILON_DOUBLE = Math.ulp(1.0);
   public static final float EPSILON_FLOAT = Math.ulp(1.0F);
   public static float PITCH_EDGE_PADDING = 0.01F;

   public static int abs(int i) {
      int mask = i >> 31;
      return i + mask ^ mask;
   }

   public static int floor(double d) {
      int i = (int)d;
      if (i <= d) {
         return i;
      } else {
         return d < -2.1474836E9F ? Integer.MIN_VALUE : i - 1;
      }
   }

   public static int ceil(double d) {
      int i = (int)d;
      if (!(d > 0.0) || d == i) {
         return i;
      } else {
         return d > 2.147483647E9 ? Integer.MAX_VALUE : i + 1;
      }
   }

   public static int randomInt(int min, int max) {
      return ThreadLocalRandom.current().nextInt(min, max);
   }

   public static double randomDouble(double min, double max) {
      return min + Math.random() * (max - min);
   }

   public static float randomFloat(float min, float max) {
      return min + (float)Math.random() * (max - min);
   }

   public static double round(double d, int p) {
      double pow = Math.pow(10.0, p);
      return Math.round(d * pow) / pow;
   }

   public static boolean within(double val, double min, double max) {
      return val >= min && val <= max;
   }

   public static double minValue(double v, double a, double c) {
      if (a < v) {
         v = a;
      }

      if (c < v) {
         v = c;
      }

      return v;
   }

   public static int minValue(int v, int a, int c) {
      if (a < v) {
         v = a;
      }

      if (c < v) {
         v = c;
      }

      return v;
   }

   public static double maxValue(double v, double a, double b, double c) {
      if (a > v) {
         v = a;
      }

      if (b > v) {
         v = b;
      }

      if (c > v) {
         v = c;
      }

      return v;
   }

   public static double maxValue(double v, double a, double b) {
      if (a > v) {
         v = a;
      }

      if (b > v) {
         v = b;
      }

      return v;
   }

   public static byte maxValue(byte v, byte a, byte b) {
      if (a > v) {
         v = a;
      }

      if (b > v) {
         v = b;
      }

      return v;
   }

   public static byte maxValue(byte v, byte a, byte b, byte c) {
      if (a > v) {
         v = a;
      }

      if (b > v) {
         v = b;
      }

      if (c > v) {
         v = c;
      }

      return v;
   }

   public static int maxValue(int v, int a, int b) {
      if (a > v) {
         v = a;
      }

      if (b > v) {
         v = b;
      }

      return v;
   }

   public static double lengthSquared(double x, double y) {
      return x * x + y * y;
   }

   public static double length(double x, double y) {
      return Math.sqrt(lengthSquared(x, y));
   }

   public static double lengthSquared(double x, double y, double z) {
      return x * x + y * y + z * z;
   }

   public static double length(double x, double y, double z) {
      return Math.sqrt(lengthSquared(x, y, z));
   }

   public static double maxValue(double v, double a) {
      return a > v ? a : v;
   }

   public static double clipToZero(double v) {
      return clipToZero(v, EPSILON_DOUBLE);
   }

   public static double clipToZero(double v, double epsilon) {
      return v >= -epsilon && v <= epsilon ? 0.0 : v;
   }

   public static float clipToZero(float v) {
      return clipToZero(v, EPSILON_FLOAT);
   }

   public static float clipToZero(float v, float epsilon) {
      return v >= -epsilon && v <= epsilon ? 0.0F : v;
   }

   public static boolean closeToZero(double v) {
      return closeToZero(v, EPSILON_DOUBLE);
   }

   public static boolean closeToZero(double v, double epsilon) {
      return v >= -epsilon && v <= epsilon;
   }

   public static boolean closeToZero(float v) {
      return closeToZero(v, EPSILON_FLOAT);
   }

   public static boolean closeToZero(float v, float epsilon) {
      return v >= -epsilon && v <= epsilon;
   }

   public static double clamp(double v, double min, double max) {
      if (v > max) {
         return v < min ? min : max;
      } else {
         return v < min ? min : v;
      }
   }

   public static float clamp(float v, float min, float max) {
      if (v > max) {
         return v < min ? min : max;
      } else {
         return v < min ? min : v;
      }
   }

   public static int clamp(int v, int min, int max) {
      if (v > max) {
         return v < min ? min : max;
      } else {
         return v < min ? min : v;
      }
   }

   public static long clamp(long v, long min, long max) {
      if (v > max) {
         return v < min ? min : max;
      } else {
         return v < min ? min : v;
      }
   }

   public static int getPercentageOf(int index, int max) {
      return (int)(index / (max - 1.0) * 100.0);
   }

   public static double percent(int v, int total) {
      return total == 0 ? 0.0 : v * 100.0 / total;
   }

   public static int fastRound(float f) {
      return fastFloor(f + 0.5F);
   }

   public static long fastRound(double d) {
      return fastFloor(d + 0.5);
   }

   public static int fastFloor(float f) {
      int i = (int)f;
      if (i <= f) {
         return i;
      } else {
         return f < -2.1474836E9F ? Integer.MIN_VALUE : i - 1;
      }
   }

   public static long fastFloor(double d) {
      long i = (long)d;
      if (i <= d) {
         return i;
      } else {
         return d < -9.223372E18F ? Long.MIN_VALUE : i - 1L;
      }
   }

   public static int fastCeil(float f) {
      int i = (int)f;
      if (!(f > 0.0F) || f == i) {
         return i;
      } else {
         return f > 2.1474836E9F ? Integer.MAX_VALUE : i + 1;
      }
   }

   public static long fastCeil(double d) {
      long i = (long)d;
      if (!(d > 0.0) || d == i) {
         return i;
      } else {
         return d > 9.223372E18F ? Long.MAX_VALUE : i + 1L;
      }
   }

   private MathUtil() {
   }

   public static float halfFloatToFloat(int hbits) {
      int mant = hbits & 1023;
      int exp = hbits & 31744;
      if (exp == 31744) {
         exp = 261120;
      } else if (exp != 0) {
         exp += 114688;
         if (mant == 0 && exp > 115712) {
            return Float.intBitsToFloat((hbits & 32768) << 16 | exp << 13 | 1023);
         }
      } else if (mant != 0) {
         exp = 115712;

         do {
            mant <<= 1;
            exp -= 1024;
         } while ((mant & 1024) == 0);

         mant &= 1023;
      }

      return Float.intBitsToFloat((hbits & 32768) << 16 | (exp | mant) << 13);
   }

   public static int halfFloatFromFloat(float fval) {
      int fbits = Float.floatToIntBits(fval);
      int sign = fbits >>> 16 & 32768;
      int val = (fbits & 2147483647) + 4096;
      if (val >= 1199570944) {
         if ((fbits & 2147483647) >= 1199570944) {
            return val < 2139095040 ? sign | 31744 : sign | 31744 | (fbits & 8388607) >>> 13;
         } else {
            return sign | 31743;
         }
      } else if (val >= 947912704) {
         return sign | val - 939524096 >>> 13;
      } else if (val < 855638016) {
         return sign;
      } else {
         val = (fbits & 2147483647) >>> 23;
         return sign | (fbits & 8388607 | 8388608) + (8388608 >>> val - 102) >>> 126 - val;
      }
   }

   public static int byteCount(int i) {
      if (i > 65535) {
         return 4;
      } else if (i > 255) {
         return 2;
      } else {
         return i > 0 ? 1 : 0;
      }
   }

   public static int packInt(int x, int z) {
      return x << 16 | z & 65535;
   }

   public static int unpackLeft(int packed) {
      int i = packed >> 16 & 65535;
      if ((i & 32768) != 0) {
         i |= -65536;
      }

      return i;
   }

   public static int unpackRight(int packed) {
      int i = packed & 65535;
      if ((i & 32768) != 0) {
         i |= -65536;
      }

      return i;
   }

   public static long packLong(int left, int right) {
      return (long)left << 32 | right & 4294967295L;
   }

   public static int unpackLeft(long packed) {
      return (int)(packed >> 32);
   }

   public static int unpackRight(long packed) {
      return (int)packed;
   }

   @Nonnull
   public static Vector3i rotateVectorYAxis(@Nonnull Vector3i vector, int angle, boolean clockwise) {
      float radAngle = (float) (Math.PI / 180.0) * angle;
      int x1;
      int z1;
      if (clockwise) {
         x1 = (int)(vector.x * TrigMathUtil.cos(radAngle) - vector.z * TrigMathUtil.sin(radAngle));
         z1 = (int)(vector.x * TrigMathUtil.sin(radAngle) + vector.z * TrigMathUtil.cos(radAngle));
      } else {
         x1 = (int)(vector.x * TrigMathUtil.cos(radAngle) + vector.z * TrigMathUtil.sin(radAngle));
         z1 = (int)(-vector.x * TrigMathUtil.sin(radAngle) + vector.z * TrigMathUtil.cos(radAngle));
      }

      return new Vector3i(x1, vector.y, z1);
   }

   @Nonnull
   public static Vector3d rotateVectorYAxis(@Nonnull Vector3d vector, int angle, boolean clockwise) {
      float radAngle = (float) (Math.PI / 180.0) * angle;
      double x1;
      double z1;
      if (clockwise) {
         x1 = vector.x * TrigMathUtil.cos(radAngle) - vector.z * TrigMathUtil.sin(radAngle);
         z1 = vector.x * TrigMathUtil.sin(radAngle) + vector.z * TrigMathUtil.cos(radAngle);
      } else {
         x1 = vector.x * TrigMathUtil.cos(radAngle) + vector.z * TrigMathUtil.sin(radAngle);
         z1 = -vector.x * TrigMathUtil.sin(radAngle) + vector.z * TrigMathUtil.cos(radAngle);
      }

      return new Vector3d(x1, vector.y, z1);
   }

   public static float wrapAngle(float angle) {
      angle %= (float) (Math.PI * 2);
      if (angle <= (float) -Math.PI) {
         angle += (float) (Math.PI * 2);
      } else if (angle > (float) Math.PI) {
         angle -= (float) (Math.PI * 2);
      }

      return angle;
   }

   public static float lerp(float a, float b, float t) {
      return lerpUnclamped(a, b, clamp(t, 0.0F, 1.0F));
   }

   public static float lerpUnclamped(float a, float b, float t) {
      return a + t * (b - a);
   }

   public static double lerp(double a, double b, double t) {
      return lerpUnclamped(a, b, clamp(t, 0.0, 1.0));
   }

   public static double lerpUnclamped(double a, double b, double t) {
      return a + t * (b - a);
   }

   public static float shortAngleDistance(float a, float b) {
      float distance = (b - a) % (float) (Math.PI * 2);
      return 2.0F * distance % (float) (Math.PI * 2) - distance;
   }

   public static float lerpAngle(float a, float b, float t) {
      return a + shortAngleDistance(a, b) * t;
   }

   public static double floorMod(double x, double y) {
      return x - Math.floor(x / y) * y;
   }

   public static double compareAngle(double a, double b) {
      double diff = b - a;
      return floorMod(diff + Math.PI, Math.PI * 2) - Math.PI;
   }

   public static double percentile(@Nonnull long[] sortedData, double percentile) {
      if (sortedData.length == 1) {
         return sortedData[0];
      } else if (percentile >= 1.0) {
         return sortedData[sortedData.length - 1];
      } else {
         double position = (sortedData.length + 1) * percentile;
         double n = percentile * (sortedData.length - 1) + 1.0;
         long left;
         long right;
         if (position >= 1.0) {
            left = sortedData[floor(n) - 1];
            right = sortedData[floor(n)];
         } else {
            left = sortedData[0];
            right = sortedData[1];
         }

         if (left == right) {
            return left;
         } else {
            double part = n - floor(n);
            return left + part * (right - left);
         }
      }
   }

   public static double distanceToLineSq(double x, double y, double ax, double ay, double bx, double by) {
      double dx0 = x - ax;
      double dy0 = y - ay;
      double dx1 = bx - ax;
      double dy1 = by - ay;
      return distanceToLineSq(x, y, ax, ay, bx, by, dx0, dy0, dx1, dy1);
   }

   public static double distanceToLineSq(double x, double y, double ax, double ay, double bx, double by, double dxAx, double dyAy, double dBxAx, double dByAy) {
      double t = dxAx * dBxAx + dyAy * dByAy;
      t /= dBxAx * dBxAx + dByAy * dByAy;
      double px = ax;
      double py = ay;
      if (t > 1.0) {
         px = bx;
         py = by;
      } else if (t > 0.0) {
         px = ax + t * dBxAx;
         py = ay + t * dByAy;
      }

      dBxAx = x - px;
      dByAy = y - py;
      return dBxAx * dBxAx + dByAy * dByAy;
   }

   public static double distanceToInfLineSq(double x, double y, double ax, double ay, double bx, double by) {
      double dx0 = x - ax;
      double dy0 = y - ay;
      double dx1 = bx - ax;
      double dy1 = by - ay;
      return distanceToInfLineSq(x, y, ax, ay, dx0, dy0, dx1, dy1);
   }

   public static double distanceToInfLineSq(double x, double y, double ax, double ay, double dxAx, double dyAy, double dBxAx, double dByAy) {
      double t = dxAx * dBxAx + dyAy * dByAy;
      t /= dBxAx * dBxAx + dByAy * dByAy;
      double px = ax + t * dBxAx;
      double py = ay + t * dByAy;
      dBxAx = x - px;
      dByAy = y - py;
      return dBxAx * dBxAx + dByAy * dByAy;
   }

   public static int sideOfLine(double x, double y, double ax, double ay, double bx, double by) {
      return (ax - x) * (by - y) - (ay - y) * (bx - x) >= 0.0 ? 1 : -1;
   }

   public static Vector3f getRotationForHitNormal(Vector3f normal) {
      if (normal == null) {
         return Vector3f.ZERO;
      } else if (normal.y == 1.0F) {
         return Vector3f.ZERO;
      } else if (normal.y == -1.0F) {
         return new Vector3f(0.0F, 0.0F, (float) Math.PI);
      } else if (normal.x == 1.0F) {
         return new Vector3f(0.0F, 0.0F, (float) (-Math.PI / 2));
      } else if (normal.x == -1.0F) {
         return new Vector3f(0.0F, 0.0F, (float) (Math.PI / 2));
      } else if (normal.z == 1.0F) {
         return new Vector3f((float) (Math.PI / 2), 0.0F, 0.0F);
      } else {
         return normal.z == -1.0F ? new Vector3f((float) (-Math.PI / 2), 0.0F, 0.0F) : Vector3f.ZERO;
      }
   }

   public static String getNameForHitNormal(Vector3f normal) {
      if (normal == null) {
         return "UP";
      } else if (normal.y == 1.0F) {
         return "UP";
      } else if (normal.y == -1.0F) {
         return "DOWN";
      } else if (normal.x == 1.0F) {
         return "WEST";
      } else if (normal.x == -1.0F) {
         return "EAST";
      } else if (normal.z == 1.0F) {
         return "NORTH";
      } else {
         return normal.z == -1.0F ? "SOUTH" : "UP";
      }
   }

   public static float mapToRange(float value, float valueMin, float valueMax, float rangeMin, float rangeMax) {
      float alpha = (value - valueMin) / (valueMax - valueMin);
      return rangeMin + alpha * (rangeMax - rangeMin);
   }
}
