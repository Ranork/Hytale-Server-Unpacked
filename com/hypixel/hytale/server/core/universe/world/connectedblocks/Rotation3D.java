package com.hypixel.hytale.server.core.universe.world.connectedblocks;

import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import javax.annotation.Nonnull;

@Deprecated(forRemoval = true)
public class Rotation3D {
   public Rotation rotationYaw;
   public Rotation rotationPitch;
   public Rotation rotationRoll;

   public Rotation3D(Rotation rotationYaw, Rotation rotationPitch, Rotation rotationRoll) {
      this.rotationYaw = rotationYaw;
      this.rotationPitch = rotationPitch;
      this.rotationRoll = rotationRoll;
   }

   public void assign(Rotation yaw, Rotation pitch, Rotation roll) {
      this.rotationYaw = yaw;
      this.rotationPitch = pitch;
      this.rotationRoll = roll;
   }

   public void assign(@Nonnull RotationTuple rotation) {
      this.assign(rotation.yaw(), rotation.pitch(), rotation.roll());
   }

   public void add(@Nonnull Rotation3D toAdd) {
      this.rotationYaw = this.rotationYaw.add(toAdd.rotationYaw);
      this.rotationPitch = this.rotationPitch.add(toAdd.rotationPitch);
      this.rotationRoll = this.rotationRoll.add(toAdd.rotationRoll);
   }

   public void subtract(@Nonnull Rotation3D toSubtract) {
      this.rotationYaw = this.rotationYaw.subtract(toSubtract.rotationYaw);
      this.rotationPitch = this.rotationPitch.subtract(toSubtract.rotationPitch);
      this.rotationRoll = this.rotationRoll.subtract(toSubtract.rotationRoll);
   }

   public void negate() {
      this.assign(Rotation.None.subtract(this.rotationYaw), Rotation.None.subtract(this.rotationPitch), Rotation.None.subtract(this.rotationRoll));
   }

   @Nonnull
   public Rotation3D rotateSelfBy(@Nonnull Rotation rotationYawToRotate, @Nonnull Rotation rotationPitchToRotate, @Nonnull Rotation rotationRollToRotate) {
      Vector3f vector3f = new Vector3f(this.rotationPitch.getDegrees(), this.rotationYaw.getDegrees(), this.rotationRoll.getDegrees());
      vector3f = Rotation.rotate(vector3f, rotationYawToRotate, rotationPitchToRotate, rotationRollToRotate);
      this.assign(Rotation.closestOfDegrees(vector3f.y), Rotation.closestOfDegrees(vector3f.x), Rotation.closestOfDegrees(vector3f.z));
      return this;
   }

   public void rotateSelfBy(@Nonnull Rotation3D rotation) {
      this.rotateSelfBy(rotation.rotationYaw, rotation.rotationPitch, rotation.rotationRoll);
   }
}
