package com.hypixel.hytale.math.iterator;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public final class BlockIterator {
   private BlockIterator() {
      throw new UnsupportedOperationException("This is a utilitiy class. Do not instantiate.");
   }

   public static boolean iterateFromTo(@Nonnull Vector3d origin, @Nonnull Vector3d target, @Nonnull BlockIterator.BlockIteratorProcedure procedure) {
      return iterateFromTo(origin.x, origin.y, origin.z, target.x, target.y, target.z, procedure);
   }

   public static boolean iterateFromTo(@Nonnull Vector3i origin, @Nonnull Vector3i target, @Nonnull BlockIterator.BlockIteratorProcedure procedure) {
      return iterateFromTo(origin.x, origin.y, origin.z, target.x, target.y, target.z, procedure);
   }

   public static boolean iterateFromTo(
      double sx, double sy, double sz, double tx, double ty, double tz, @Nonnull BlockIterator.BlockIteratorProcedure procedure
   ) {
      double dx = tx - sx;
      double dy = ty - sy;
      double dz = tz - sz;
      double maxDistance = Math.sqrt(dx * dx + dy * dy + dz * dz);
      return iterate(sx, sy, sz, dx, dy, dz, maxDistance, procedure);
   }

   public static <T> boolean iterateFromTo(
      double sx, double sy, double sz, double tx, double ty, double tz, @Nonnull BlockIterator.BlockIteratorProcedurePlus1<T> procedure, T t
   ) {
      double dx = tx - sx;
      double dy = ty - sy;
      double dz = tz - sz;
      double maxDistance = Math.sqrt(dx * dx + dy * dy + dz * dz);
      return iterate(sx, sy, sz, dx, dy, dz, maxDistance, procedure, t);
   }

   public static boolean iterate(
      @Nonnull Vector3d origin, @Nonnull Vector3d direction, double maxDistance, @Nonnull BlockIterator.BlockIteratorProcedure procedure
   ) {
      return iterate(origin.x, origin.y, origin.z, direction.x, direction.y, direction.z, maxDistance, procedure);
   }

   public static boolean iterate(
      double sx, double sy, double sz, double dx, double dy, double dz, double maxDistance, @Nonnull BlockIterator.BlockIteratorProcedure procedure
   ) {
      checkParameters(sx, sy, sz, dx, dy, dz);
      return iterate0(sx, sy, sz, dx, dy, dz, maxDistance, procedure);
   }

   private static boolean iterate0(
      double sx, double sy, double sz, double dx, double dy, double dz, double maxDistance, @Nonnull BlockIterator.BlockIteratorProcedure procedure
   ) {
      maxDistance /= Math.sqrt(dx * dx + dy * dy + dz * dz);
      int bx = (int)BlockIterator.FastMath.fastFloor(sx);
      int by = (int)BlockIterator.FastMath.fastFloor(sy);
      int bz = (int)BlockIterator.FastMath.fastFloor(sz);
      double px = sx - bx;
      double py = sy - by;
      double pz = sz - bz;
      double pt = 0.0;

      while (pt <= maxDistance) {
         double t = intersection(px, py, pz, dx, dy, dz);
         double qx = px + t * dx;
         double qy = py + t * dy;
         double qz = pz + t * dz;
         if (!procedure.accept(bx, by, bz, px, py, pz, qx, qy, qz)) {
            return false;
         }

         if (dx < 0.0 && BlockIterator.FastMath.sEq(qx, 0.0)) {
            qx++;
            bx--;
         } else if (dx > 0.0 && BlockIterator.FastMath.gEq(qx, 1.0)) {
            qx--;
            bx++;
         }

         if (dy < 0.0 && BlockIterator.FastMath.sEq(qy, 0.0)) {
            qy++;
            by--;
         } else if (dy > 0.0 && BlockIterator.FastMath.gEq(qy, 1.0)) {
            qy--;
            by++;
         }

         if (dz < 0.0 && BlockIterator.FastMath.sEq(qz, 0.0)) {
            qz++;
            bz--;
         } else if (dz > 0.0 && BlockIterator.FastMath.gEq(qz, 1.0)) {
            qz--;
            bz++;
         }

         pt += t;
         px = qx;
         py = qy;
         pz = qz;
      }

      return true;
   }

   public static <T> boolean iterate(
      double sx,
      double sy,
      double sz,
      double dx,
      double dy,
      double dz,
      double maxDistance,
      @Nonnull BlockIterator.BlockIteratorProcedurePlus1<T> procedure,
      T obj1
   ) {
      checkParameters(sx, sy, sz, dx, dy, dz);
      return iterate0(sx, sy, sz, dx, dy, dz, maxDistance, procedure, obj1);
   }

   private static <T> boolean iterate0(
      double sx,
      double sy,
      double sz,
      double dx,
      double dy,
      double dz,
      double maxDistance,
      @Nonnull BlockIterator.BlockIteratorProcedurePlus1<T> procedure,
      T obj1
   ) {
      maxDistance /= Math.sqrt(dx * dx + dy * dy + dz * dz);
      int bx = (int)BlockIterator.FastMath.fastFloor(sx);
      int by = (int)BlockIterator.FastMath.fastFloor(sy);
      int bz = (int)BlockIterator.FastMath.fastFloor(sz);
      double px = sx - bx;
      double py = sy - by;
      double pz = sz - bz;
      double pt = 0.0;

      while (pt <= maxDistance) {
         double t = intersection(px, py, pz, dx, dy, dz);
         double qx = px + t * dx;
         double qy = py + t * dy;
         double qz = pz + t * dz;
         if (!procedure.accept(bx, by, bz, px, py, pz, qx, qy, qz, obj1)) {
            return false;
         }

         if (dx < 0.0 && BlockIterator.FastMath.sEq(qx, 0.0)) {
            qx++;
            bx--;
         } else if (dx > 0.0 && BlockIterator.FastMath.gEq(qx, 1.0)) {
            qx--;
            bx++;
         }

         if (dy < 0.0 && BlockIterator.FastMath.sEq(qy, 0.0)) {
            qy++;
            by--;
         } else if (dy > 0.0 && BlockIterator.FastMath.gEq(qy, 1.0)) {
            qy--;
            by++;
         }

         if (dz < 0.0 && BlockIterator.FastMath.sEq(qz, 0.0)) {
            qz++;
            bz--;
         } else if (dz > 0.0 && BlockIterator.FastMath.gEq(qz, 1.0)) {
            qz--;
            bz++;
         }

         pt += t;
         px = qx;
         py = qy;
         pz = qz;
      }

      return true;
   }

   private static void checkParameters(double sx, double sy, double sz, double dx, double dy, double dz) {
      if (isNonValidNumber(sx)) {
         throw new IllegalArgumentException("sx is a non-valid number! Given: " + sx);
      } else if (isNonValidNumber(sy)) {
         throw new IllegalArgumentException("sy is a non-valid number! Given: " + sy);
      } else if (isNonValidNumber(sz)) {
         throw new IllegalArgumentException("sz is a non-valid number! Given: " + sz);
      } else if (isNonValidNumber(dx)) {
         throw new IllegalArgumentException("dx is a non-valid number! Given: " + dx);
      } else if (isNonValidNumber(dy)) {
         throw new IllegalArgumentException("dy is a non-valid number! Given: " + dy);
      } else if (isNonValidNumber(dz)) {
         throw new IllegalArgumentException("dz is a non-valid number! Given: " + dz);
      } else if (isZeroDirection(dx, dy, dz)) {
         throw new IllegalArgumentException("Direction is ZERO! Given: (" + dx + ", " + dy + ", " + dz + ")");
      }
   }

   public static boolean isNonValidNumber(double d) {
      return Double.isNaN(d) || Double.isInfinite(d);
   }

   public static boolean isZeroDirection(double dx, double dy, double dz) {
      return BlockIterator.FastMath.eq(dx, 0.0) && BlockIterator.FastMath.eq(dy, 0.0) && BlockIterator.FastMath.eq(dz, 0.0);
   }

   private static double intersection(double px, double py, double pz, double dx, double dy, double dz) {
      double tFar = 0.0;
      if (dx < 0.0) {
         double t = -px / dx;
         double u = pz + dz * t;
         double v = py + dy * t;
         if (t > tFar
            && BlockIterator.FastMath.gEq(u, 0.0)
            && BlockIterator.FastMath.sEq(u, 1.0)
            && BlockIterator.FastMath.gEq(v, 0.0)
            && BlockIterator.FastMath.sEq(v, 1.0)) {
            tFar = t;
         }
      } else if (dx > 0.0) {
         double t = (1.0 - px) / dx;
         double u = pz + dz * t;
         double v = py + dy * t;
         if (t > tFar
            && BlockIterator.FastMath.gEq(u, 0.0)
            && BlockIterator.FastMath.sEq(u, 1.0)
            && BlockIterator.FastMath.gEq(v, 0.0)
            && BlockIterator.FastMath.sEq(v, 1.0)) {
            tFar = t;
         }
      }

      if (dy < 0.0) {
         double t = -py / dy;
         double u = px + dx * t;
         double v = pz + dz * t;
         if (t > tFar
            && BlockIterator.FastMath.gEq(u, 0.0)
            && BlockIterator.FastMath.sEq(u, 1.0)
            && BlockIterator.FastMath.gEq(v, 0.0)
            && BlockIterator.FastMath.sEq(v, 1.0)) {
            tFar = t;
         }
      } else if (dy > 0.0) {
         double t = (1.0 - py) / dy;
         double u = px + dx * t;
         double v = pz + dz * t;
         if (t > tFar
            && BlockIterator.FastMath.gEq(u, 0.0)
            && BlockIterator.FastMath.sEq(u, 1.0)
            && BlockIterator.FastMath.gEq(v, 0.0)
            && BlockIterator.FastMath.sEq(v, 1.0)) {
            tFar = t;
         }
      }

      if (dz < 0.0) {
         double t = -pz / dz;
         double u = px + dx * t;
         double v = py + dy * t;
         if (t > tFar
            && BlockIterator.FastMath.gEq(u, 0.0)
            && BlockIterator.FastMath.sEq(u, 1.0)
            && BlockIterator.FastMath.gEq(v, 0.0)
            && BlockIterator.FastMath.sEq(v, 1.0)) {
            tFar = t;
         }
      } else if (dz > 0.0) {
         double t = (1.0 - pz) / dz;
         double u = px + dx * t;
         double v = py + dy * t;
         if (t > tFar
            && BlockIterator.FastMath.gEq(u, 0.0)
            && BlockIterator.FastMath.sEq(u, 1.0)
            && BlockIterator.FastMath.gEq(v, 0.0)
            && BlockIterator.FastMath.sEq(v, 1.0)) {
            tFar = t;
         }
      }

      return tFar;
   }

   @FunctionalInterface
   public interface BlockIteratorProcedure {
      boolean accept(int var1, int var2, int var3, double var4, double var6, double var8, double var10, double var12, double var14);
   }

   @FunctionalInterface
   public interface BlockIteratorProcedurePlus1<T> {
      boolean accept(int var1, int var2, int var3, double var4, double var6, double var8, double var10, double var12, double var14, T var16);
   }

   static class FastMath {
      static final double TWO_POWER_52 = 4.5035996E15F;
      static final double ROUNDING_ERROR = 1.0E-15;

      static boolean eq(double a, double b) {
         return abs(a - b) < 1.0E-15;
      }

      static boolean sEq(double a, double b) {
         return a <= b + 1.0E-15;
      }

      static boolean gEq(double a, double b) {
         return a >= b - 1.0E-15;
      }

      static double abs(double x) {
         return x < 0.0 ? -x : x;
      }

      static long fastFloor(double x) {
         if (!(x >= 4.5035996E15F) && !(x <= -4.5035996E15F)) {
            long y = (long)x;
            if (x < 0.0 && y != x) {
               y--;
            }

            return y == 0L ? (long)(x * y) : y;
         } else {
            return (long)x;
         }
      }
   }
}
