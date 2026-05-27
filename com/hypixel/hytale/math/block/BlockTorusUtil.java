package com.hypixel.hytale.math.block;

import com.hypixel.hytale.function.predicate.TriIntObjPredicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockTorusUtil {
   public static <T> boolean forEachBlock(
      int originX, int originY, int originZ, int outerRadius, int minorRadius, @Nullable T t, @Nonnull TriIntObjPredicate<T> consumer
   ) {
      return forEachBlock(originX, originY, originZ, outerRadius, minorRadius, false, false, t, consumer);
   }

   public static <T> boolean forEachBlock(
      int originX,
      int originY,
      int originZ,
      int outerRadius,
      int minorRadius,
      boolean evenXZ,
      boolean evenY,
      @Nullable T t,
      @Nonnull TriIntObjPredicate<T> consumer
   ) {
      if (outerRadius <= 0) {
         throw new IllegalArgumentException(String.valueOf(outerRadius));
      } else if (minorRadius <= 0) {
         throw new IllegalArgumentException(String.valueOf(minorRadius));
      } else {
         float offsetXZ = evenXZ ? 0.5F : 0.0F;
         float offsetY = evenY ? 0.5F : 0.0F;
         int majorRadius = Math.max(1, outerRadius - minorRadius);
         int sizeXZ = majorRadius + minorRadius;
         int maxXZ = evenXZ ? sizeXZ - 1 : sizeXZ;
         int maxY = evenY ? minorRadius - 1 : minorRadius;
         float minorRadiusAdjusted = minorRadius + 0.41F;

         for (int x = -sizeXZ; x <= maxXZ; x++) {
            double sx = x + offsetXZ;

            for (int z = -sizeXZ; z <= maxXZ; z++) {
               double sz = z + offsetXZ;
               double distFromCenter = Math.sqrt(sx * sx + sz * sz);
               double distFromRing = distFromCenter - majorRadius;

               for (int y = -minorRadius; y <= maxY; y++) {
                  double sy = y + offsetY;
                  double distFromTube = Math.sqrt(distFromRing * distFromRing + sy * sy);
                  if (distFromTube <= minorRadiusAdjusted && !consumer.test(originX + x, originY + y, originZ + z, t)) {
                     return false;
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
      int outerRadius,
      int minorRadius,
      int thickness,
      boolean capped,
      @Nullable T t,
      @Nonnull TriIntObjPredicate<T> consumer
   ) {
      if (thickness < 1) {
         return forEachBlock(originX, originY, originZ, outerRadius, minorRadius, t, consumer);
      } else if (outerRadius <= 0) {
         throw new IllegalArgumentException(String.valueOf(outerRadius));
      } else if (minorRadius <= 0) {
         throw new IllegalArgumentException(String.valueOf(minorRadius));
      } else {
         int majorRadius = Math.max(1, outerRadius - minorRadius);
         int sizeXZ = majorRadius + minorRadius;
         float minorRadiusAdjusted = minorRadius + 0.41F;
         float innerMinorRadius = Math.max(0.0F, minorRadiusAdjusted - thickness);

         for (int x = -sizeXZ; x <= sizeXZ; x++) {
            for (int z = -sizeXZ; z <= sizeXZ; z++) {
               double distFromCenter = Math.sqrt(x * x + z * z);
               double distFromRing = distFromCenter - majorRadius;

               for (int y = -minorRadius; y <= minorRadius; y++) {
                  double distFromTube = Math.sqrt(distFromRing * distFromRing + y * y);
                  boolean inOuter = distFromTube <= minorRadiusAdjusted;
                  if (inOuter) {
                     boolean inInner = distFromTube < innerMinorRadius;
                     if (!inInner && !consumer.test(originX + x, originY + y, originZ + z, t)) {
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
      int outerRadius,
      int minorRadius,
      int thickness,
      boolean capped,
      boolean evenXZ,
      boolean evenY,
      @Nullable T t,
      @Nonnull TriIntObjPredicate<T> consumer
   ) {
      return !evenXZ && !evenY
         ? forEachBlock(originX, originY, originZ, outerRadius, minorRadius, thickness, capped, t, consumer)
         : forEachBlock(originX, originY, originZ, outerRadius, minorRadius, evenXZ, evenY, t, consumer);
   }
}
