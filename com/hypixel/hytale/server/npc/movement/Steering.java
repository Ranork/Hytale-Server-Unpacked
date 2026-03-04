package com.hypixel.hytale.server.npc.movement;

import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Steering {
   public static final Steering NULL = new Steering().clear();
   private final Vector3d translation = new Vector3d();
   private double maxDistance = Double.MAX_VALUE;
   private Vector3d maxDistanceComponentSelector;
   private boolean hasTranslation;
   private float yaw;
   private boolean hasYaw;
   private float pitch;
   private boolean hasPitch;
   private float roll;
   private boolean hasRoll;
   private double relativeTurnSpeed;
   private boolean hasRelativeTurnSpeed;

   @Nonnull
   public Steering clear() {
      this.clearTranslation();
      this.clearRotation();
      return this;
   }

   @Nonnull
   public Steering assign(@Nonnull Steering other) {
      this.translation.assign(other.translation);
      this.maxDistance = other.maxDistance;
      this.maxDistanceComponentSelector = other.maxDistanceComponentSelector;
      this.hasTranslation = other.hasTranslation;
      this.yaw = other.yaw;
      this.hasYaw = other.hasYaw;
      this.pitch = other.pitch;
      this.hasPitch = other.hasPitch;
      this.relativeTurnSpeed = other.relativeTurnSpeed;
      this.hasRelativeTurnSpeed = other.hasRelativeTurnSpeed;
      return this;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Steering steering = (Steering)o;
         if (Double.compare(steering.maxDistance, this.maxDistance) != 0) {
            return false;
         } else if (this.hasTranslation != steering.hasTranslation) {
            return false;
         } else if (Float.compare(steering.yaw, this.yaw) != 0) {
            return false;
         } else if (this.hasYaw != steering.hasYaw) {
            return false;
         } else if (Float.compare(steering.pitch, this.pitch) != 0) {
            return false;
         } else if (this.hasPitch != steering.hasPitch) {
            return false;
         } else if (Float.compare(steering.roll, this.roll) != 0) {
            return false;
         } else if (this.hasRoll != steering.hasRoll) {
            return false;
         } else if (!this.translation.equals(steering.translation)) {
            return false;
         } else if (Double.compare(steering.relativeTurnSpeed, this.relativeTurnSpeed) != 0) {
            return false;
         } else {
            return this.hasRelativeTurnSpeed != steering.hasRelativeTurnSpeed
               ? false
               : this.maxDistanceComponentSelector.equals(steering.maxDistanceComponentSelector);
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.translation.hashCode();
      long temp = Double.doubleToLongBits(this.maxDistance);
      result = 31 * result + (int)(temp ^ temp >>> 32);
      result = 31 * result + this.maxDistanceComponentSelector.hashCode();
      result = 31 * result + (this.hasTranslation ? 1 : 0);
      result = 31 * result + (this.yaw != 0.0F ? Float.floatToIntBits(this.yaw) : 0);
      result = 31 * result + (this.hasYaw ? 1 : 0);
      result = 31 * result + (this.pitch != 0.0F ? Float.floatToIntBits(this.pitch) : 0);
      result = 31 * result + (this.hasPitch ? 1 : 0);
      result = 31 * result + (this.roll != 0.0F ? Float.floatToIntBits(this.roll) : 0);
      return 31 * result + (this.hasRoll ? 1 : 0);
   }

   @Nonnull
   public Steering clearTranslation() {
      this.translation.assign(Vector3d.ZERO);
      this.maxDistance = Double.MAX_VALUE;
      this.hasTranslation = false;
      return this;
   }

   @Nonnull
   public Steering clearRotation() {
      this.yaw = 0.0F;
      this.hasYaw = false;
      this.pitch = 0.0F;
      this.hasPitch = false;
      this.relativeTurnSpeed = 0.0;
      this.hasRelativeTurnSpeed = false;
      return this;
   }

   @Nonnull
   public Vector3d getTranslation() {
      return this.translation;
   }

   public double getX() {
      return this.translation.x;
   }

   @Nonnull
   public Steering setX(double value) {
      this.hasTranslation = true;
      this.translation.x = value;
      return this;
   }

   public double getY() {
      return this.translation.y;
   }

   @Nonnull
   public Steering setY(double value) {
      this.hasTranslation = true;
      this.translation.y = value;
      return this;
   }

   public double getZ() {
      return this.translation.z;
   }

   @Nonnull
   public Steering setZ(double value) {
      this.hasTranslation = true;
      this.translation.z = value;
      return this;
   }

   @Nonnull
   public Steering setTranslation(@Nonnull Vector3d translation) {
      this.hasTranslation = true;
      this.translation.assign(translation);
      return this;
   }

   @Nonnull
   public Steering setTranslation(double x, double y, double z) {
      this.hasTranslation = true;
      this.translation.assign(x, y, z);
      return this;
   }

   @Nonnull
   public Steering setTranslationRelativeSpeed(double relativeSpeed) {
      this.translation.setLength(relativeSpeed);
      return this;
   }

   @Nonnull
   public Steering scaleTranslation(double speedFactor) {
      this.translation.scale(speedFactor);
      return this;
   }

   @Nonnull
   public Steering ensureMinTranslation(double relativeSpeed) {
      if (this.translation.squaredLength() < relativeSpeed * relativeSpeed && this.translation.squaredLength() > 0.0) {
         this.translation.setLength(relativeSpeed);
      }

      return this;
   }

   public double getMaxDistance() {
      return this.maxDistance;
   }

   public void setMaxDistance(double maxDistance) {
      this.maxDistance = maxDistance;
   }

   public void clearMaxDistance() {
      this.setMaxDistance(Double.MAX_VALUE);
   }

   public Vector3d getMaxDistanceComponentSelector() {
      return this.maxDistanceComponentSelector;
   }

   public void setMaxDistanceComponentSelector(Vector3d maxDistanceComponentSelector) {
      this.maxDistanceComponentSelector = maxDistanceComponentSelector;
   }

   public void clearMaxDistanceComponentSelector() {
      this.setMaxDistanceComponentSelector(null);
   }

   public float getYaw() {
      return this.yaw;
   }

   @Nonnull
   public Steering setYaw(float angle) {
      this.yaw = angle;
      this.hasYaw = true;
      return this;
   }

   public void clearYaw() {
      this.hasYaw = false;
   }

   public float getPitch() {
      return this.pitch;
   }

   @Nonnull
   public Steering setPitch(float angle) {
      this.pitch = angle;
      this.hasPitch = true;
      return this;
   }

   public void clearPitch() {
      this.hasPitch = false;
   }

   public float getRoll() {
      return this.roll;
   }

   @Nonnull
   public Steering setRoll(float angle) {
      this.roll = angle;
      this.hasRoll = true;
      return this;
   }

   public void clearRoll() {
      this.hasRoll = false;
   }

   @Nonnull
   public Steering setRelativeTurnSpeed(double relativeTurnSpeed) {
      this.relativeTurnSpeed = relativeTurnSpeed;
      this.hasRelativeTurnSpeed = true;
      return this;
   }

   public boolean hasTranslation() {
      return this.hasTranslation;
   }

   public boolean hasYaw() {
      return this.hasYaw;
   }

   public boolean hasPitch() {
      return this.hasPitch;
   }

   public boolean hasRoll() {
      return this.hasRoll;
   }

   public double getSpeed() {
      return !this.hasTranslation() ? 0.0 : this.translation.length();
   }

   public double getRelativeTurnSpeed() {
      return !this.hasRelativeTurnSpeed ? 1.0 : this.relativeTurnSpeed;
   }

   @Nonnull
   @Override
   public String toString() {
      return "Steering{translation="
         + this.translation
         + ", hasTranslation="
         + this.hasTranslation
         + ", yaw="
         + this.yaw
         + ", hasYaw="
         + this.hasYaw
         + ", pitch="
         + this.pitch
         + ", hasPitch="
         + this.hasPitch
         + ", roll="
         + this.roll
         + ", hasRoll="
         + this.hasRoll
         + ", relativeTurnSpeed="
         + this.relativeTurnSpeed
         + ", hasRelativeTurnSpeed="
         + this.hasRelativeTurnSpeed
         + "}";
   }
}
