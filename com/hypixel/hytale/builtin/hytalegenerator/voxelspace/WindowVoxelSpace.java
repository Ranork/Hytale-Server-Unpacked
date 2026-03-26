package com.hypixel.hytale.builtin.hytalegenerator.voxelspace;

import com.hypixel.hytale.builtin.hytalegenerator.bounds.Bounds3i;
import com.hypixel.hytale.math.vector.Vector3i;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

public class WindowVoxelSpace<T> implements VoxelSpace<T> {
   @Nonnull
   private final VoxelSpace<T> source;
   @Nonnull
   private final Bounds3i bounds;

   public WindowVoxelSpace(@Nonnull VoxelSpace<T> voxelSpace) {
      this.source = voxelSpace;
      this.bounds = voxelSpace.getBounds().clone();
   }

   public void setBounds(@Nonnull Bounds3i bounds) {
      assert this.source.getBounds().contains(bounds);

      this.bounds.assign(bounds);
   }

   public void setBounds(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      this.bounds.min.assign(minX, minY, minZ);
      this.bounds.max.assign(maxX, maxY, maxZ);
   }

   @Nonnull
   public VoxelSpace<T> getSourceVoxelSpace() {
      return this.source;
   }

   @Override
   public void set(T content, int x, int y, int z) {
      if (this.bounds.contains(x, y, z)) {
         if (this.source.getBounds().contains(x, y, z)) {
            this.source.set(content, x, y, z);
         }
      }
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
      if (!this.getBounds().contains(x, y, z)) {
         throw new IllegalArgumentException("outside schematic");
      } else {
         return this.source.get(x, y, z);
      }
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
