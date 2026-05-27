package com.hypixel.hytale.math.block;

import com.hypixel.hytale.function.predicate.TriIntObjPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockInvertedDomeUtil {
   public static <T> boolean forEachBlock(
      int originX, int originY, int originZ, int radiusX, int radiusY, int radiusZ, @Nullable T t, @Nonnull TriIntObjPredicate<T> consumer
   ) {
      return forEachBlock(originX, originY, originZ, radiusX, radiusY, radiusZ, false, false, t, consumer);
   }

   public static <T> boolean forEachBlock(
      int originX,
      int originY,
      int originZ,
      int radiusX,
      int radiusY,
      int radiusZ,
      boolean evenXZ,
      boolean evenH,
      @Nullable T t,
      @Nonnull TriIntObjPredicate<T> consumer
   ) {
      if (radiusX <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusX));
      } else if (radiusY <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusY));
      } else if (radiusZ <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusZ));
      } else {
         float offsetXZ = evenXZ ? 0.5F : 0.0F;
         float offsetH = evenH ? 0.5F : 0.0F;
         int maxXi = evenXZ ? radiusX - 1 : radiusX;
         int maxZi = evenXZ ? radiusZ - 1 : radiusZ;
         int maxHi = radiusY - 1;
         float radiusXAdjusted = radiusX + 0.41F;
         float radiusYAdjusted = radiusY + 0.41F;
         float radiusZAdjusted = radiusZ + 0.41F;
         float invRadiusXSqr = 1.0F / (radiusXAdjusted * radiusXAdjusted);
         float invRadiusYSqr = 1.0F / (radiusYAdjusted * radiusYAdjusted);

         for (int x = -radiusX; x <= maxXi; x++) {
            float sx = x + offsetXZ;
            float qx = 1.0F - sx * sx * invRadiusXSqr;
            if (!(qx < 0.0F)) {
               double dy = Math.sqrt(qx) * radiusYAdjusted;
               int maxYi = Math.min(maxHi, (int)(dy - offsetH));

               for (int y = 0; y <= maxYi; y++) {
                  float sy = y + offsetH;
                  double dz = Math.sqrt(qx - sy * sy * invRadiusYSqr) * radiusZAdjusted;
                  int minZi = (int)Math.ceil(-dz - offsetXZ);
                  int maxZc = (int)(dz - offsetXZ);
                  if (minZi < -radiusZ) {
                     minZi = -radiusZ;
                  }

                  if (maxZc > maxZi) {
                     maxZc = maxZi;
                  }

                  for (int z = minZi; z <= maxZc; z++) {
                     if (!consumer.test(originX + x, originY - y, originZ + z, t)) {
                        return false;
                     }
                  }
               }
            }
         }

         return true;
      }
   }

   public static <T> boolean forEachBlock(
      int originX,
      int originY,
      int originZ,
      int radiusX,
      int radiusY,
      int radiusZ,
      int thickness,
      boolean capped,
      @Nullable T t,
      @Nonnull TriIntObjPredicate<T> consumer
   ) {
      if (thickness < 1) {
         return forEachBlock(originX, originY, originZ, radiusX, radiusY, radiusZ, t, consumer);
      } else if (radiusX <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusX));
      } else if (radiusY <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusY));
      } else if (radiusZ <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusZ));
      } else {
         float radiusXAdjusted = radiusX + 0.41F;
         float radiusYAdjusted = radiusY + 0.41F;
         float radiusZAdjusted = radiusZ + 0.41F;
         float innerRadiusXAdjusted = radiusXAdjusted - thickness;
         float innerRadiusYAdjusted = radiusYAdjusted - thickness;
         float innerRadiusZAdjusted = radiusZAdjusted - thickness;
         float invRadiusX2 = 1.0F / (radiusXAdjusted * radiusXAdjusted);
         float invRadiusY2 = 1.0F / (radiusYAdjusted * radiusYAdjusted);
         float invRadiusZ2 = 1.0F / (radiusZAdjusted * radiusZAdjusted);
         float invInnerRadiusX2 = 1.0F / (innerRadiusXAdjusted * innerRadiusXAdjusted);
         float invInnerRadiusY2 = 1.0F / (innerRadiusYAdjusted * innerRadiusYAdjusted);
         float invInnerRadiusZ2 = 1.0F / (innerRadiusZAdjusted * innerRadiusZAdjusted);
         int y = 0;

         for (int y1 = 1; y < radiusY; y1++) {
            float qy = y * y * invRadiusY2;
            double dx = Math.sqrt(1.0F - qy) * radiusXAdjusted;
            int maxX = (int)dx;
            float innerQy = y * y * invInnerRadiusY2;
            float outerQy = y1 * y1 * invRadiusY2;
            boolean isAtTop = y == 0 && capped;
            int x = 0;

            for (int x1 = 1; x <= maxX; x1++) {
               float qx = x * x * invRadiusX2;
               double dz = Math.sqrt(1.0F - qx - qy) * radiusZAdjusted;
               int maxZ = (int)dz;
               float innerQx = x * x * invInnerRadiusX2;
               float outerQx = x1 * x1 * invRadiusX2;
               int z = 0;

               for (int z1 = 1; z <= maxZ; z1++) {
                  float innerQz = z * z * invInnerRadiusZ2;
                  if (isAtTop) {
                     if (!test(originX, originY, originZ, x, -y, z, t, consumer)) {
                        return false;
                     }
                  } else {
                     label60: {
                        if (innerQx + innerQy + innerQz < 1.0F) {
                           float outerQz = z1 * z1 * invRadiusZ2;
                           if (outerQx + outerQy + outerQz < 1.0F) {
                              break label60;
                           }
                        }

                        if (!test(originX, originY, originZ, x, -y, z, t, consumer)) {
                           return false;
                        }
                     }
                  }

                  z++;
               }

               x++;
            }

            y++;
         }

         return true;
      }
   }

   public static <T> boolean forEachBlock(
      int originX,
      int originY,
      int originZ,
      int radiusX,
      int radiusY,
      int radiusZ,
      int thickness,
      boolean capped,
      boolean evenXZ,
      boolean evenH,
      @Nullable T t,
      @Nonnull TriIntObjPredicate<T> consumer
   ) {
      return !evenXZ && !evenH
         ? forEachBlock(originX, originY, originZ, radiusX, radiusY, radiusZ, thickness, capped, t, consumer)
         : forEachBlock(originX, originY, originZ, radiusX, radiusY, radiusZ, evenXZ, evenH, t, consumer);
   }

   private static <T> boolean test(int originX, int originY, int originZ, int x, int y, int z, T context, @Nonnull TriIntObjPredicate<T> consumer) {
      if (!consumer.test(originX + x, originY + y, originZ + z, context)) {
         return false;
      } else {
         if (x > 0) {
            if (!consumer.test(originX - x, originY + y, originZ + z, context)) {
               return false;
            }

            if (z > 0 && !consumer.test(originX - x, originY + y, originZ - z, context)) {
               return false;
            }
         }

         return z > 0 ? consumer.test(originX + x, originY + y, originZ - z, context) : true;
      }
   }
}
