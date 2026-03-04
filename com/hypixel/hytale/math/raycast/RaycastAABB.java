package com.hypixel.hytale.math.raycast;

import javax.annotation.Nonnull;

public class RaycastAABB {
   public static final double EPSILON = -1.0E-8;

   public static double intersect(
      double minX, double minY, double minZ, double maxX, double maxY, double maxZ, double ox, double oy, double oz, double dx, double dy, double dz
   ) {
      double tNear = Double.POSITIVE_INFINITY;
      double t = (minX - ox) / dx;
      if (t < tNear && t > -1.0E-8) {
         double u = oz + dz * t;
         double v = oy + dy * t;
         if (u >= minZ && u <= maxZ && v >= minY && v <= maxY) {
            tNear = t;
         }
      }

      t = (maxX - ox) / dx;
      if (t < tNear && t > -1.0E-8) {
         double u = oz + dz * t;
         double v = oy + dy * t;
         if (u >= minZ && u <= maxZ && v >= minY && v <= maxY) {
            tNear = t;
         }
      }

      t = (minY - oy) / dy;
      if (t < tNear && t > -1.0E-8) {
         double u = ox + dx * t;
         double v = oz + dz * t;
         if (u >= minX && u <= maxX && v >= minZ && v <= maxZ) {
            tNear = t;
         }
      }

      t = (maxY - oy) / dy;
      if (t < tNear && t > -1.0E-8) {
         double u = ox + dx * t;
         double v = oz + dz * t;
         if (u >= minX && u <= maxX && v >= minZ && v <= maxZ) {
            tNear = t;
         }
      }

      t = (minZ - oz) / dz;
      if (t < tNear && t > -1.0E-8) {
         double u = ox + dx * t;
         double v = oy + dy * t;
         if (u >= minX && u <= maxX && v >= minY && v <= maxY) {
            tNear = t;
         }
      }

      t = (maxZ - oz) / dz;
      if (t < tNear && t > -1.0E-8) {
         double u = ox + dx * t;
         double v = oy + dy * t;
         if (u >= minX && u <= maxX && v >= minY && v <= maxY) {
            tNear = t;
         }
      }

      return tNear;
   }

   public static void intersect(
      double minX,
      double minY,
      double minZ,
      double maxX,
      double maxY,
      double maxZ,
      double ox,
      double oy,
      double oz,
      double dx,
      double dy,
      double dz,
      @Nonnull RaycastAABB.RaycastConsumer consumer
   ) {
      double tNear = Double.POSITIVE_INFINITY;
      double nx = 0.0;
      double ny = 0.0;
      double nz = 0.0;
      double t = (minX - ox) / dx;
      if (t < tNear && t > -1.0E-8) {
         double u = oz + dz * t;
         double v = oy + dy * t;
         if (u >= minZ && u <= maxZ && v >= minY && v <= maxY) {
            tNear = t;
            nx = -1.0;
         }
      }

      t = (maxX - ox) / dx;
      if (t < tNear && t > -1.0E-8) {
         double u = oz + dz * t;
         double v = oy + dy * t;
         if (u >= minZ && u <= maxZ && v >= minY && v <= maxY) {
            tNear = t;
            nx = 1.0;
         }
      }

      t = (minY - oy) / dy;
      if (t < tNear && t > -1.0E-8) {
         double u = ox + dx * t;
         double v = oz + dz * t;
         if (u >= minX && u <= maxX && v >= minZ && v <= maxZ) {
            tNear = t;
            ny = -1.0;
         }
      }

      t = (maxY - oy) / dy;
      if (t < tNear && t > -1.0E-8) {
         double u = ox + dx * t;
         double v = oz + dz * t;
         if (u >= minX && u <= maxX && v >= minZ && v <= maxZ) {
            tNear = t;
            ny = 1.0;
         }
      }

      t = (minZ - oz) / dz;
      if (t < tNear && t > -1.0E-8) {
         double u = ox + dx * t;
         double v = oy + dy * t;
         if (u >= minX && u <= maxX && v >= minY && v <= maxY) {
            tNear = t;
            nz = -1.0;
         }
      }

      t = (maxZ - oz) / dz;
      if (t < tNear && t > -1.0E-8) {
         double u = ox + dx * t;
         double v = oy + dy * t;
         if (u >= minX && u <= maxX && v >= minY && v <= maxY) {
            tNear = t;
            nz = 1.0;
         }
      }

      consumer.accept(tNear != Double.POSITIVE_INFINITY, ox, oy, oz, dx, dy, dz, tNear, nx, ny, nz);
   }

