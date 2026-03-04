package com.hypixel.hytale.math.block;

import com.hypixel.hytale.function.predicate.TriIntObjPredicate;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public class BlockCubeUtil {
   public static <T> boolean forEachBlock(
      int originX, int originY, int originZ, int radiusX, int height, int radiusZ, T t, @Nonnull TriIntObjPredicate<T> consumer
   ) {
      int radiusY = height / 2;

      for (int dx = -radiusX; dx <= radiusX; dx++) {
         for (int dz = -radiusZ; dz <= radiusZ; dz++) {
            for (int dy = -radiusY; dy <= radiusY; dy++) {
               if (!consumer.test(originX + dx, originY + dy, originZ + dz, t)) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   public static <T> boolean forEachBlock(
      int originX, int originY, int originZ, int radiusX, int height, int radiusZ, int thickness, T t, @Nonnull TriIntObjPredicate<T> consumer
   ) {
      return forEachBlock(originX, originY, originZ, radiusX, height, radiusZ, thickness, false, t, consumer);
   }

   public static <T> boolean forEachBlock(
      int originX, int originY, int originZ, int radiusX, int height, int radiusZ, int thickness, boolean capped, T t, @Nonnull TriIntObjPredicate<T> consumer
   ) {
      return forEachBlock(originX, originY, originZ, radiusX, height, radiusZ, thickness, capped, capped, false, t, consumer);
   }

   public static <T> boolean forEachBlock(
      int originX,
      int originY,
      int originZ,
      int radiusX,
      int height,
      int radiusZ,
      int thickness,
      boolean cappedTop,
      boolean cappedBottom,
      boolean hollow,
      T t,
      @Nonnull TriIntObjPredicate<T> consumer
   ) {
      if (thickness < 1) {
         return forEachBlock(originX, originY, originZ, radiusX, height, radiusZ, t, consumer);
      } else {
         int radiusY = height / 2;
         int innerMinX = -radiusX + thickness;
         int innerMaxX = radiusX - thickness;
         int innerMinZ = -radiusZ + thickness;
         int innerMaxZ = radiusZ - thickness;
         int innerMinY = cappedBottom ? -radiusY + thickness : -height;
         int innerMaxY = cappedTop ? radiusY - thickness : height;

         for (int dx = -radiusX; dx <= radiusX; dx++) {
            for (int dz = -radiusZ; dz <= radiusZ; dz++) {
               for (int dy = -radiusY; dy <= radiusY; dy++) {
                  if (dy < innerMinY || dy > innerMaxY || dx < innerMinX || dx > innerMaxX || dz < innerMinZ || dz > innerMaxZ) {
                     if (!consumer.test(originX + dx, originY + dy, originZ + dz, t)) {
                        return false;
                     }
                  } else if (hollow && !consumer.test(originX + dx, originY + dy, originZ + dz, t)) {
                     return false;
                  }
               }
            }
         }

         return true;
      }
   }

   public static <T> boolean forEachBlock(@Nonnull Vector3i pointOne, @Nonnull Vector3i pointTwo, T t, @Nonnull TriIntObjPredicate<T> consumer) {
      Vector3i min = Vector3i.min(pointOne, pointTwo);
      Vector3i max = Vector3i.max(pointOne, pointTwo);

      for (int x = min.x; x <= max.x; x++) {
         for (int z = min.z; z <= max.z; z++) {
            for (int y = min.y; y <= max.y; y++) {
               if (!consumer.test(x, y, z, t)) {
                  return false;
               }
            }
         }
      }

      return true;
   }

   public static <T> boolean forEachBlock(
      @Nonnull Vector3i pointOne,
      @Nonnull Vector3i pointTwo,
      int thickness,
      boolean cappedTop,
      boolean cappedBottom,
      boolean hollow,
      T t,
      @Nonnull TriIntObjPredicate<T> consumer
   ) {
      if (thickness < 1) {
         return forEachBlock(pointOne, pointTwo, t, consumer);
      } else {
         Vector3i min = Vector3i.min(pointOne, pointTwo);
         Vector3i max = Vector3i.max(pointOne, pointTwo);
         int innerMinX = min.x + thickness;
         int innerMaxX = max.x - thickness;
         int innerMinZ = min.z + thickness;
         int innerMaxZ = max.z - thickness;
         int innerMinY = cappedBottom ? min.y + thickness : min.y;
         int innerMaxY = cappedTop ? max.y - thickness : max.y;

         for (int x = min.x; x <= max.x; x++) {
            for (int z = min.z; z <= max.z; z++) {
               for (int y = min.y; y <= max.y; y++) {
                  if (hollow) {
                     if (y >= innerMinY && y <= innerMaxY && x >= innerMinX && x <= innerMaxX && z >= innerMinZ && z <= innerMaxZ && !consumer.test(x, y, z, t)
                        )
                      {
                        return false;
                     }
                  } else if ((y < innerMinY || y > innerMaxY || x < innerMinX || x > innerMaxX || z < innerMinZ || z > innerMaxZ) && !consumer.test(x, y, z, t)
                     )
                   {
                     return false;
                  }
               }
            }
         }

         return true;
      }
   }
}
