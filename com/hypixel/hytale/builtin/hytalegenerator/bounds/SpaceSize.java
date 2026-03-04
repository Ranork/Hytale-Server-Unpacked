package com.hypixel.hytale.builtin.hytalegenerator.bounds;

import com.hypixel.hytale.builtin.hytalegenerator.VectorUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;

@Deprecated
public class SpaceSize {
   @Nonnull
   private final Vector3i minInclusive;
   @Nonnull
   private final Vector3i maxExclusive;
   @Nonnull
   private final Vector3i maxInclusive;

   public SpaceSize(@Nonnull Vector3i minInclusive, @Nonnull Vector3i maxExclusive) {
      this.minInclusive = minInclusive.clone();
      this.maxExclusive = maxExclusive.clone();
      this.maxInclusive = maxExclusive.clone().add(-1, -1, -1);
   }

   public SpaceSize(@Nonnull Vector3i voxel) {
      this(voxel.clone(), voxel.clone().add(1, 1, 1));
   }

   public SpaceSize() {
      this(new Vector3i(), new Vector3i());
   }

   @Nonnull
   public SpaceSize moveBy(@Nonnull Vector3i delta) {
      this.minInclusive.add(delta);
      this.maxExclusive.add(delta);
      this.maxInclusive.add(delta);
      return this;
   }

   @Nonnull
   public Vector3i getMinInclusive() {
      return this.minInclusive.clone();
   }

   @Nonnull
   public Vector3i getMaxExclusive() {
      return this.maxExclusive.clone();
   }

   @Nonnull
   public Vector3i getMaxInclusive() {
      return this.maxInclusive.clone();
   }

   @Nonnull
   public Vector3i getRange() {
      Vector3i absMin = VectorUtil.fromOperation(value -> Math.abs(value.from(this.minInclusive)));
      Vector3i absMax = VectorUtil.fromOperation(value -> Math.abs(value.from(this.maxInclusive)));
      return Vector3i.max(absMin, absMax);
   }

   @Nonnull
   public Bounds3i toBounds3i() {
      return new Bounds3i(this.minInclusive, this.maxExclusive);
   }

   @Nonnull
   public SpaceSize clone() {
      return new SpaceSize(this.minInclusive, this.maxExclusive);
   }

   @Nonnull
   public static SpaceSize merge(@Nonnull SpaceSize a, @Nonnull SpaceSize b) {
      return new SpaceSize(Vector3i.min(a.minInclusive, b.minInclusive), Vector3i.max(a.maxExclusive, b.maxExclusive));
   }

   @Nonnull
   public static SpaceSize stack(@Nonnull SpaceSize a, @Nonnull SpaceSize b) {
      SpaceSize aMovedToMin = a.clone().moveBy(b.minInclusive);
      SpaceSize aMovedToMax = a.clone().moveBy(b.maxInclusive);
      Vector3i stackedMin = Vector3i.min(aMovedToMin.minInclusive, b.minInclusive);
      Vector3i stackedMax = Vector3i.max(aMovedToMax.maxExclusive, b.maxExclusive);
      return new SpaceSize(stackedMin, stackedMax);
   }

   @Nonnull
   public static SpaceSize empty() {
      return new SpaceSize(new Vector3i(), new Vector3i());
   }
}
