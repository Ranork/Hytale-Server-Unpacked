package com.hypixel.hytale.builtin.hytalegenerator.bounds;

import com.hypixel.hytale.builtin.hytalegenerator.newsystem.performanceinstruments.MemInstrument;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

public class Bounds3i implements MemInstrument {
   @Nonnull
   public final Vector3i min;
   @Nonnull
   public final Vector3i max;

   public Bounds3i() {
      this(Vector3i.ZERO, Vector3i.ZERO);
   }

   public Bounds3i(@Nonnull Vector3i min, @Nonnull Vector3i max) {
      this.min = min.clone();
      this.max = max.clone();
      this.correct();
   }

   public boolean contains(@Nonnull Vector3i position) {
      return position.x >= this.min.x
         && position.y >= this.min.y
         && position.z >= this.min.z
         && position.x < this.max.x
         && position.y < this.max.y
         && position.z < this.max.z;
   }

   public boolean contains(@Nonnull Bounds3i other) {
      return other.min.x >= this.min.x
         && other.min.y >= this.min.y
         && other.min.z >= this.min.z
         && other.max.x <= this.max.x
         && other.max.y <= this.max.y
         && other.max.z <= this.max.z;
   }

   public boolean intersects(@Nonnull Bounds3i other) {
      return this.min.x < other.max.x
         && this.min.y < other.max.y
         && this.min.z < other.max.z
         && this.max.x > other.min.x
         && this.max.y > other.min.y
         && this.max.z > other.min.z;
   }

   public boolean isZeroVolume() {
      return this.min.x >= this.max.x || this.min.y >= this.max.y || this.min.z >= this.max.z;
   }

   @Nonnull
   public Vector3i getSize() {
      return this.max.clone().subtract(this.min);
   }

   @Nonnull
   public Bounds3i assign(@Nonnull Bounds3i other) {
      this.min.assign(other.min);
      this.max.assign(other.max);
      this.correct();
      return this;
   }

   @Nonnull
   public Bounds3i assign(@Nonnull Vector3i min, @Nonnull Vector3i max) {
      this.min.assign(min);
      this.max.assign(max);
      this.correct();
      return this;
   }

   @Nonnull
   public Bounds3i offset(@Nonnull Vector3i vector) {
      this.min.add(vector);
      this.max.add(vector);
      return this;
   }

   @Nonnull
   public Bounds3i intersect(@Nonnull Bounds3i other) {
      if (!this.intersects(other)) {
         this.min.assign(Vector3i.ZERO);
         this.max.assign(Vector3i.ZERO);
      }

      this.min.assign(Math.max(this.min.x, other.min.x), Math.max(this.min.y, other.min.y), Math.max(this.min.z, other.min.z));
      this.max.assign(Math.min(this.max.x, other.max.x), Math.min(this.max.y, other.max.y), Math.min(this.max.z, other.max.z));
      return this;
   }

   @Nonnull
   public Bounds3i encompass(@Nonnull Bounds3i other) {
      if (other.isZeroVolume()) {
         return this;
      } else if (this.isZeroVolume()) {
         this.min.assign(other.min);
         this.max.assign(other.max);
         return this;
      } else {
         this.min.assign(Math.min(this.min.x, other.min.x), Math.min(this.min.y, other.min.y), Math.min(this.min.z, other.min.z));
         this.max.assign(Math.max(this.max.x, other.max.x), Math.max(this.max.y, other.max.y), Math.max(this.max.z, other.max.z));
         return this;
      }
   }

   @Nonnull
   public Bounds3i encompass(@Nonnull Vector3i position) {
      this.min.assign(Math.min(this.min.x, position.x), Math.min(this.min.y, position.y), Math.min(this.min.z, position.z));
      this.max.assign(Math.max(this.max.x, position.x), Math.max(this.max.y, position.y), Math.max(this.max.z, position.z));
      return this;
   }

   @Nonnull
   public Bounds3i stack(@Nonnull Bounds3i other) {
      if (!this.isZeroVolume() && !other.isZeroVolume()) {
         Vector3i initialMax = this.max.clone();
         Bounds3i stamp = other.clone();
         stamp.offset(this.min);
         this.encompass(stamp);
         stamp = other.clone();
         stamp.offset(initialMax.clone().subtract(Vector3i.ALL_ONES));
         this.encompass(stamp);
         return this;
      } else {
         return this;
      }
   }

   @Nonnull
   public Bounds3i flipOnOriginPoint() {
      Vector3i swap = this.min.clone();
      this.min.assign(this.max);
      this.min.scale(-1);
      this.max.assign(swap);
      this.max.scale(-1);
      return this;
   }

   @Nonnull
   public Bounds3i flipOnOriginVoxel() {
      Vector3i swap = this.min.clone();
      this.min.assign(Vector3i.ALL_ONES);
      this.min.subtract(this.max);
      this.max.assign(Vector3i.ALL_ONES);
      this.max.subtract(swap);
      return this;
   }

   @Nonnull
   public Bounds3d toBounds3d() {
      return new Bounds3d(this.min.toVector3d(), this.max.toVector3d());
   }

   @Nonnull
   public Bounds3i clone() {
      return new Bounds3i(this.min.clone(), this.max.clone());
   }

   public boolean isCorrect() {
      return this.min.x <= this.max.x && this.min.y <= this.max.y && this.min.z <= this.max.z;
   }

   public void correct() {
      Vector3i swap = this.min.clone();
      this.min.assign(Math.min(this.max.x, this.min.x), Math.min(this.max.y, this.min.y), Math.min(this.max.z, this.min.z));
      this.max.assign(Math.max(swap.x, this.max.x), Math.max(swap.y, this.max.y), Math.max(swap.z, this.max.z));
   }

   @Nonnull
   @Override
   public MemInstrument.Report getMemoryUsage() {
      long size_byte = 28L;
      return new MemInstrument.Report(28L);
   }
}
