package com.hypixel.hytale.math.block;

import com.hypixel.hytale.function.predicate.TriIntObjPredicate;
import javax.annotation.Nonnull;

public class BlockConeUtil {
   public static <T> void forEachBlock(
      int originX, int originY, int originZ, int radiusX, int height, int radiusZ, T t, @Nonnull TriIntObjPredicate<T> consumer
   ) {
      forEachBlock(originX, originY, originZ, radiusX, height, radiusZ, false, false, t, consumer);
   }

   public static <T> void forEachBlock(
      int originX, int originY, int originZ, int radiusX, int height, int radiusZ, boolean evenXZ, boolean evenH, T t, @Nonnull TriIntObjPredicate<T> consumer
   ) {
      if (radiusX <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusX));
      } else if (height <= 0) {
         throw new IllegalArgumentException(String.valueOf(height));
      } else if (radiusZ <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusZ));
      } else {
         float offsetXZ = evenXZ ? 0.5F : 0.0F;
         float offsetH = evenH ? 0.5F : 0.0F;
         int maxXi = evenXZ ? radiusX - 1 : radiusX;
         int maxZi = evenXZ ? radiusZ - 1 : radiusZ;
         float radiusXAdjusted = radiusX + 0.41F;
         float radiusZAdjusted = radiusZ + 0.41F;

         for (int y = height - 1; y >= 0; y--) {
            double sy = y + offsetH;
            double rf = 1.0 - sy / height;
            double dx = radiusXAdjusted * rf;

            for (int x = -radiusX; x <= maxXi; x++) {
               double sx = x + offsetXZ;
               if (!(Math.abs(sx) > dx)) {
                  double qx = 1.0 - sx * sx / (dx * dx);
                  double dz = Math.sqrt(qx) * radiusZAdjusted * rf;
                  int minZi = (int)Math.ceil(-dz - offsetXZ);
                  int maxZc = (int)(dz - offsetXZ);
                  if (minZi < -radiusZ) {
                     minZi = -radiusZ;
                  }

                  if (maxZc > maxZi) {
                     maxZc = maxZi;
                  }

                  for (int z = minZi; z <= maxZc; z++) {
                     if (!consumer.test(originX + x, originY + y, originZ + z, t)) {
                        return;
                     }
                  }
               }
            }
         }
      }
   }

   public static <T> void forEachBlock(
      int originX, int originY, int originZ, int radiusX, int height, int radiusZ, int thickness, T t, @Nonnull TriIntObjPredicate<T> consumer
   ) {
      forEachBlock(originX, originY, originZ, radiusX, height, radiusZ, thickness, false, t, consumer);
   }

   public static <T> void forEachBlock(
      int originX, int originY, int originZ, int radiusX, int height, int radiusZ, int thickness, boolean capped, T t, @Nonnull TriIntObjPredicate<T> consumer
   ) {
      if (thickness < 1) {
         forEachBlock(originX, originY, originZ, radiusX, height, radiusZ, t, consumer);
      } else if (radiusX <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusX));
      } else if (height <= 0) {
         throw new IllegalArgumentException(String.valueOf(height));
      } else if (radiusZ <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusZ));
      } else {
         float radiusXAdjusted = radiusX + 0.41F;

         for (int y = height - 1; y >= 0; y--) {
            boolean cap = capped && y < thickness;
            double rf = 1.0 - (double)y / height;
            double dx = radiusXAdjusted * rf;
            double dxInvSqr = 1.0 / (dx * dx);
            double innerDx = dx > thickness ? dx - thickness : 0.0;
            double innerDxInvSqr = innerDx > 0.0 ? 1.0 / (innerDx * innerDx) : 0.0;
            int maxX;
            int minX = -(maxX = (int)dx);

            for (int x = minX; x <= maxX; x++) {
               double dz = Math.sqrt(1.0 - x * x * dxInvSqr) * dx;
               int maxZ;
               int minZ = -(maxZ = (int)dz);
               double innerMaxZ = cap ? 0.0 : Math.sqrt(1.0 - x * x * innerDxInvSqr) * innerDx;
               double innerMinZ = cap ? 0.0 : -innerMaxZ;

               for (int z = minZ; z <= maxZ; z++) {
                  if ((!(z > innerMinZ) || !(z < innerMaxZ)) && !consumer.test(originX + x, originY + y, originZ + z, t)) {
                     return;
                  }
               }
            }
         }
      }
   }

   public static <T> void forEachBlock(
      int originX,
      int originY,
      int originZ,
      int radiusX,
      int height,
      int radiusZ,
      int thickness,
      boolean capped,
      boolean evenXZ,
      boolean evenH,
      T t,
      @Nonnull TriIntObjPredicate<T> consumer
   ) {
      if (!evenXZ && !evenH) {
         forEachBlock(originX, originY, originZ, radiusX, height, radiusZ, thickness, capped, t, consumer);
      } else {
         forEachBlock(originX, originY, originZ, radiusX, height, radiusZ, evenXZ, evenH, t, consumer);
      }
   }

   public static <T> void forEachBlockInverted(
      int originX, int originY, int originZ, int radiusX, int height, int radiusZ, T t, @Nonnull TriIntObjPredicate<T> consumer
   ) {
      forEachBlockInverted(originX, originY, originZ, radiusX, height, radiusZ, false, false, t, consumer);
   }

   public static <T> void forEachBlockInverted(
      int originX, int originY, int originZ, int radiusX, int height, int radiusZ, boolean evenXZ, boolean evenH, T t, @Nonnull TriIntObjPredicate<T> consumer
   ) {
      if (radiusX <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusX));
      } else if (height <= 0) {
         throw new IllegalArgumentException(String.valueOf(height));
      } else if (radiusZ <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusZ));
      } else {
         float offsetXZ = evenXZ ? 0.5F : 0.0F;
         float offsetH = evenH ? 0.5F : 0.0F;
         int maxXi = evenXZ ? radiusX - 1 : radiusX;
         int maxZi = evenXZ ? radiusZ - 1 : radiusZ;
         float radiusXAdjusted = radiusX + 0.41F;
         float radiusZAdjusted = radiusZ + 0.41F;

         for (int y = height - 1; y >= 0; y--) {
            double sy = y + offsetH;
            double rf = 1.0 - sy / height;
            double dx = radiusXAdjusted * rf;

            for (int x = -radiusX; x <= maxXi; x++) {
               double sx = x + offsetXZ;
               if (!(Math.abs(sx) > dx)) {
                  double qx = 1.0 - sx * sx / (dx * dx);
                  double dz = Math.sqrt(qx) * radiusZAdjusted * rf;
                  int minZi = (int)Math.ceil(-dz - offsetXZ);
                  int maxZc = (int)(dz - offsetXZ);
                  if (minZi < -radiusZ) {
                     minZi = -radiusZ;
                  }

                  if (maxZc > maxZi) {
                     maxZc = maxZi;
                  }

                  for (int z = minZi; z <= maxZc; z++) {
                     if (!consumer.test(originX + x, originY + height - 1 - y, originZ + z, t)) {
                        return;
                     }
                  }
               }
            }
         }
      }
   }

   public static <T> void forEachBlockInverted(
      int originX, int originY, int originZ, int radiusX, int height, int radiusZ, int thickness, T t, @Nonnull TriIntObjPredicate<T> consumer
   ) {
      forEachBlock(originX, originY, originZ, radiusX, height, radiusZ, thickness, false, t, consumer);
   }

   public static <T> void forEachBlockInverted(
      int originX, int originY, int originZ, int radiusX, int height, int radiusZ, int thickness, boolean capped, T t, @Nonnull TriIntObjPredicate<T> consumer
   ) {
      if (thickness < 1) {
         forEachBlockInverted(originX, originY, originZ, radiusX, height, radiusZ, t, consumer);
      } else if (radiusX <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusX));
      } else if (height <= 0) {
         throw new IllegalArgumentException(String.valueOf(height));
      } else if (radiusZ <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusZ));
      } else {
         float radiusXAdjusted = radiusX + 0.41F;

         for (int y = height - 1; y >= 0; y--) {
            boolean cap = capped && y < thickness;
            double rf = 1.0 - (double)y / height;
            double dx = radiusXAdjusted * rf;
            double dxInvSqr = 1.0 / (dx * dx);
            double innerDx = dx > thickness ? dx - thickness : 0.0;
            double innerDxInvSqr = innerDx > 0.0 ? 1.0 / (innerDx * innerDx) : 0.0;
            int maxX;
            int minX = -(maxX = (int)dx);

            for (int x = minX; x <= maxX; x++) {
               double dz = Math.sqrt(1.0 - x * x * dxInvSqr) * dx;
               int maxZ;
               int minZ = -(maxZ = (int)dz);
               double innerMaxZ = cap ? 0.0 : Math.sqrt(1.0 - x * x * innerDxInvSqr) * innerDx;
               double innerMinZ = cap ? 0.0 : -innerMaxZ;

               for (int z = minZ; z <= maxZ; z++) {
                  if ((!(z > innerMinZ) || !(z < innerMaxZ)) && !consumer.test(originX + x, originY + height - 1 - y, originZ + z, t)) {
                     return;
                  }
               }
            }
         }
      }
   }

   public static <T> void forEachBlockInverted(
      int originX,
      int originY,
      int originZ,
      int radiusX,
      int height,
      int radiusZ,
      int thickness,
      boolean capped,
      boolean evenXZ,
      boolean evenH,
      T t,
      @Nonnull TriIntObjPredicate<T> consumer
   ) {
      if (!evenXZ && !evenH) {
         forEachBlockInverted(originX, originY, originZ, radiusX, height, radiusZ, thickness, capped, t, consumer);
      } else {
         forEachBlockInverted(originX, originY, originZ, radiusX, height, radiusZ, evenXZ, evenH, t, consumer);
      }
   }
}
