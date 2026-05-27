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
      boolean evenY,
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
         float offsetY = evenY ? 0.5F : 0.0F;
         int minX = -radiusX;
         int maxX = evenXZ ? radiusX - 1 : radiusX;
         int minY = -radiusY;
         int maxY = evenY ? radiusY - 1 : radiusY;
         int minZ = -radiusZ;
         int maxZ = evenXZ ? radiusZ - 1 : radiusZ;
         float radiusXAdjusted = radiusX + 0.41F;
         float radiusYAdjusted = radiusY + 0.41F;
         float radiusZAdjusted = radiusZ + 0.41F;
         float invRadiusXSqr = 1.0F / (radiusXAdjusted * radiusXAdjusted);
         float invRadiusYSqr = 1.0F / (radiusYAdjusted * radiusYAdjusted);

         for (int x = minX; x <= maxX; x++) {
            float sx = x + offsetXZ;
            float qx = 1.0F - sx * sx * invRadiusXSqr;
            if (!(qx < 0.0F)) {
               double dy = Math.sqrt(qx) * radiusYAdjusted;
               int loY = MathUtil.ceil(-dy - offsetY);
               int hiY = (int)(dy - offsetY);
               loY = Math.max(loY, minY);
               hiY = Math.min(hiY, maxY);
               if (loY <= hiY) {
                  for (int y = loY; y <= hiY; y++) {
                     float sy = y + offsetY;
                     float qxy = qx - sy * sy * invRadiusYSqr;
                     if (!(qxy < 0.0F)) {
                        double dz = Math.sqrt(qxy) * radiusZAdjusted;
                        int loZ = MathUtil.ceil(-dz - offsetXZ);
                        int hiZ = (int)(dz - offsetXZ);
                        loZ = Math.max(loZ, minZ);
                        hiZ = Math.min(hiZ, maxZ);
                        if (loZ <= hiZ) {
                           for (int z = loZ; z <= hiZ; z++) {
                              if (!consumer.test(originX + x, originY + y, originZ + z, t)) {
                                 return false;
                              }
                           }
                        }
                     }
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
      return forEachBlock(originX, originY, originZ, radiusX, radiusY, radiusZ, thickness, false, false, t, consumer);
   }

   public static <T> boolean forEachBlock(
      int originX,
      int originY,
      int originZ,
      int radiusX,
      int radiusY,
      int radiusZ,
      int thickness,
      boolean evenXZ,
      boolean evenY,
      @Nullable T t,
      @Nonnull TriIntObjPredicate<T> consumer
   ) {
      if (thickness < 1) {
         return forEachBlock(originX, originY, originZ, radiusX, radiusY, radiusZ, evenXZ, evenY, t, consumer);
      } else if (radiusX <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusX));
      } else if (radiusY <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusY));
      } else if (radiusZ <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusZ));
      } else {
         float offsetXZ = evenXZ ? 0.5F : 0.0F;
         float offsetY = evenY ? 0.5F : 0.0F;
         int minX = -radiusX;
         int maxX = evenXZ ? radiusX - 1 : radiusX;
         int minY = -radiusY;
         int maxY = evenY ? radiusY - 1 : radiusY;
         int minZ = -radiusZ;
         int maxZ = evenXZ ? radiusZ - 1 : radiusZ;
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
         float invRadiusXSqr = invRadiusX2;
         float invRadiusYSqr = invRadiusY2;

         for (int x = minX; x <= maxX; x++) {
            float sx = x + offsetXZ;
            float qx = 1.0F - sx * sx * invRadiusXSqr;
            if (!(qx < 0.0F)) {
               double dy = Math.sqrt(qx) * radiusYAdjusted;
               int loY = MathUtil.ceil(-dy - offsetY);
               int hiY = (int)(dy - offsetY);
               loY = Math.max(loY, minY);
               hiY = Math.min(hiY, maxY);
               if (loY <= hiY) {
                  for (int y = loY; y <= hiY; y++) {
                     float sy = y + offsetY;
                     float qxy = qx - sy * sy * invRadiusYSqr;
                     if (!(qxy < 0.0F)) {
                        double dz = Math.sqrt(qxy) * radiusZAdjusted;
                        int loZ = MathUtil.ceil(-dz - offsetXZ);
                        int hiZ = (int)(dz - offsetXZ);
                        loZ = Math.max(loZ, minZ);
                        hiZ = Math.min(hiZ, maxZ);
                        if (loZ <= hiZ) {
                           for (int z = loZ; z <= hiZ; z++) {
                              float sz = z + offsetXZ;
                              float outerVal = sx * sx * invRadiusX2 + sy * sy * invRadiusY2 + sz * sz * invRadiusZ2;
                              if (!(outerVal > 1.0F)) {
                                 float innerVal = sx * sx * invInnerRadiusX2 + sy * sy * invInnerRadiusY2 + sz * sz * invInnerRadiusZ2;
                                 if (!(innerVal < 1.0F) && !consumer.test(originX + x, originY + y, originZ + z, t)) {
                                    return false;
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            }
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
