package com.hypixel.hytale.math;

import com.hypixel.hytale.math.vector.Rotation3f;
import javax.annotation.Nonnull;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.joml.Vector3ic;

public enum Axis {
   X(new Vector3i(1, 0, 0)),
   Y(new Vector3i(0, 1, 0)),
   Z(new Vector3i(0, 0, 1));

   private final Vector3ic direction;

   private Axis(@Nonnull final Vector3i direction) {
      this.direction = direction;
   }

   @Nonnull
   public Vector3ic getDirection() {
      return this.direction;
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
            vector.set(vector.x(), -vector.z(), vector.y());
            break;
         case Y:
            vector.set(vector.z(), vector.y(), -vector.x());
            break;
         case Z:
            vector.set(-vector.y(), vector.x(), vector.z());
      }
   }

   public void rotate(@Nonnull Vector3d vector) {
      switch (this) {
         case X:
            vector.set(vector.x(), -vector.z(), vector.y());
            break;
         case Y:
            vector.set(vector.z(), vector.y(), -vector.x());
            break;
         case Z:
            vector.set(-vector.y(), vector.x(), vector.z());
      }
   }

   public void flip(@Nonnull Vector3i vector) {
      switch (this) {
         case X:
            vector.set(-vector.x(), vector.y(), vector.z());
            break;
         case Y:
            vector.set(vector.x(), -vector.y(), vector.z());
            break;
         case Z:
            vector.set(vector.x(), vector.y(), -vector.z());
      }
   }

   public void flip(@Nonnull Vector3d vector) {
      switch (this) {
         case X:
            vector.set(-vector.x(), vector.y(), vector.z());
            break;
         case Y:
            vector.set(vector.x(), -vector.y(), vector.z());
            break;
         case Z:
            vector.set(vector.x(), vector.y(), -vector.z());
      }
   }

   public void flipRotation(@Nonnull Rotation3f rotation) {
      switch (this) {
         case X:
            rotation.setYaw(-rotation.yaw());
            break;
         case Y:
            rotation.setPitch(-rotation.pitch());
            break;
         case Z:
            rotation.setYaw((float) Math.PI - rotation.yaw());
      }
   }
}
