package com.hypixel.hytale.builtin.hytalegenerator.bounds;

import com.hypixel.hytale.builtin.hytalegenerator.engine.performanceinstruments.MemInstrument;
import com.hypixel.hytale.math.vector.Vector3dUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import javax.annotation.Nonnull;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3i;

public class Bounds3d implements MemInstrument {
   @Nonnull
   public static final Bounds3d ZERO = new Bounds3d();
   @Nonnull
   public final Vector3d min;
   @Nonnull
   public final Vector3d max;

   public Bounds3d() {
      this(Vector3dUtil.ZERO, Vector3dUtil.ZERO);
   }

   public Bounds3d(@Nonnull Vector3dc min, @Nonnull Vector3dc max) {
      this.min = new Vector3d(min);
      this.max = new Vector3d(max);
      this.correct();
   }

   public boolean contains(int x, int y, int z) {
      return x >= this.min.x && y >= this.min.y && z >= this.min.z && x < this.max.x && y < this.max.y && z < this.max.z;
   }

   public boolean contains(double x, double y, double z) {
      return x >= this.min.x && y >= this.min.y && z >= this.min.z && x < this.max.x && y < this.max.y && z < this.max.z;
   }

   public boolean contains(@Nonnull Vector3i position) {
      return position.x >= this.min.x
         && position.y >= this.min.y
         && position.z >= this.min.z
         && position.x < this.max.x
         && position.y < this.max.y
         && position.z < this.max.z;
   }

   public boolean contains(@Nonnull Vector3d position) {
      return position.x >= this.min.x
         && position.y >= this.min.y
         && position.z >= this.min.z
         && position.x < this.max.x
         && position.y < this.max.y
         && position.z < this.max.z;
   }

   public boolean contains(@Nonnull Bounds3d other) {
      return other.min.x >= this.min.x
         && other.min.y >= this.min.y
         && other.min.z >= this.min.z
         && other.max.x <= this.max.x
         && other.max.y <= this.max.y
         && other.max.z <= this.max.z;
   }

   public boolean intersects(@Nonnull Bounds3d other) {
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
   public Vector3d getSize() {
      return new Vector3d(this.max).sub(this.min);
   }

   @Nonnull
   public Bounds3d assign(@Nonnull Bounds3d other) {
      this.min.set(other.min);
      this.max.set(other.max);
      this.correct();
      return this;
   }

   @Nonnull
   public Bounds3d assign(@Nonnull Bounds3i other) {
      this.min.set(other.min);
      this.max.set(other.max);
      this.correct();
      return this;
   }

   @Nonnull
   public Bounds3d assign(@Nonnull Vector3d min, @Nonnull Vector3d max) {
      this.min.set(min);
      this.max.set(max);
      this.correct();
      return this;
   }

   @Nonnull
   public Bounds3d offset(@Nonnull Vector3d vector) {
      this.min.add(vector);
      this.max.add(vector);
      return this;
   }

   @Nonnull
   public Bounds3d offsetOpposite(@Nonnull Vector3d vector) {
      this.min.sub(vector);
      this.max.sub(vector);
      return this;
   }

   @Nonnull
   public Bounds3d intersect(@Nonnull Bounds3d other) {
      if (!this.intersects(other)) {
         this.min.zero();
         this.max.zero();
      }

      this.min.set(Math.max(this.min.x, other.min.x), Math.max(this.min.y, other.min.y), Math.max(this.min.z, other.min.z));
      this.max.set(Math.min(this.max.x, other.max.x), Math.min(this.max.y, other.max.y), Math.min(this.max.z, other.max.z));
      return this;
   }

   @Nonnull
   public Bounds3d encompass(@Nonnull Bounds3d other) {
      if (other.isZeroVolume()) {
         return this;
      } else if (this.isZeroVolume()) {
         this.min.set(other.min);
         this.max.set(other.max);
         return this;
      } else {
         this.min.set(Math.min(this.min.x, other.min.x), Math.min(this.min.y, other.min.y), Math.min(this.min.z, other.min.z));
         this.max.set(Math.max(this.max.x, other.max.x), Math.max(this.max.y, other.max.y), Math.max(this.max.z, other.max.z));
         return this;
      }
   }

   @Nonnull
   public Bounds3d encompass(@Nonnull Vector3d position) {
      this.min.set(Math.min(this.min.x, position.x), Math.min(this.min.y, position.y), Math.min(this.min.z, position.z));
      this.max.set(Math.max(this.max.x, position.x), Math.max(this.max.y, position.y), Math.max(this.max.z, position.z));
      return this;
   }

   @Nonnull
   public Bounds3d stack(@Nonnull Bounds3d other) {
      if (!this.isZeroVolume() && !other.isZeroVolume()) {
         Vector3d initialMax = new Vector3d(this.max);
         Bounds3d stamp = other.clone();
         stamp.offset(this.min);
         this.encompass(stamp);
         stamp = other.clone();
         stamp.offset(initialMax);
         this.encompass(stamp);
         return this;
      } else {
         return this;
      }
   }

   @Nonnull
   public Bounds3d flipOnOriginPoint() {
      if (this.isZeroVolume()) {
         return this;
      } else {
         Vector3d swap = new Vector3d(this.min);
         this.min.set(this.max);
         this.min.mul(-1.0);
         this.max.set(swap);
         this.max.mul(-1.0);
         return this;
      }
   }

   @Nonnull
   public Bounds3d flipOnOriginVoxel() {
      if (this.isZeroVolume()) {
         return this;
      } else {
         Vector3d swap = new Vector3d(this.min);
         this.min.set(Vector3dUtil.ALL_ONES);
         this.min.sub(this.max);
         this.max.set(Vector3dUtil.ALL_ONES);
         this.max.sub(swap);
         return this;
      }
   }

   public Bounds3d applyRotation(@Nonnull RotationTuple rotationTuple, @Nonnull Vector3d anchor) {
      if (this.isZeroVolume()) {
         return this;
      } else {
         this.min.sub(anchor);
         rotationTuple.applyRotationTo(this.min);
         this.min.add(anchor);
         this.max.sub(anchor);
         rotationTuple.applyRotationTo(this.max);
         this.max.add(anchor);
         this.correct();
         return this;
      }
   }

   public Bounds3d undoRotation(@Nonnull RotationTuple rotationTuple, @Nonnull Vector3d anchor) {
      if (this.isZeroVolume()) {
         return this;
      } else {
         this.min.sub(anchor);
         rotationTuple.undoRotationTo(this.min);
         this.min.add(anchor);
         this.max.sub(anchor);
         rotationTuple.undoRotationTo(this.max);
         this.max.add(anchor);
         this.correct();
         return this;
      }
   }

   @Nonnull
   public Bounds3d clone() {
      return new Bounds3d(new Vector3d(this.min), new Vector3d(this.max));
   }

   public boolean isCorrect() {
      return this.min.x <= this.max.x && this.min.y <= this.max.y && this.min.z <= this.max.z;
   }

   public void correct() {
      Vector3d swap = new Vector3d(this.min);
      this.min.set(Math.min(this.max.x, this.min.x), Math.min(this.max.y, this.min.y), Math.min(this.max.z, this.min.z));
      this.max.set(Math.max(swap.x, this.max.x), Math.max(swap.y, this.max.y), Math.max(swap.z, this.max.z));
   }

   @Nonnull
   @Override
   public MemInstrument.Report getMemoryUsage() {
      long size_byte = 40L;
      return new MemInstrument.Report(40L);
   }
}
