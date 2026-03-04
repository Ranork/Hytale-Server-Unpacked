package com.hypixel.hytale.math.hitdetection.projection;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.hitdetection.MatrixProvider;
import com.hypixel.hytale.math.matrix.Matrix4d;
import javax.annotation.Nonnull;

public class OrthogonalProjectionProvider implements MatrixProvider {
   public static final BuilderCodec<OrthogonalProjectionProvider> CODEC = BuilderCodec.builder(
         OrthogonalProjectionProvider.class, OrthogonalProjectionProvider::new
      )
      .addField(new KeyedCodec<>("Left", Codec.DOUBLE), (projectionProvider, d) -> projectionProvider.left = d, projectionProvider -> projectionProvider.left)
      .addField(
         new KeyedCodec<>("Right", Codec.DOUBLE), (projectionProvider, d) -> projectionProvider.right = d, projectionProvider -> projectionProvider.right
      )
      .addField(new KeyedCodec<>("Top", Codec.DOUBLE), (projectionProvider, d) -> projectionProvider.top = d, projectionProvider -> projectionProvider.top)
      .addField(
         new KeyedCodec<>("Bottom", Codec.DOUBLE), (projectionProvider, d) -> projectionProvider.bottom = d, projectionProvider -> projectionProvider.bottom
      )
      .addField(new KeyedCodec<>("Near", Codec.DOUBLE), (projectionProvider, d) -> projectionProvider.near = d, projectionProvider -> projectionProvider.near)
      .addField(new KeyedCodec<>("Far", Codec.DOUBLE), (projectionProvider, d) -> projectionProvider.far = d, projectionProvider -> projectionProvider.far)
      .build();
   protected final Matrix4d matrix;
   protected final Matrix4d rotMatrix = new Matrix4d();
   protected boolean invalid;
   protected double left;
   protected double right;
   protected double bottom;
   protected double top;
   protected double near;
   protected double far;
   protected double yaw;
   protected double pitch;
   protected double roll;

   public OrthogonalProjectionProvider() {
      this(new Matrix4d(), 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0);
   }

   public OrthogonalProjectionProvider(
      Matrix4d matrix, double left, double right, double bottom, double top, double near, double far, double yaw, double pitch, double roll
   ) {
      this.matrix = matrix;
      this.left = left;
      this.right = right;
      this.bottom = bottom;
      this.top = top;
      this.near = near;
      this.far = far;
      this.yaw = yaw;
      this.pitch = pitch;
      this.roll = roll;
      this.invalid = true;
   }

   @Nonnull
   public OrthogonalProjectionProvider setLeft(double left) {
      this.left = left;
      this.invalid = true;
      return this;
   }

   @Nonnull
   public OrthogonalProjectionProvider setRight(double right) {
      this.right = right;
      this.invalid = true;
      return this;
   }

   @Nonnull
   public OrthogonalProjectionProvider setBottom(double bottom) {
      this.bottom = bottom;
      this.invalid = true;
      return this;
   }

   @Nonnull
   public OrthogonalProjectionProvider setTop(double top) {
      this.top = top;
      this.invalid = true;
      return this;
   }

   @Nonnull
   public OrthogonalProjectionProvider setNear(double near) {
      this.near = near;
      this.invalid = true;
      return this;
   }

   @Nonnull
   public OrthogonalProjectionProvider setFar(double far) {
      this.far = far;
      this.invalid = true;
      return this;
   }

   public double getRange() {
      return this.far;
   }

   @Nonnull
   public OrthogonalProjectionProvider setRotation(double yaw, double pitch, double roll) {
      this.yaw = yaw;
      this.pitch = pitch;
      this.roll = roll;
      return this;
   }

   @Override
   public Matrix4d getMatrix() {
      if (this.invalid) {
         this.matrix.projectionOrtho(this.left, this.right, this.bottom, this.top, this.near, this.far);
         this.matrix.rotateAxis(this.roll, 0.0, 0.0, 1.0, this.rotMatrix);
         this.matrix.rotateAxis(this.pitch, 1.0, 0.0, 0.0, this.rotMatrix);
         this.matrix.rotateAxis(this.yaw, 0.0, 1.0, 0.0, this.rotMatrix);
         this.invalid = false;
      }

      return this.matrix;
   }

   @Nonnull
   @Override
   public String toString() {
      return "OrthogonalProjectionProvider{left="
         + this.left
         + ", right="
         + this.right
         + ", bottom="
         + this.bottom
         + ", top="
         + this.top
         + ", near="
         + this.near
         + ", far="
         + this.far
         + "}";
   }
}
