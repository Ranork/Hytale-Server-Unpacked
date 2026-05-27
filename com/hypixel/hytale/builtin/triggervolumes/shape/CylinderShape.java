package com.hypixel.hytale.builtin.triggervolumes.shape;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class CylinderShape extends TriggerVolumeShape {
   @Nonnull
   public static final BuilderCodec<CylinderShape> CODEC = BuilderCodec.builder(CylinderShape.class, CylinderShape::new, BASE_CODEC)
      .append(new KeyedCodec<>("Center", Vector3dUtil.CODEC), (s, v) -> s.center = v, s -> s.center)
      .add()
      .append(new KeyedCodec<>("Radius", Codec.DOUBLE), (s, v) -> s.radius = v, s -> s.radius)
      .add()
      .append(new KeyedCodec<>("Height", Codec.DOUBLE), (s, v) -> s.height = v, s -> s.height)
      .add()
      .build();
   private Vector3d center = new Vector3d();
   private double radius;
   private double height;

   public CylinderShape() {
   }

   public CylinderShape(@Nonnull Vector3d center, double radius, double height) {
      this.center = center;
      this.radius = radius;
      this.height = height;
   }

   @Override
   public boolean contains(@Nonnull Vector3d origin, @Nonnull Vector3d testPoint) {
      double cx = origin.x() + this.center.x();
      double cz = origin.z() + this.center.z();
      double baseY = origin.y() + this.center.y();
      double dx = testPoint.x() - cx;
      double dz = testPoint.z() - cz;
      if (dx * dx + dz * dz > this.radius * this.radius) {
         return false;
      } else {
         double py = testPoint.y();
         return py >= baseY && py <= baseY + this.height;
      }
   }

   @Override
   public double getBoundingRadius() {
      double halfHeight = this.height * 0.5;
      return Math.sqrt(this.radius * this.radius + halfHeight * halfHeight);
   }

   @Override
   public double getMaxDistanceFromOrigin() {
      double halfHeight = this.height * 0.5;
      double gcY = this.center.y() + halfHeight;
      double distToCenter = Math.sqrt(this.center.x() * this.center.x() + gcY * gcY + this.center.z() * this.center.z());
      return distToCenter + Math.sqrt(this.radius * this.radius + halfHeight * halfHeight);
   }

   @Override
   public void getWorldAABB(@Nonnull Vector3d origin, @Nonnull Vector3d outMin, @Nonnull Vector3d outMax) {
      double cx = origin.x() + this.center.x();
      double cy = origin.y() + this.center.y();
      double cz = origin.z() + this.center.z();
      outMin.set(cx - this.radius, cy, cz - this.radius);
      outMax.set(cx + this.radius, cy + this.height, cz + this.radius);
   }

   @Override
   public void rotateInPlace(float yawRadians) {
      if (!(Math.abs(yawRadians) < 1.0E-6F)) {
         double x = this.center.x();
         double z = this.center.z();
         double cos = Math.cos(yawRadians);
         double sin = Math.sin(yawRadians);
         this.center.set(x * cos - z * sin, this.center.y(), x * sin + z * cos);
      }
   }

   @Nonnull
   public CylinderShape copy() {
      return new CylinderShape(new Vector3d(this.center), this.radius, this.height);
   }

   @Nonnull
   public Vector3d getCenter() {
      return this.center;
   }

   public double getRadius() {
      return this.radius;
   }

   public double getHeight() {
      return this.height;
   }
}
