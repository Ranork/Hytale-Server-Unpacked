package com.hypixel.hytale.math.block;

import com.hypixel.hytale.function.predicate.TriIntObjPredicate;
import com.hypixel.hytale.math.util.MathUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockSphereUtil {
   public static <T> void forEachBlockExact(int originX, int originY, int originZ, double radius, @Nullable T t, @Nonnull TriIntObjPredicate<T> consumer) {
      if (radius <= 0.0) {
         throw new IllegalArgumentException(String.valueOf(radius));
      } else {
         int ceiledRadius = MathUtil.ceil(radius);
         double invRadiusXSqr = 1.0 / (ceiledRadius * ceiledRadius);
         double invRadiusYSqr = 1.0 / (ceiledRadius * ceiledRadius);

         for (int x = -ceiledRadius; x <= ceiledRadius; x++) {
            double qx = 1.0 - x * x * invRadiusXSqr;
            double dy = Math.sqrt(qx) * ceiledRadius;
            int maxY;
            int minY = -(maxY = (int)dy);

            for (int y = maxY; y >= minY; y--) {
               double dz = Math.sqrt(qx - y * y * invRadiusYSqr) * ceiledRadius;
               int maxZ;
               int minZ = -(maxZ = (int)dz);

               for (int z = minZ; z <= maxZ; z++) {
                  if (!consumer.test(originX + x, originY + y, originZ + z, t)) {
                     return;
                  }
               }
            }
         }
      }
   }

   public static <T> void forEachBlock(int originX, int originY, int originZ, int radius, @Nullable T t, @Nonnull TriIntObjPredicate<T> consumer) {
      forEachBlock(originX, originY, originZ, radius, radius, radius, t, consumer);
   }

   public static <T> boolean forEachBlock(
      int originX, int originY, int originZ, int radiusX, int radiusY, int radiusZ, @Nullable T t, @Nonnull TriIntObjPredicate<T> consumer
   ) {
      if (radiusX <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusX));
      } else if (radiusY <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusY));
      } else if (radiusZ <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusZ));
      } else {
         float radiusXAdjusted = radiusX + 0.41F;
         float radiusYAdjusted = radiusY + 0.41F;
         float radiusZAdjusted = radiusZ + 0.41F;
         float invRadiusXSqr = 1.0F / (radiusXAdjusted * radiusXAdjusted);
         float invRadiusYSqr = 1.0F / (radiusYAdjusted * radiusYAdjusted);

         for (int x = 0; x <= radiusX; x++) {
            float qx = 1.0F - x * x * invRadiusXSqr;
            double dy = Math.sqrt(qx) * radiusYAdjusted;
            int maxY = (int)dy;

            for (int y = 0; y <= maxY; y++) {
               double dz = Math.sqrt(qx - y * y * invRadiusYSqr) * radiusZAdjusted;
               int maxZ = (int)dz;

               for (int z = 0; z <= maxZ; z++) {
                  if (!test(originX, originY, originZ, x, y, z, t, consumer)) {
                     return false;
                  }
               }
            }
         }

         return true;
      }
   }

   public static <T> void forEachBlock(int originX, int originY, int originZ, int radius, int thickness, @Nullable T t, @Nonnull TriIntObjPredicate<T> consumer) {
      forEachBlock(originX, originY, originZ, radius, radius, radius, thickness, t, consumer);
   }

   public static <T> boolean forEachBlock(
      int originX, int originY, int originZ, int radiusX, int radiusY, int radiusZ, int thickness, @Nullable T t, @Nonnull TriIntObjPredicate<T> consumer
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

         for (int y1 = 1; y <= radiusY; y1++) {
            float qy = y * y * invRadiusY2;
            double dx = Math.sqrt(1.0F - qy) * radiusXAdjusted;
            int maxX = (int)dx;
            float innerQy = y * y * invInnerRadiusY2;
            float outerQy = y1 * y1 * invRadiusY2;
            int x = 0;

            for (int x1 = 1; x <= maxX; x1++) {
               float qx = x * x * invRadiusX2;
               double dz = Math.sqrt(1.0F - qx - qy) * radiusZAdjusted;
               int maxZ = (int)dz;
               float innerQx = x * x * invInnerRadiusX2;
               float outerQx = x1 * x1 * invRadiusX2;
               int z = 0;

               for (int z1 = 1; z <= maxZ; z1++) {
                  label47: {
                     float innerQz = z * z * invInnerRadiusZ2;
                     if (innerQx + innerQy + innerQz < 1.0F) {
                        float outerQz = z1 * z1 * invRadiusZ2;
                        if (outerQx + outerQy + outerQz < 1.0F) {
                           break label47;
                        }
                     }

                     if (!test(originX, originY, originZ, x, y, z, t, consumer)) {
                        return false;
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

   private static <T> boolean test(int originX, int originY, int originZ, int x, int y, int z, T context, @Nonnull TriIntObjPredicate<T> consumer) {
      if (!consumer.test(originX + x, originY + y, originZ + z, context)) {
         return false;
      } else {
         if (x > 0) {
            if (!consumer.test(originX - x, originY + y, originZ + z, context)) {
               return false;
            }

            if (y > 0 && !consumer.test(originX - x, originY - y, originZ + z, context)) {
               return false;
            }

            if (z > 0 && !consumer.test(originX - x, originY + y, originZ - z, context)) {
               return false;
            }

            if (y > 0 && z > 0 && !consumer.test(originX - x, originY - y, originZ - z, context)) {
               return false;
            }
         }

         if (y > 0) {
            if (!consumer.test(originX + x, originY - y, originZ + z, context)) {
               return false;
            }

            if (z > 0 && !consumer.test(originX + x, originY - y, originZ - z, context)) {
               return false;
            }
         }

         return z > 0 ? consumer.test(originX + x, originY + y, originZ - z, context) : true;
      }
   }
}
