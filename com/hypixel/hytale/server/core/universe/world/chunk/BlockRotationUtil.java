package com.hypixel.hytale.server.core.universe.world.chunk;

import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockFlipType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.VariantRotation;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockRotationUtil {
   private static final int[][][] LOCAL_FLIP_CORRECTIONS = new int[][][]{
      {{0, 0, 1}, {0, 1, 0}, {1, 0, 0}}, {{0, 0, -1}, {0, 1, 0}, {-1, 0, 0}}, {{-1, 0, 0}, {0, 1, 0}, {0, 0, 1}}
   };

   @Nullable
   public static RotationTuple getFlipped(@Nonnull RotationTuple blockRotation, @Nullable BlockFlipType flipType, @Nonnull Axis axis) {
      if (flipType == null) {
         Rotation yaw = blockRotation.yaw();
         Rotation pitch = blockRotation.pitch();
         Rotation roll = blockRotation.roll();
         switch (axis) {
            case X:
               yaw = yaw.toInverse();
               roll = roll.toInverse();
               break;
            case Y:
               pitch = pitch.add(Rotation.OneEighty);
               roll = roll.toInverse();
               break;
            case Z:
               yaw = yaw.toInverse();
               pitch = pitch.toInverse();
         }

         return RotationTuple.of(yaw, pitch, roll);
      } else {
         return RotationTuple.flip(blockRotation, flipType, axis, LOCAL_FLIP_CORRECTIONS);
      }
   }

   @Nullable
   public static RotationTuple getRotated(@Nonnull RotationTuple blockRotation, @Nonnull Axis axis, Rotation rotation, @Nonnull VariantRotation variantRotation) {
      return get(blockRotation.yaw(), blockRotation.pitch(), blockRotation.roll(), axis, rotation, variantRotation, false);
   }

   @Nullable
   private static RotationTuple get(
      @Nonnull Rotation rotationYaw,
      @Nonnull Rotation rotationPitch,
      @Nonnull Rotation rotationRoll,
      @Nonnull Axis axis,
      Rotation rotation,
      @Nonnull VariantRotation variantRotation,
      boolean preventPitchRotation
   ) {
      RotationTuple rotationPair = null;
      switch (axis) {
         case X:
            rotationPair = variantRotation.rotateX(RotationTuple.of(rotationYaw, rotationPitch), rotation);
            break;
         case Y:
            rotationPair = variantRotation.verify(RotationTuple.of(rotationYaw.add(rotation), rotationPitch));
            break;
         case Z:
            rotationPair = variantRotation.rotateZ(RotationTuple.of(rotationYaw, rotationPitch), rotation);
      }

      if (rotationPair == null) {
         return null;
      } else {
         if (preventPitchRotation) {
            rotationPair = RotationTuple.of(rotationPair.yaw(), rotationPitch);
         }

         return rotationPair;
      }
   }

   public static int getFlippedFiller(int filler, @Nonnull Axis axis) {
      return getRotatedFiller(filler, axis, Rotation.OneEighty);
   }

   public static int getRotatedFiller(int filler, @Nonnull Axis axis, Rotation rotation) {
      return switch (axis) {
         case X -> rotation.rotateX(filler);
         case Y -> rotation.rotateY(filler);
         case Z -> rotation.rotateZ(filler);
      };
   }
}
