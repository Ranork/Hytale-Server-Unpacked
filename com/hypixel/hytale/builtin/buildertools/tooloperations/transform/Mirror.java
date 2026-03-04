package com.hypixel.hytale.builtin.buildertools.tooloperations.transform;

import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.packets.buildertools.BrushAxis;
import javax.annotation.Nonnull;

public class Mirror implements Transform {
   public static final Transform X = new Mirror(Axis.X);
   public static final Transform Y = new Mirror(Axis.Y);
   public static final Transform Z = new Mirror(Axis.Z);
   private final Axis axis;

   private Mirror(Axis axis) {
      this.axis = axis;
   }

   @Override
   public void apply(@Nonnull Vector3i vector3i) {
      this.axis.flip(vector3i);
   }

   @Nonnull
   @Override
   public String toString() {
      return "Mirror{axis=" + this.axis + "}";
   }

   public static Transform forAxis(BrushAxis axis) {
      if (axis == BrushAxis.X) {
         return X;
      } else if (axis == BrushAxis.Y) {
         return Y;
      } else {
         return axis == BrushAxis.Z ? Z : NONE;
      }
   }

   public static Transform forDirection(@Nonnull Vector3i direction) {
      return forDirection(direction, true);
   }

   public static Transform forDirection(@Nonnull Vector3i direction, boolean negativeY) {
      if (direction.getX() != 0) {
         return X;
      } else if (direction.getZ() != 0) {
         return Z;
      } else if (direction.getY() > 0) {
         return Y;
      } else {
         return direction.getY() < 0 && negativeY ? Y : NONE;
      }
   }
}
