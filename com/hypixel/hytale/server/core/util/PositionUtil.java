package com.hypixel.hytale.server.core.util;

import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.Transform;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PositionUtil {
   @Nonnull
   public static Transform toTransformPacket(@Nonnull com.hypixel.hytale.math.vector.Transform transform) {
      Vector3d position = transform.getPosition();
      Vector3f rotation = transform.getRotation();
      return new Transform(toPositionPacket(position), toDirectionPacket(rotation));
   }

   @Nonnull
   public static Position toPositionPacket(@Nonnull Vector3d position) {
      return new Position(position.x, position.y, position.z);
   }

   @Nonnull
   public static Direction toDirectionPacket(@Nonnull Vector3f rotation) {
      return new Direction(rotation.getYaw(), rotation.getPitch(), rotation.getRoll());
   }

   public static com.hypixel.hytale.math.vector.Transform toTransform(@Nullable Transform transform) {
      return transform == null ? null : new com.hypixel.hytale.math.vector.Transform(toVector3d(transform.position), toRotation(transform.orientation));
   }

   @Nonnull
   public static Vector3d toVector3d(@Nonnull Position position_) {
      return new Vector3d(position_.x, position_.y, position_.z);
   }

   @Nonnull
   public static Vector3f toRotation(@Nonnull Direction orientation) {
      return new Vector3f(orientation.pitch, orientation.yaw, orientation.roll);
   }

   public static boolean equals(@Nonnull Vector3d vector, @Nonnull Position position) {
      return vector.x == position.x && vector.y == position.y && vector.z == position.z;
   }

   public static void assign(@Nonnull Position position, @Nonnull Vector3d vector) {
      position.x = vector.x;
      position.y = vector.y;
      position.z = vector.z;
   }

   public static boolean equals(@Nonnull Vector3f vector, @Nonnull Direction direction) {
      return vector.x == direction.pitch && vector.y == direction.yaw && vector.z == direction.roll;
   }

   public static void assign(@Nonnull Direction direction, @Nonnull Vector3f vector) {
      direction.pitch = vector.x;
      direction.yaw = vector.y;
      direction.roll = vector.z;
   }

   public static void assign(@Nonnull Position position, @Nonnull Position other) {
      position.x = other.x;
      position.y = other.y;
      position.z = other.z;
   }

   public static void assign(@Nonnull Direction direction, @Nonnull Direction other) {
      direction.pitch = other.pitch;
      direction.yaw = other.yaw;
      direction.roll = other.roll;
   }
}
