package com.hypixel.hytale.server.core.prefab;

import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.math.vector.Vector3l;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockRotationUtil;
import javax.annotation.Nonnull;

public enum PrefabRotation {
   ROTATION_0(Rotation.None, new PrefabRotation.RotationExecutor_0()),
   ROTATION_90(Rotation.Ninety, new PrefabRotation.RotationExecutor_90()),
   ROTATION_180(Rotation.OneEighty, new PrefabRotation.RotationExecutor_180()),
   ROTATION_270(Rotation.TwoSeventy, new PrefabRotation.RotationExecutor_270());

   public static final PrefabRotation[] VALUES = values();
   public static final String PREFIX = "ROTATION_";
   private final Rotation rotation;
   private final PrefabRotation.RotationExecutor executor;

   @Nonnull
   public static PrefabRotation fromRotation(@Nonnull Rotation rotation) {
      return switch (rotation) {
         case None -> ROTATION_0;
         case Ninety -> ROTATION_90;
         case OneEighty -> ROTATION_180;
         case TwoSeventy -> ROTATION_270;
      };
   }

   @Nonnull
   public static PrefabRotation valueOfExtended(@Nonnull String s) {
      return s.startsWith("ROTATION_") ? valueOf(s) : valueOf("ROTATION_" + s);
   }

   private PrefabRotation(Rotation rotation, PrefabRotation.RotationExecutor executor) {
      this.rotation = rotation;
      this.executor = executor;
   }

   public PrefabRotation add(@Nonnull PrefabRotation other) {
      int val = this.rotation.getDegrees() + other.rotation.getDegrees();
      return VALUES[val % 360 / 90];
   }

   public void rotate(@Nonnull Vector3d v) {
      double x = v.x;
      double z = v.z;
      v.x = this.executor.rotateDoubleX(x, z);
      v.z = this.executor.rotateDoubleZ(x, z);
   }

   public void rotate(@Nonnull Vector3i v) {
      int x = v.x;
      int z = v.z;
      v.x = this.executor.rotateIntX(x, z);
      v.z = this.executor.rotateIntZ(x, z);
   }

   public void rotate(@Nonnull Vector3l v) {
      long x = v.x;
      long z = v.z;
      v.x = this.executor.rotateLongX(x, z);
      v.z = this.executor.rotateLongZ(x, z);
   }

   public int getX(int x, int z) {
      return this.executor.rotateIntX(x, z);
   }

   public int getZ(int x, int z) {
      return this.executor.rotateIntZ(x, z);
   }

   public float getYaw() {
      return this.executor.getYaw();
   }

   public int getRotation(int rotation) {
      if (this.rotation == Rotation.None) {
         return rotation;
      } else {
         RotationTuple inRotation = RotationTuple.get(rotation);
         return RotationTuple.of(inRotation.yaw().add(this.rotation), inRotation.pitch(), inRotation.roll()).index();
      }
   }

   public int getFiller(int filler) {
      return this.rotation == Rotation.None ? filler : BlockRotationUtil.getRotatedFiller(filler, Axis.Y, this.rotation);
   }

   private interface RotationExecutor {
      float getYaw();

      int rotateIntX(int var1, int var2);

      long rotateLongX(long var1, long var3);

      double rotateDoubleX(double var1, double var3);

      int rotateIntZ(int var1, int var2);

      long rotateLongZ(long var1, long var3);

      double rotateDoubleZ(double var1, double var3);
   }

   private static class RotationExecutor_0 implements PrefabRotation.RotationExecutor {
      @Override
      public float getYaw() {
         return 0.0F;
      }

      @Override
      public int rotateIntX(int x, int z) {
         return x;
      }

      @Override
      public long rotateLongX(long x, long z) {
         return x;
      }

      @Override
      public double rotateDoubleX(double x, double z) {
         return x;
      }

      @Override
      public int rotateIntZ(int x, int z) {
         return z;
      }

      @Override
      public long rotateLongZ(long x, long z) {
         return z;
      }

      @Override
      public double rotateDoubleZ(double x, double z) {
         return z;
      }
   }

   private static class RotationExecutor_180 implements PrefabRotation.RotationExecutor {
      @Override
      public float getYaw() {
         return (float) -Math.PI;
      }

      @Override
      public int rotateIntX(int x, int z) {
         return -x;
      }

      @Override
      public long rotateLongX(long x, long z) {
         return -x;
      }

      @Override
      public double rotateDoubleX(double x, double z) {
         return -x;
      }

      @Override
      public int rotateIntZ(int x, int z) {
         return -z;
      }

      @Override
      public long rotateLongZ(long x, long z) {
         return -z;
      }

      @Override
      public double rotateDoubleZ(double x, double z) {
         return -z;
      }
   }

   private static class RotationExecutor_270 implements PrefabRotation.RotationExecutor {
      @Override
      public float getYaw() {
         return (float) (-Math.PI * 3.0 / 2.0);
      }

      @Override
      public int rotateIntX(int x, int z) {
         return -z;
      }

      @Override
      public long rotateLongX(long x, long z) {
         return -z;
      }

      @Override
      public double rotateDoubleX(double x, double z) {
         return -z;
      }

      @Override
      public int rotateIntZ(int x, int z) {
         return x;
      }

      @Override
      public long rotateLongZ(long x, long z) {
         return x;
      }

      @Override
      public double rotateDoubleZ(double x, double z) {
         return x;
      }
   }

   private static class RotationExecutor_90 implements PrefabRotation.RotationExecutor {
      @Override
      public float getYaw() {
         return (float) (-Math.PI / 2);
      }

      @Override
      public int rotateIntX(int x, int z) {
         return z;
      }

      @Override
      public long rotateLongX(long x, long z) {
         return z;
      }

      @Override
      public double rotateDoubleX(double x, double z) {
         return z;
      }

      @Override
      public int rotateIntZ(int x, int z) {
         return -x;
      }

      @Override
      public long rotateLongZ(long x, long z) {
         return -x;
      }

      @Override
      public double rotateDoubleZ(double x, double z) {
         return -x;
      }
   }
}
