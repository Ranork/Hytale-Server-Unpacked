package com.hypixel.hytale.math.vector;

import com.hypixel.hytale.math.Axis;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.util.TrigMathUtil;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Location {
   @Nullable
   protected String world;
   @Nonnull
   protected Vector3d position;
   @Nonnull
   protected Vector3f rotation;

   public Location() {
      this(null, new Vector3d(), new Vector3f(Float.NaN, Float.NaN, Float.NaN));
   }

   public Location(@Nonnull Vector3i position) {
      this(null, new Vector3d(position), new Vector3f(Float.NaN, Float.NaN, Float.NaN));
   }

   public Location(@Nullable String world, @Nonnull Vector3i position) {
      this(world, new Vector3d(position), new Vector3f(Float.NaN, Float.NaN, Float.NaN));
   }

   public Location(@Nonnull Vector3d position) {
      this(null, new Vector3d(position), new Vector3f(Float.NaN, Float.NaN, Float.NaN));
   }

   public Location(@Nullable String world, @Nonnull Vector3d position) {
      this(world, new Vector3d(position), new Vector3f(Float.NaN, Float.NaN, Float.NaN));
   }

   public Location(double x, double y, double z) {
      this(null, new Vector3d(x, y, z), new Vector3f(Float.NaN, Float.NaN, Float.NaN));
   }

   public Location(@Nullable String world, double x, double y, double z) {
      this(world, new Vector3d(x, y, z), new Vector3f(Float.NaN, Float.NaN, Float.NaN));
   }

   public Location(double x, double y, double z, float pitch, float yaw, float roll) {
      this(null, new Vector3d(x, y, z), new Vector3f(pitch, yaw, roll));
   }

   public Location(@Nullable String world, double x, double y, double z, float pitch, float yaw, float roll) {
      this(world, new Vector3d(x, y, z), new Vector3f(pitch, yaw, roll));
   }

   public Location(@Nonnull Vector3d position, @Nonnull Vector3f rotation) {
      this(null, position, rotation);
   }

   public Location(@Nonnull Transform transform) {
      this(null, transform.position, transform.rotation);
   }

   public Location(@Nullable String world, @Nonnull Transform transform) {
      this(world, transform.position, transform.rotation);
   }

   public Location(@Nullable String world, @Nonnull Vector3d position, @Nonnull Vector3f rotation) {
      this.world = world;
      this.position = position;
      this.rotation = rotation;
   }

   @Nullable
   public String getWorld() {
      return this.world;
   }

   public void setWorld(@Nullable String world) {
      this.world = world;
   }

   @Nonnull
   public Vector3d getPosition() {
      return this.position;
   }

   public void setPosition(@Nonnull Vector3d position) {
      this.position = position;
   }

   @Nonnull
   public Vector3f getRotation() {
      return this.rotation;
   }

   public void setRotation(@Nonnull Vector3f rotation) {
      this.rotation = rotation;
   }

   @Nonnull
   public Vector3d getDirection() {
      return Transform.getDirection(this.rotation.getPitch(), this.rotation.getYaw());
   }

   @Nonnull
   public Vector3i getAxisDirection() {
      return this.getAxisDirection(this.rotation.getPitch(), this.rotation.getYaw());
   }

   @Nonnull
   public Vector3i getAxisDirection(float pitch, float yaw) {
      if (Float.isNaN(pitch)) {
         throw new IllegalStateException("Pitch can't be NaN");
      } else if (Float.isNaN(yaw)) {
         throw new IllegalStateException("Yaw can't be NaN");
      } else {
         float len = TrigMathUtil.cos(pitch);
         float x = len * -TrigMathUtil.sin(yaw);
         float y = TrigMathUtil.sin(pitch);
         float z = len * -TrigMathUtil.cos(yaw);
         return new Vector3i(MathUtil.fastRound(x), MathUtil.fastRound(y), MathUtil.fastRound(z));
      }
   }

   @Nonnull
   public Axis getAxis() {
      Vector3i axisDirection = this.getAxisDirection();
      if (axisDirection.getX() != 0) {
         return Axis.X;
      } else {
         return axisDirection.getY() != 0 ? Axis.Y : Axis.Z;
      }
   }

   @Nonnull
   public Transform toTransform() {
      return new Transform(this.position, this.rotation);
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Location location = (Location)o;
         if (!Objects.equals(this.world, location.world)) {
            return false;
         } else {
            return !Objects.equals(this.position, location.position) ? false : Objects.equals(this.rotation, location.rotation);
         }
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.world != null ? this.world.hashCode() : 0;
      result = 31 * result + this.position.hashCode();
      return 31 * result + this.rotation.hashCode();
   }

   @Nonnull
   @Override
   public String toString() {
      return "Location{world='" + this.world + "', position=" + this.position + ", rotation=" + this.rotation + "}";
   }
}
