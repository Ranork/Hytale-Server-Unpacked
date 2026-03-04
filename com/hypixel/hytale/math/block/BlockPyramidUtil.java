package com.hypixel.hytale.math.block;

import com.hypixel.hytale.function.predicate.TriIntObjPredicate;
import javax.annotation.Nonnull;

public class BlockPyramidUtil {
   public static <T> void forEachBlock(
      int originX, int originY, int originZ, int radiusX, int height, int radiusZ, T t, @Nonnull TriIntObjPredicate<T> consumer
   ) {
      if (radiusX <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusX));
      } else if (height <= 0) {
         throw new IllegalArgumentException(String.valueOf(height));
      } else if (radiusZ <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusZ));
      } else {
         for (int y = height - 1; y >= 0; y--) {
            double rf = 1.0 - (double)y / height;
            double dx = radiusX * rf;
            int maxX;
            int minX = -(maxX = (int)dx);

            for (int x = minX; x <= maxX; x++) {
               double dz = radiusZ * rf;
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
         double df = 1.0 / height;

         for (int y = height - 1; y >= 0; y--) {
            boolean cap = capped && y < thickness;
            double rf = 1.0 - y * df;
            double dx = rf * radiusX;
            double dz = rf * radiusZ;
            int maxX;
            int minX = -(maxX = (int)dx);
            int maxZ;
            int minZ = -(maxZ = (int)dz);
            double innerRf = rf - df;
            double innerDx = innerRf * radiusX;
            double innerDz = innerRf * radiusZ;
            int innerMinX = cap ? 1 : -((int)innerDx) + thickness;
            int innerMaxX = cap ? 0 : (int)innerDx - thickness;
            int innerMinZ = cap ? 1 : -((int)innerDz) + thickness;
            int innerMaxZ = cap ? 0 : (int)innerDz - thickness;

            for (int x = minX; x <= maxX; x++) {
               for (int z = minZ; z <= maxZ; z++) {
                  if ((x < innerMinX || x > innerMaxX || z < innerMinZ || z > innerMaxZ) && !consumer.test(originX + x, originY + y, originZ + z, t)) {
                     return;
                  }
               }
            }
         }
      }
   }

   public static <T> void forEachBlockInverted(
      int originX, int originY, int originZ, int radiusX, int height, int radiusZ, T t, @Nonnull TriIntObjPredicate<T> consumer
   ) {
      if (radiusX <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusX));
      } else if (height <= 0) {
         throw new IllegalArgumentException(String.valueOf(height));
      } else if (radiusZ <= 0) {
         throw new IllegalArgumentException(String.valueOf(radiusZ));
      } else {
         for (int y = height - 1; y >= 0; y--) {
            double rf = 1.0 - (double)y / height;
            double dx = radiusX * rf;
            int maxX;
            int minX = -(maxX = (int)dx);

            for (int x = minX; x <= maxX; x++) {
               double dz = radiusZ * rf;
               int maxZ;
               int minZ = -(maxZ = (int)dz);

               for (int z = minZ; z <= maxZ; z++) {
                  if (!consumer.test(originX + x, originY + height - 1 - y, originZ + z, t)) {
                     return;
                  }
               }
            }
         }
      }
   }

   public static <T> void forEachBlockInverted(
      int originX, int originY, int originZ, int radiusX, int height, int radiusZ, int thickness, T t, @Nonnull TriIntObjPredicate<T> consumer
   ) {
      forEachBlockInverted(originX, originY, originZ, radiusX, height, radiusZ, thickness, false, t, consumer);
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
         double df = 1.0 / height;

         for (int y = height - 1; y >= 0; y--) {
            boolean cap = capped && y < thickness;
            double rf = 1.0 - y * df;
            double dx = rf * radiusX;
            double dz = rf * radiusZ;
            int maxX;
            int minX = -(maxX = (int)dx);
            int maxZ;
            int minZ = -(maxZ = (int)dz);
            double innerRf = rf - df;
            double innerDx = innerRf * radiusX;
            double innerDz = innerRf * radiusZ;
            int innerMinX = cap ? 1 : -((int)innerDx) + thickness;
            int innerMaxX = cap ? 0 : (int)innerDx - thickness;
            int innerMinZ = cap ? 1 : -((int)innerDz) + thickness;
            int innerMaxZ = cap ? 0 : (int)innerDz - thickness;

            for (int x = minX; x <= maxX; x++) {
               for (int z = minZ; z <= maxZ; z++) {
                  if ((x < innerMinX || x > innerMaxX || z < innerMinZ || z > innerMaxZ)
                     && !consumer.test(originX + x, originY + height - 1 - y, originZ + z, t)) {
                     return;
                  }
               }
            }
         }
      }
   }
}
