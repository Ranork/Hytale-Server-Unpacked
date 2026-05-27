package com.hypixel.hytale.builtin.triggervolumes.shape;

import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import javax.annotation.Nonnull;
import org.joml.Vector3d;

public class BoxShape extends TriggerVolumeShape {
   @Nonnull
   public static final BuilderCodec<BoxShape> CODEC = BuilderCodec.builder(BoxShape.class, BoxShape::new, BASE_CODEC)
      .append(new KeyedCodec<>("Min", Vector3dUtil.CODEC), (s, v) -> s.min = v, s -> s.min)
      .add()
      .append(new KeyedCodec<>("Max", Vector3dUtil.CODEC), (s, v) -> s.max = v, s -> s.max)
      .add()
      .afterDecode(BoxShape::computeDerived)
      .build();
   private Vector3d min;
   private Vector3d max;
   private transient Vector3d offset;
   private transient Vector3d halfExtents;

   public BoxShape() {
   }

   public BoxShape(@Nonnull Vector3d min, @Nonnull Vector3d max) {
      this.min = min;
      this.max = max;
      this.computeDerived();
   }

   private void computeDerived() {
      if (this.min != null && this.max != null) {
         this.offset = new Vector3d(this.min).add(this.max).mul(0.5);
         this.halfExtents = new Vector3d(this.max).sub(this.min).mul(0.5).absolute();
      }
   }

   @Override
   public boolean contains(@Nonnull Vector3d origin, @Nonnull Vector3d testPoint) {
      if (this.offset == null) {
         return false;
      } else {
         double cx = origin.x() + this.offset.x();
         double cy = origin.y() + this.offset.y();
         double cz = origin.z() + this.offset.z();
         return Math.abs(testPoint.x() - cx) <= this.halfExtents.x()
            && Math.abs(testPoint.y() - cy) <= this.halfExtents.y()
            && Math.abs(testPoint.z() - cz) <= this.halfExtents.z();
      }
   }

   @Override
   public double getBoundingRadius() {
      return this.halfExtents != null ? this.halfExtents.length() : 0.0;
   }

   @Override
   public double getMaxDistanceFromOrigin() {
      return this.offset != null && this.halfExtents != null ? this.offset.length() + this.halfExtents.length() : 0.0;
   }

   @Override
   public void getWorldAABB(@Nonnull Vector3d origin, @Nonnull Vector3d outMin, @Nonnull Vector3d outMax) {
      outMin.set(origin).add(this.min);
      outMax.set(origin).add(this.max);
   }

   @Override
   public void rotateInPlace(float yawRadians) {
      if (this.min != null && this.max != null && !(Math.abs(yawRadians) < 1.0E-6F)) {
         double cos = Math.cos(yawRadians);
         double sin = Math.sin(yawRadians);
         Vector3d newMin = new Vector3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
         Vector3d newMax = new Vector3d(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

         for (double x : new double[]{this.min.x(), this.max.x()}) {
            for (double y : new double[]{this.min.y(), this.max.y()}) {
               for (double z : new double[]{this.min.z(), this.max.z()}) {
                  double rotatedX = x * cos - z * sin;
                  double rotatedZ = x * sin + z * cos;
                  newMin.set(Math.min(newMin.x(), rotatedX), Math.min(newMin.y(), y), Math.min(newMin.z(), rotatedZ));
                  newMax.set(Math.max(newMax.x(), rotatedX), Math.max(newMax.y(), y), Math.max(newMax.z(), rotatedZ));
               }
            }
         }

         this.min.set(newMin);
         this.max.set(newMax);
         this.computeDerived();
      }
   }

   @Nonnull
   public BoxShape copy() {
      return new BoxShape(new Vector3d(this.min), new Vector3d(this.max));
   }

   @Nonnull
   public Vector3d getMin() {
      return this.min;
   }

   @Nonnull
   public Vector3d getMax() {
      return this.max;
   }
}
