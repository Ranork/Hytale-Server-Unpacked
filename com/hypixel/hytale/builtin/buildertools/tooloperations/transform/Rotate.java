package com.hypixel.hytale.builtin.buildertools.tooloperations.transform;

import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.Rotation;
import com.hypixel.hytale.protocol.packets.buildertools.BrushAxis;
import javax.annotation.Nonnull;

public class Rotate implements Transform {
   public static final Transform X_90 = new Rotate(Axis.X, 90);
   public static final Transform X_180 = new Rotate(Axis.X, 180);
   public static final Transform X_270 = new Rotate(Axis.X, 270);
   public static final Transform Y_90 = new Rotate(Axis.Y, 90);
   public static final Transform Y_180 = new Rotate(Axis.Y, 180);
   public static final Transform Y_270 = new Rotate(Axis.Y, 270);
   public static final Transform Z_90 = new Rotate(Axis.Z, 90);
   public static final Transform Z_180 = new Rotate(Axis.Z, 180);
   public static final Transform Z_270 = new Rotate(Axis.Z, 270);
   public static final Transform FACING_NORTH = X_90;
   public static final Transform FACING_EAST = Z_90;
   public static final Transform FACING_SOUTH = X_90.then(Y_180);
   public static final Transform FACING_WEST = Z_90.then(Y_180);
   private final Axis axis;
   private final int rotations;

   public Rotate(Axis axis) {
      this(axis, 90);
   }

   public Rotate(Axis axis, int angle) {
      angle = Math.floorMod(angle, 360);
      int rotations = angle / 90;
      this.axis = axis;
      this.rotations = rotations;
   }

   @Override
   public void apply(@Nonnull Vector3i vector3i) {
      if (this.rotations == 1) {
         this.axis.rotate(vector3i);
      } else if (this.rotations > 1) {
         for (int i = 0; i < this.rotations; i++) {
            this.axis.rotate(vector3i);
         }
      }
   }

   @Nonnull
   @Override
   public String toString() {
      return "Rotate{axis=" + this.axis + ", rotations=" + this.rotations + "}";
   }

   public static Transform forDirection(@Nonnull Vector3i direction, Rotation angle) {
      if (direction.getX() < 0) {
         return selectRotation(angle, FACING_WEST, FACING_NORTH, FACING_EAST, FACING_SOUTH);
      } else if (direction.getX() > 0) {
         return selectRotation(angle, FACING_EAST, FACING_SOUTH, FACING_WEST, FACING_NORTH);
      } else if (direction.getZ() < 0) {
         return selectRotation(angle, FACING_NORTH, FACING_EAST, FACING_SOUTH, FACING_WEST);
      } else {
         return direction.getZ() > 0 ? selectRotation(angle, FACING_SOUTH, FACING_WEST, FACING_NORTH, FACING_EAST) : NONE;
      }
   }

   public static Transform forAxisAndAngle(BrushAxis axis, Rotation angle) {
      if (axis == BrushAxis.X) {
         return selectRotation(angle, NONE, X_90, X_180, X_270);
      } else if (axis == BrushAxis.Y) {
         return selectRotation(angle, NONE, Y_90, Y_180, Y_270);
      } else {
         return axis == BrushAxis.Z ? selectRotation(angle, NONE, Z_90, Z_180, Z_270) : NONE;
      }
   }

   private static Transform selectRotation(Rotation angle, Transform rotate0, Transform rotate90, Transform rotate180, Transform rotate270) {
      if (angle == Rotation.Ninety) {
         return rotate90;
      } else if (angle == Rotation.OneEighty) {
         return rotate180;
      } else {
         return angle == Rotation.TwoSeventy ? rotate270 : rotate0;
      }
   }
}
