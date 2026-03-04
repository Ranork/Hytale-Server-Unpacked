package com.hypixel.hytale.math.block;

import com.hypixel.hytale.function.predicate.TriIntObjPredicate;
import com.hypixel.hytale.math.util.MathUtil;
import javax.annotation.Nonnull;

public class BlockCylinderUtil {
   public static <T> boolean forEachBlock(
      int originX, int originY, int originZ, int radiusX, int height, int radiusZ, T t, @Nonnull TriIntObjPredicate<T> consumer
   ) {
      if (radiusX <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusX));
      } else if (height <= 0) {
         throw new IllegalArgumentException(String.valueOf(height));
      } else if (radiusZ <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusZ));
      } else {
         float radiusXAdjusted = radiusX + 0.41F;
         float radiusZAdjusted = radiusZ + 0.41F;
         double invRadiusXSqr = 1.0 / (radiusXAdjusted * radiusXAdjusted);

         for (int x = -radiusX; x <= radiusX; x++) {
            double qx = 1.0 - x * x * invRadiusXSqr;
            double dz = Math.sqrt(qx) * radiusZAdjusted;
            int maxZ;
            int minZ = -(maxZ = (int)dz);

            for (int z = minZ; z <= maxZ; z++) {
               for (int y = height - 1; y >= 0; y--) {
                  if (!consumer.test(originX + x, originY + y, originZ + z, t)) {
                     return false;
                  }
               }
            }
         }

         return true;
      }
   }

   public static <T> boolean forEachBlock(
      int originX, int originY, int originZ, int radiusX, int height, int radiusZ, int thickness, T t, @Nonnull TriIntObjPredicate<T> consumer
   ) {
      return forEachBlock(originX, originY, originZ, radiusX, height, radiusZ, thickness, false, t, consumer);
   }

   public static <T> boolean forEachBlock(
      int originX, int originY, int originZ, int radiusX, int height, int radiusZ, int thickness, boolean capped, T t, @Nonnull TriIntObjPredicate<T> consumer
   ) {
      if (thickness < 1) {
         return forEachBlock(originX, originY, originZ, radiusX, height, radiusZ, t, consumer);
      } else if (radiusX <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusX));
      } else if (height <= 0) {
         throw new IllegalArgumentException(String.valueOf(height));
      } else if (radiusZ <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusZ));
      } else {
         float radiusXAdjusted = radiusX + 0.41F;
         float radiusZAdjusted = radiusZ + 0.41F;
         float innerRadiusXAdjusted = radiusXAdjusted - thickness;
         float innerRadiusZAdjusted = radiusZAdjusted - thickness;
         if (!(innerRadiusXAdjusted <= 0.0F) && !(innerRadiusZAdjusted <= 0.0F)) {
            double invRadiusXSqr = 1.0 / (radiusXAdjusted * radiusXAdjusted);
            double invInnerRadiusXSqr = 1.0 / (innerRadiusXAdjusted * innerRadiusXAdjusted);
            int innerMinY = thickness;
            int innerMaxY = height - thickness;

            for (int y = height - 1; y >= 0; y--) {
               boolean cap = capped && (y < innerMinY || y > innerMaxY);

               for (int x = -radiusX; x <= radiusX; x++) {
                  double qx = 1.0 - x * x * invRadiusXSqr;
                  double dz = Math.sqrt(qx) * radiusZAdjusted;
                  int maxZ = (int)dz;
                  double innerQx = x < innerRadiusXAdjusted ? 1.0 - x * x * invInnerRadiusXSqr : 0.0;
                  double innerDZ = innerQx > 0.0 ? Math.sqrt(innerQx) * innerRadiusZAdjusted : 0.0;
                  int minZ = cap ? 0 : MathUtil.ceil(innerDZ);
                  int z = minZ;
                  if (minZ == 0) {
                     if (!consumer.test(originX + x, originY + y, originZ, t)) {
                        return false;
                     }

                     z = minZ + 1;
                  }

                  while (z <= maxZ) {
                     if (!consumer.test(originX + x, originY + y, originZ + z, t)) {
                        return false;
                     }

                     if (!consumer.test(originX + x, originY + y, originZ - z, t)) {
                        return false;
                     }

                     z++;
                  }
               }
            }

            return true;
         } else {
            return forEachBlock(originX, originY, originZ, radiusX, height, radiusZ, t, consumer);
         }
      }
   }
}
