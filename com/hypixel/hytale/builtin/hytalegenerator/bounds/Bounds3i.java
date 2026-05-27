package com.hypixel.hytale.builtin.hytalegenerator.bounds;

import com.hypixel.hytale.builtin.hytalegenerator.engine.performanceinstruments.MemInstrument;
import com.hypixel.hytale.math.vector.Vector3iUtil;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import javax.annotation.Nonnull;
import org.joml.Vector3d;
import org.joml.Vector3i;
import org.joml.Vector3ic;

public class Bounds3i implements MemInstrument {
   @Nonnull
   public static final Bounds3i ZERO = new Bounds3i();
   @Nonnull
   public final Vector3i min;
   @Nonnull
   public final Vector3i max;

   public Bounds3i() {
      this(Vector3iUtil.ZERO, Vector3iUtil.ZERO);
   }

   public Bounds3i(@Nonnull Vector3ic min, @Nonnull Vector3ic max) {
      this.min = new Vector3i(min);
      this.max = new Vector3i(max);
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
      return new Vector3i(this.max).sub(this.min);
   }

   @Nonnull
   public Bounds3i assign(@Nonnull Bounds3i other) {
      this.min.set(other.min);
      this.max.set(other.max);
      this.correct();
      return this;
   }

   @Nonnull
   public Bounds3i assign(@Nonnull Vector3i min, @Nonnull Vector3i max) {
      this.min.set(min);
      this.max.set(max);
      this.correct();
      return this;
   }

   @Nonnull
   public Bounds3i offset(int x, int y, int z) {
      this.min.add(x, y, z);
      this.max.add(x, y, z);
      return this;
   }

   @Nonnull
   public Bounds3i offset(@Nonnull Vector3i vector) {
      this.min.add(vector);
      this.max.add(vector);
      return this;
   }

   @Nonnull
   public Bounds3i offsetOpposite(@Nonnull Vector3i vector) {
      this.min.sub(vector);
      this.max.sub(vector);
      return this;
   }

   @Nonnull
   public Bounds3i intersect(@Nonnull Bounds3i other) {
      if (!this.intersects(other)) {
         this.min.set(Vector3iUtil.ZERO);
         this.max.set(Vector3iUtil.ZERO);
      }

      this.min.set(Math.max(this.min.x, other.min.x), Math.max(this.min.y, other.min.y), Math.max(this.min.z, other.min.z));
      this.max.set(Math.min(this.max.x, other.max.x), Math.min(this.max.y, other.max.y), Math.min(this.max.z, other.max.z));
      return this;
   }

   @Nonnull
   public Bounds3i encompass(@Nonnull Bounds3i other) {
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
   public Bounds3i encompass(@Nonnull Vector3ic position) {
      this.min.set(Math.min(this.min.x, position.x()), Math.min(this.min.y, position.y()), Math.min(this.min.z, position.z()));
      this.max.set(Math.max(this.max.x, position.x() + 1), Math.max(this.max.y, position.y() + 1), Math.max(this.max.z, position.z() + 1));
      return this;
   }

   @Nonnull
   public Bounds3i stack(@Nonnull Bounds3i other) {
      if (!this.isZeroVolume() && !other.isZeroVolume()) {
         Vector3i initialMax = new Vector3i(this.max);
         Bounds3i stamp = other.clone();
         stamp.offset(this.min);
         this.encompass(stamp);
         stamp = other.clone();
         stamp.offset(new Vector3i(initialMax).sub(Vector3iUtil.ALL_ONES));
         this.encompass(stamp);
         return this;
      } else {
         return this;
      }
   }

   @Nonnull
   public Bounds3i flipOnOriginPoint() {
      if (this.isZeroVolume()) {
         return this;
      } else {
         Vector3i swap = new Vector3i(this.min);
         this.min.set(this.max);
         this.min.mul(-1);
         this.max.set(swap);
         this.max.mul(-1);
         return this;
      }
   }

   @Nonnull
   public Bounds3i flipOnOriginVoxel() {
      if (this.isZeroVolume()) {
         return this;
      } else {
         Vector3i swap = new Vector3i(this.min);
         this.min.set(Vector3iUtil.ALL_ONES);
         this.min.sub(this.max);
         this.max.set(Vector3iUtil.ALL_ONES);
         this.max.sub(swap);
         return this;
      }
   }

   public Bounds3i applyRotationAroundVoxel(@Nonnull RotationTuple rotationTuple, @Nonnull Vector3ic anchor) {
      if (this.isZeroVolume()) {
         return this;
      } else {
         this.max.sub(Vector3iUtil.ALL_ONES);
         this.min.sub(anchor);
         rotationTuple.applyRotationTo(this.min);
         this.min.add(anchor);
         this.max.sub(anchor);
         rotationTuple.applyRotationTo(this.max);
         this.max.add(anchor);
         this.correct();
         this.max.add(Vector3iUtil.ALL_ONES);
         return this;
      }
   }

   public Bounds3i undoRotationAroundVoxel(@Nonnull RotationTuple rotationTuple, @Nonnull Vector3ic anchor) {
      if (this.isZeroVolume()) {
         return this;
      } else {
         this.max.sub(Vector3iUtil.ALL_ONES);
         this.min.sub(anchor);
         rotationTuple.undoRotationTo(this.min);
         this.min.add(anchor);
         this.max.sub(anchor);
         rotationTuple.undoRotationTo(this.max);
         this.max.add(anchor);
         this.correct();
         this.max.add(Vector3iUtil.ALL_ONES);
         return this;
      }
   }

   @Nonnull
   public Bounds3d toBounds3d() {
      return new Bounds3d(Vector3iUtil.toVector3d(this.min), Vector3iUtil.toVector3d(this.max));
   }

   @Nonnull
   public Bounds3i clone() {
      return new Bounds3i(new Vector3i(this.min), new Vector3i(this.max));
   }

   public boolean isCorrect() {
      return this.min.x <= this.max.x && this.min.y <= this.max.y && this.min.z <= this.max.z;
   }

   public void correct() {
      Vector3i swap = new Vector3i(this.min);
      this.min.set(Math.min(this.max.x, this.min.x), Math.min(this.max.y, this.min.y), Math.min(this.max.z, this.min.z));
      this.max.set(Math.max(swap.x, this.max.x), Math.max(swap.y, this.max.y), Math.max(swap.z, this.max.z));
   }

   @Nonnull
   @Override
   public MemInstrument.Report getMemoryUsage() {
      long size_byte = 28L;
      return new MemInstrument.Report(28L);
   }
}
