package com.hypixel.hytale.math;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public enum Axis {
   X(new Vector3i(1, 0, 0)),
   Y(new Vector3i(0, 1, 0)),
   Z(new Vector3i(0, 0, 1));

   private final Vector3i direction;

   private Axis(@Nonnull final Vector3i direction) {
      this.direction = direction;
   }

   @Nonnull
   public Vector3i getDirection() {
      return this.direction.clone();
   }

   public void rotate(@Nonnull Vector3i vector, int angle) {
      if (angle < 0) {
         angle = Math.floorMod(angle, 360);
      }

      for (int i = angle; i > 0; i -= 90) {
         this.rotate(vector);
      }
   }

   public void rotate(@Nonnull Vector3d vector, int angle) {
      if (angle < 0) {
         angle = Math.floorMod(angle, 360);
      }

      for (int i = angle; i > 0; i -= 90) {
         this.rotate(vector);
      }
   }

   public void rotate(@Nonnull Vector3i vector) {
      switch (this) {
         case X:
            vector.assign(vector.getX(), -vector.getZ(), vector.getY());
            break;
         case Y:
            vector.assign(vector.getZ(), vector.getY(), -vector.getX());
            break;
         case Z:
            vector.assign(-vector.getY(), vector.getX(), vector.getZ());
      }
   }

   public void rotate(@Nonnull Vector3d vector) {
      switch (this) {
         case X:
            vector.assign(vector.getX(), -vector.getZ(), vector.getY());
            break;
         case Y:
            vector.assign(vector.getZ(), vector.getY(), -vector.getX());
            break;
         case Z:
            vector.assign(-vector.getY(), vector.getX(), vector.getZ());
      }
   }

   public void flip(@Nonnull Vector3i vector) {
      switch (this) {
         case X:
            vector.assign(-vector.getX(), vector.getY(), vector.getZ());
            break;
         case Y:
            vector.assign(vector.getX(), -vector.getY(), vector.getZ());
            break;
         case Z:
            vector.assign(vector.getX(), vector.getY(), -vector.getZ());
      }
   }

   public void flip(@Nonnull Vector3d vector) {
      switch (this) {
         case X:
            vector.assign(-vector.getX(), vector.getY(), vector.getZ());
            break;
         case Y:
            vector.assign(vector.getX(), -vector.getY(), vector.getZ());
            break;
         case Z:
            vector.assign(vector.getX(), vector.getY(), -vector.getZ());
      }
   }

   public void flipRotation(@Nonnull Vector3f rotation) {
      switch (this) {
         case X:
            rotation.setYaw(-rotation.getYaw());
            break;
         case Y:
            rotation.setPitch(-rotation.getPitch());
            break;
         case Z:
            rotation.setYaw((float) Math.PI - rotation.getYaw());
      }
   }
}