   public static <T> void intersect(
      double minX,
      double minY,
      double minZ,
      double maxX,
      double maxY,
      double maxZ,
      double ox,
      double oy,
      double oz,
      double dx,
      double dy,
      double dz,
      @Nonnull RaycastAABB.RaycastConsumerPlus1<T> consumer,
      T obj1
   ) {
      double tNear = Double.POSITIVE_INFINITY;
      double nx = 0.0;
      double ny = 0.0;
      double nz = 0.0;
      double t = (minX - ox) / dx;
      if (t < tNear && t > -1.0E-8) {
         double u = oz + dz * t;
         double v = oy + dy * t;
         if (u >= minZ && u <= maxZ && v >= minY && v <= maxY) {
            tNear = t;
            nx = -1.0;
         }
      }

      t = (maxX - ox) / dx;
      if (t < tNear && t > -1.0E-8) {
         double u = oz + dz * t;
         double v = oy + dy * t;
         if (u >= minZ && u <= maxZ && v >= minY && v <= maxY) {
            tNear = t;
            nx = 1.0;
         }
      }

      t = (minY - oy) / dy;
      if (t < tNear && t > -1.0E-8) {
         double u = ox + dx * t;
         double v = oz + dz * t;
         if (u >= minX && u <= maxX && v >= minZ && v <= maxZ) {
            tNear = t;
            ny = -1.0;
         }
      }

      t = (maxY - oy) / dy;
      if (t < tNear && t > -1.0E-8) {
         double u = ox + dx * t;
         double v = oz + dz * t;
         if (u >= minX && u <= maxX && v >= minZ && v <= maxZ) {
            tNear = t;
            ny = 1.0;
         }
      }

      t = (minZ - oz) / dz;
      if (t < tNear && t > -1.0E-8) {
         double u = ox + dx * t;
         double v = oy + dy * t;
         if (u >= minX && u <= maxX && v >= minY && v <= maxY) {
            tNear = t;
            nz = -1.0;
         }
      }

      t = (maxZ - oz) / dz;
      if (t < tNear && t > -1.0E-8) {
         double u = ox + dx * t;
         double v = oy + dy * t;
         if (u >= minX && u <= maxX && v >= minY && v <= maxY) {
            tNear = t;
            nz = 1.0;
         }
      }

      consumer.accept(tNear != Double.POSITIVE_INFINITY, ox, oy, oz, dx, dy, dz, tNear, nx, ny, nz, obj1);
   }

   public static <T, K> void intersect(
      double minX,
      double minY,
      double minZ,
      double maxX,
      double maxY,
      double maxZ,
      double ox,
      double oy,
      double oz,
      double dx,
      double dy,
      double dz,
      @Nonnull RaycastAABB.RaycastConsumerPlus2<T, K> consumer,
      T obj1,
      K obj2
   ) {
      double tNear = Double.POSITIVE_INFINITY;
      double nx = 0.0;
      double ny = 0.0;
      double nz = 0.0;
      double t = (minX - ox) / dx;
      if (t < tNear && t > -1.0E-8) {
         double u = oz + dz * t;
         double v = oy + dy * t;
         if (u >= minZ && u <= maxZ && v >= minY && v <= maxY) {
            tNear = t;
            nx = -1.0;
         }
      }

      t = (maxX - ox) / dx;
      if (t < tNear && t > -1.0E-8) {
         double u = oz + dz * t;
         double v = oy + dy * t;
         if (u >= minZ && u <= maxZ && v >= minY && v <= maxY) {
            tNear = t;
            nx = 1.0;
         }
      }

      t = (minY - oy) / dy;
      if (t < tNear && t > -1.0E-8) {
         double u = ox + dx * t;
         double v = oz + dz * t;
         if (u >= minX && u <= maxX && v >= minZ && v <= maxZ) {
            tNear = t;
            ny = -1.0;
         }
      }

      t = (maxY - oy) / dy;
      if (t < tNear && t > -1.0E-8) {
         double u = ox + dx * t;
         double v = oz + dz * t;
         if (u >= minX && u <= maxX && v >= minZ && v <= maxZ) {
            tNear = t;
            ny = 1.0;
         }
      }

      t = (minZ - oz) / dz;
      if (t < tNear && t > -1.0E-8) {
         double u = ox + dx * t;
         double v = oy + dy * t;
         if (u >= minX && u <= maxX && v >= minY && v <= maxY) {
            tNear = t;
            nz = -1.0;
         }
      }

      t = (maxZ - oz) / dz;
      if (t < tNear && t > -1.0E-8) {
         double u = ox + dx * t;
         double v = oy + dy * t;
         if (u >= minX && u <= maxX && v >= minY && v <= maxY) {
            tNear = t;
            nz = 1.0;
         }
      }

      consumer.accept(tNear != Double.POSITIVE_INFINITY, ox, oy, oz, dx, dy, dz, tNear, nx, ny, nz, obj1, obj2);
   }

