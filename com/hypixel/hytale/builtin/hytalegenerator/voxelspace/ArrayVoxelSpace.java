package com.hypixel.hytale.builtin.hytalegenerator.voxelspace;

import com.hypixel.hytale.builtin.hytalegenerator.GridUtils;
import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class ArrayVoxelSpace<T> implements VoxelSpace<T> {
   @Nonnull
   private final Bounds3i bounds;
   @Nonnull
   private final T[] contents;

   public ArrayVoxelSpace(@Nonnull Bounds3i bounds) {
      this.bounds = bounds.clone();
      Vector3i size = bounds.getSize();
      int voxelCount = size.x * size.y * size.z;
      this.contents = (T[])(new Object[voxelCount]);
   }

   public void offset(@Nonnull Vector3i vector) {
      this.bounds.offset(vector);
   }

   public void offsetOpposite(@Nonnull Vector3i vector) {
      this.bounds.offsetOpposite(vector);
   }

   @Override
   public void set(T content, int x, int y, int z) {
      assert this.bounds.contains(x, y, z);

      int index = GridUtils.toIndexFromPositionYXZ(x, y, z, this.bounds);
      this.contents[index] = content;
   }

   @Override
   public void set(T content, @Nonnull Vector3i position) {
      this.set(content, position.x, position.y, position.z);
   }

   @Override
   public void setAll(T content) {
      for (int x = this.bounds.min.x; x < this.bounds.max.x; x++) {
         for (int y = this.bounds.min.y; y < this.bounds.max.y; y++) {
            for (int z = this.bounds.min.z; z < this.bounds.max.z; z++) {
               this.set(content, x, y, z);
            }
         }
      }
   }

   @Override
   public T get(int x, int y, int z) {
      assert this.bounds.contains(x, y, z);

      int index = GridUtils.toIndexFromPositionYXZ(x, y, z, this.bounds);
      return this.contents[index];
   }

   @Nullable
   @Override
   public T get(@Nonnull Vector3i position) {
      return this.get(position.x, position.y, position.z);
   }

   @NonNullDecl
   @Override
   public Bounds3i getBounds() {
      return this.bounds;
   }
}
