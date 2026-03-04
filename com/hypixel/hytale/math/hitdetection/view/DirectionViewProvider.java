package com.hypixel.hytale.math.hitdetection.view;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.hitdetection.MatrixProvider;
import com.hypixel.hytale.math.matrix.Matrix4d;
import com.hypixel.hytale.math.vector.Vector3d;
import javax.annotation.Nonnull;

public class DirectionViewProvider implements MatrixProvider {
   public static final BuilderCodec<DirectionViewProvider> CODEC = BuilderCodec.builder(DirectionViewProvider.class, DirectionViewProvider::new)
      .append(
         new KeyedCodec<>("YawOffset", Codec.DOUBLE),
         (projectionProvider, d) -> projectionProvider.yawOffset = d,
         projectionProvider -> projectionProvider.yawOffset
      )
      .add()
      .append(
         new KeyedCodec<>("PitchOffset", Codec.DOUBLE),
         (projectionProvider, d) -> projectionProvider.pitchOffset = d,
         projectionProvider -> projectionProvider.pitchOffset
      )
      .add()
      .append(
         new KeyedCodec<>("Up", Vector3d.CODEC), (projectionProvider, vec) -> projectionProvider.up.assign(vec), projectionProvider -> projectionProvider.up
      )
      .add()
      .build();
   public static final Vector3d DEFAULT_UP = new Vector3d(0.0, 1.0, 0.0);
   protected final Matrix4d matrix;
   protected final Vector3d position;
   protected final Vector3d direction;
   protected final Vector3d up;
   protected double yaw;
   protected double pitch;
   protected double yawOffset;
   protected double pitchOffset;
   protected boolean invalid;

   public DirectionViewProvider() {
      this(new Matrix4d(), new Vector3d(), new Vector3d(), new Vector3d(DEFAULT_UP));
   }

   public DirectionViewProvider(Matrix4d matrix, Vector3d position, Vector3d direction, Vector3d up) {
      this.matrix = matrix;
      this.position = position;
      this.direction = direction;
      this.up = up;
      this.invalid = true;
   }

   public Vector3d getPosition() {
      return this.position;
   }

   @Nonnull
   public DirectionViewProvider setPosition(@Nonnull Vector3d vec) {
      return this.setPosition(vec, 0.0, 0.0, 0.0);
   }

   @Nonnull
   public DirectionViewProvider setPosition(@Nonnull Vector3d vec, double offsetX, double offsetY, double offsetZ) {
      return this.setPosition(vec.x, vec.y, vec.z, offsetX, offsetY, offsetZ);
   }

   @Nonnull
   public DirectionViewProvider setPosition(double x, double y, double z) {
      this.position.assign(x, y, z);
      this.invalid = true;
      return this;
   }

   @Nonnull
   public DirectionViewProvider setPosition(double x, double y, double z, double offsetX, double offsetY, double offsetZ) {
      return this.setPosition(x + offsetX, y + offsetY, z + offsetZ);
   }

   public Vector3d getDirection() {
      return this.direction;
   }

   @Nonnull
   public DirectionViewProvider setDirection(@Nonnull Vector3d vec) {
      return this.setDirection(vec.x, vec.y, vec.z);
   }

   @Nonnull
   public DirectionViewProvider setDirection(double yaw, double pitch) {
      yaw += this.yawOffset;
      pitch += this.pitchOffset;
      this.direction.assign(yaw, pitch);
      this.invalid = true;
      return this;
   }

   @Nonnull
   public DirectionViewProvider setDirection(double x, double y, double z) {
      this.direction.assign(x, y, z);
      this.invalid = true;
      return this;
   }

   @Nonnull
   public DirectionViewProvider setUp(double x, double y, double z) {
      this.up.assign(x, y, z);
      this.invalid = true;
      return this;
   }

   @Override
   public Matrix4d getMatrix() {
      if (this.invalid) {
         this.matrix
            .viewDirection(
               this.position.x, this.position.y, this.position.z, this.direction.x, this.direction.y, this.direction.z, this.up.x, this.up.y, this.up.z
            );
         this.invalid = false;
      }

      return this.matrix;
   }

   @Nonnull
   @Override
   public String toString() {
      return "DirectionViewProvider{up="
         + this.up
         + ", yaw="
         + this.yaw
         + ", pitch="
         + this.pitch
         + ", yawOffset="
         + this.yawOffset
         + ", pitchOffset="
         + this.pitchOffset
         + "}";
   }
}