   public static <T, K, L> void intersect(
      double minX,
      double minY,
      double minZ,
      double maxX,
      double maxY,
      double maxZ,
      double ox,
      double oy,
      double oz,
      double dx,
      double dy,
      double dz,
      @Nonnull RaycastAABB.RaycastConsumerPlus3<T, K, L> consumer,
      T obj1,
      K obj2,
      L obj3
   ) {
      double tNear = Double.POSITIVE_INFINITY;
      double nx = 0.0;
      double ny = 0.0;
      double nz = 0.0;
      double t = (minX - ox) / dx;
      if (t < tNear && t > -1.0E-8) {
         double u = oz + dz * t;
         double v = oy + dy * t;
         if (u >= minZ && u <= maxZ && v >= minY && v <= maxY) {
            tNear = t;
            nx = -1.0;
         }
      }

      t = (maxX - ox) / dx;
      if (t < tNear && t > -1.0E-8) {
         double u = oz + dz * t;
         double v = oy + dy * t;
         if (u >= minZ && u <= maxZ && v >= minY && v <= maxY) {
            tNear = t;
            nx = 1.0;
         }
      }

      t = (minY - oy) / dy;
      if (t < tNear && t > -1.0E-8) {
         double u = ox + dx * t;
         double v = oz + dz * t;
         if (u >= minX && u <= maxX && v >= minZ && v <= maxZ) {
            tNear = t;
            ny = -1.0;
         }
      }

      t = (maxY - oy) / dy;
      if (t < tNear && t > -1.0E-8) {
         double u = ox + dx * t;
         double v = oz + dz * t;
         if (u >= minX && u <= maxX && v >= minZ && v <= maxZ) {
            tNear = t;
            ny = 1.0;
         }
      }

      t = (minZ - oz) / dz;
      if (t < tNear && t > -1.0E-8) {
         double u = ox + dx * t;
         double v = oy + dy * t;
         if (u >= minX && u <= maxX && v >= minY && v <= maxY) {
            tNear = t;
            nz = -1.0;
         }
      }

      t = (maxZ - oz) / dz;
      if (t < tNear && t > -1.0E-8) {
         double u = ox + dx * t;
         double v = oy + dy * t;
         if (u >= minX && u <= maxX && v >= minY && v <= maxY) {
            tNear = t;
            nz = 1.0;
         }
      }

      consumer.accept(tNear != Double.POSITIVE_INFINITY, ox, oy, oz, dx, dy, dz, tNear, nx, ny, nz, obj1, obj2, obj3);
   }

   @FunctionalInterface
   public interface RaycastConsumer {
      void accept(
         boolean var1, double var2, double var4, double var6, double var8, double var10, double var12, double var14, double var16, double var18, double var20
      );
   }

   @FunctionalInterface
   public interface RaycastConsumerPlus1<T> {
      void accept(
         boolean var1,
         double var2,
         double var4,
         double var6,
         double var8,
         double var10,
         double var12,
         double var14,
         double var16,
         double var18,
         double var20,
         T var22
      );
   }

   @FunctionalInterface
   public interface RaycastConsumerPlus2<T, K> {
      void accept(
         boolean var1,
         double var2,
         double var4,
         double var6,
         double var8,
         double var10,
         double var12,
         double var14,
         double var16,
         double var18,
         double var20,
         T var22,
         K var23
      );
   }

   @FunctionalInterface
   public interface RaycastConsumerPlus3<T, K, L> {
      void accept(
         boolean var1,
         double var2,
         double var4,
         double var6,
         double var8,
         double var10,
         double var12,
         double var14,
         double var16,
         double var18,
         double var20,
         T var22,
         K var23,
         L var24
      );
   }
}
