package com.hypixel.hytale.builtin.triggervolumes.shape;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class SphereShape extends TriggerVolumeShape {
   @Nonnull
   public static final BuilderCodec<SphereShape> CODEC = BuilderCodec.builder(SphereShape.class, SphereShape::new, BASE_CODEC)
      .append(new KeyedCodec<>("Center", Vector3dUtil.CODEC), (s, v) -> s.center = v, s -> s.center)
      .add()
      .append(new KeyedCodec<>("Radius", Codec.DOUBLE), (s, v) -> s.radius = v, s -> s.radius)
      .add()
      .build();
   private Vector3d center = new Vector3d();
   private double radius;

   public SphereShape() {
   }

   public SphereShape(@Nonnull Vector3d center, double radius) {
      this.center = center;
      this.radius = radius;
   }

   @Override
   public boolean contains(@Nonnull Vector3d origin, @Nonnull Vector3d testPoint) {
      double dx = testPoint.x() - (origin.x() + this.center.x());
      double dy = testPoint.y() - (origin.y() + this.center.y());
      double dz = testPoint.z() - (origin.z() + this.center.z());
      return dx * dx + dy * dy + dz * dz <= this.radius * this.radius;
   }

   @Override
   public double getBoundingRadius() {
      return this.radius;
   }

   @Override
   public double getMaxDistanceFromOrigin() {
      return this.center.length() + this.radius;
   }

   @Override
   public void getWorldAABB(@Nonnull Vector3d origin, @Nonnull Vector3d outMin, @Nonnull Vector3d outMax) {
      double cx = origin.x() + this.center.x();
      double cy = origin.y() + this.center.y();
      double cz = origin.z() + this.center.z();
      outMin.set(cx - this.radius, cy - this.radius, cz - this.radius);
      outMax.set(cx + this.radius, cy + this.radius, cz + this.radius);
   }

   @Override
   public void rotateInPlace(float yawRadians) {
   }

   @Nonnull
   public SphereShape copy() {
      return new SphereShape(new Vector3d(this.center), this.radius);
   }

   @Nonnull
   public Vector3d getCenter() {
      return this.center;
   }

   public double getRadius() {
      return this.radius;
   }
}
