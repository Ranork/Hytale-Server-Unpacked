package com.hypixel.hytale.math.block;

import com.hypixel.hytale.function.predicate.TriIntObjPredicate;
import com.hypixel.hytale.math.util.MathUtil;
import javax.annotation.Nonnull;

public class BlockCylinderUtil {
   public static <T> boolean forEachBlock(
      int originX, int originY, int originZ, int radiusX, int height, int radiusZ, T t, @Nonnull TriIntObjPredicate<T> consumer
   ) {
      return forEachBlock(originX, originY, originZ, radiusX, height, radiusZ, false, false, t, consumer);
   }

   public static <T> boolean forEachBlock(
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
         int maxX = evenXZ ? radiusX - 1 : radiusX;
         int maxZBound = evenXZ ? radiusZ - 1 : radiusZ;
         int sizeH = height;
         float radiusXAdjusted = radiusX + 0.41F;
         float radiusZAdjusted = radiusZ + 0.41F;
         double invRadiusXSqr = 1.0 / (radiusXAdjusted * radiusXAdjusted);

         for (int x = -radiusX; x <= maxX; x++) {
            double sx = x + offsetXZ;
            double qx = 1.0 - sx * sx * invRadiusXSqr;
            if (!(qx < 0.0)) {
               double dz = Math.sqrt(qx) * radiusZAdjusted;
               int minZi = (int)Math.ceil(-dz - offsetXZ);
               int maxZi = (int)(dz - offsetXZ);
               minZi = Math.max(minZi, -radiusZ);
               maxZi = Math.min(maxZi, maxZBound);

               for (int z = minZi; z <= maxZi; z++) {
                  for (int y = sizeH - 1; y >= 0; y--) {
                     if (!consumer.test(originX + x, originY + y, originZ + z, t)) {
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
      int originX, int originY, int originZ, int radiusX, int height, int radiusZ, int thickness, T t, @Nonnull TriIntObjPredicate<T> consumer
   ) {
      return forEachBlock(originX, originY, originZ, radiusX, height, radiusZ, thickness, false, t, consumer);
   }

   public static <T> boolean forEachBlock(
      int originX, int originY, int originZ, int radiusX, int height, int radiusZ, int thickness, boolean capped, T t, @Nonnull TriIntObjPredicate<T> consumer
   ) {
      return forEachBlock(originX, originY, originZ, radiusX, height, radiusZ, thickness, capped, false, false, t, consumer);
   }

   public static <T> boolean forEachBlock(
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
      if (thickness < 1) {
         return forEachBlock(originX, originY, originZ, radiusX, height, radiusZ, evenXZ, evenH, t, consumer);
      } else if (radiusX <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusX));
      } else if (height <= 0) {
         throw new IllegalArgumentException(String.valueOf(height));
      } else if (radiusZ <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusZ));
      } else {
         float offsetXZ = evenXZ ? 0.5F : 0.0F;
         int maxX = evenXZ ? radiusX - 1 : radiusX;
         int maxZBound = evenXZ ? radiusZ - 1 : radiusZ;
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

               for (int x = -radiusX; x <= maxX; x++) {
                  double sx = x + offsetXZ;
                  double qx = 1.0 - sx * sx * invRadiusXSqr;
                  if (!(qx < 0.0)) {
                     double dz = Math.sqrt(qx) * radiusZAdjusted;
                     int minZi = (int)Math.ceil(-dz - offsetXZ);
                     int maxZi = (int)(dz - offsetXZ);
                     minZi = Math.max(minZi, -radiusZ);
                     maxZi = Math.min(maxZi, maxZBound);
                     if (minZi <= maxZi) {
                        double innerQx = 1.0 - sx * sx * invInnerRadiusXSqr;
                        double innerDZ = innerQx > 0.0 ? Math.sqrt(innerQx) * innerRadiusZAdjusted : 0.0;
                        int innerBound = !cap && !(innerDZ <= 0.0) ? MathUtil.ceil(innerDZ - offsetXZ) : 0;

                        for (int z = minZi; z <= maxZi; z++) {
                           if ((cap || !(innerDZ > 0.0) || Math.abs(z) >= innerBound) && !consumer.test(originX + x, originY + y, originZ + z, t)) {
                              return false;
                           }
                        }
                     }
                  }
               }
            }

            return true;
         } else {
            return forEachBlock(originX, originY, originZ, radiusX, height, radiusZ, evenXZ, evenH, t, consumer);
         }
      }
   }
}
