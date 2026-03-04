package com.hypixel.hytale.builtin.hytalegenerator.datastructures.voxelspace;

import com.hypixel.hytale.math.vector.Vector3i;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class WindowVoxelSpace<T> implements VoxelSpace<T> {
   @Nonnull
   private final VoxelSpace<T> wrappedVoxelSpace;
   @Nonnull
   private final VoxelCoordinate min;
   @Nonnull
   private final VoxelCoordinate max;

   public WindowVoxelSpace(@Nonnull VoxelSpace<T> voxelSpace) {
      this.wrappedVoxelSpace = voxelSpace;
      this.min = new VoxelCoordinate(voxelSpace.minX(), voxelSpace.minY(), voxelSpace.minZ());
      this.max = new VoxelCoordinate(voxelSpace.maxX(), voxelSpace.maxY(), voxelSpace.maxZ());
   }

   @Nonnull
   public WindowVoxelSpace<T> setWindow(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
      if (minX >= this.wrappedVoxelSpace.minX()
         && minY >= this.wrappedVoxelSpace.minY()
         && minZ >= this.wrappedVoxelSpace.minZ()
         && maxX >= minX
         && maxY >= minY
         && maxZ >= minZ
         && maxX <= this.wrappedVoxelSpace.maxX()
         && maxY <= this.wrappedVoxelSpace.maxY()
         && maxZ <= this.wrappedVoxelSpace.maxZ()) {
         this.min.x = minX;
         this.min.y = minY;
         this.min.z = minZ;
         this.max.x = maxX;
         this.max.y = maxY;
         this.max.z = maxZ;
         return this;
      } else {
         throw new IllegalArgumentException("invalid values");
      }
   }

   @Nonnull
   public VoxelSpace<T> getWrappedSchematic() {
      return this.wrappedVoxelSpace;
   }

   @Override
   public boolean set(T content, int x, int y, int z) {
      if (!this.isInsideSpace(x, y, z)) {
         return false;
      } else {
         return !this.wrappedVoxelSpace.isInsideSpace(x, y, z) ? false : this.wrappedVoxelSpace.set(content, x, y, z);
      }
   }

   @Override
   public boolean set(T content, @Nonnull Vector3i position) {
      return this.set(content, position.x, position.y, position.z);
   }

   @Override
   public void set(T content) {
      for (int x = this.minX(); x < this.maxX(); x++) {
         for (int y = this.minY(); y < this.maxY(); y++) {
            for (int z = this.minZ(); z < this.maxZ(); z++) {
               this.set(content, x, y, z);
            }
         }
      }
   }

   @Override
   public void setOrigin(int x, int y, int z) {
      throw new UnsupportedOperationException("can't set origin of window");
   }

   @Override
   public T getContent(int x, int y, int z) {
      if (!this.isInsideSpace(x, y, z)) {
         throw new IllegalArgumentException("outside schematic");
      } else {
         return this.wrappedVoxelSpace.getContent(x, y, z);
      }
   }

   @Nullable
   @Override
   public T getContent(@Nonnull Vector3i position) {
      return this.getContent(position.x, position.y, position.z);
   }

   @Override
   public boolean replace(T replacement, int x, int y, int z, @Nonnull Predicate<T> mask) {
      if (!this.isInsideSpace(x, y, z)) {
         throw new IllegalArgumentException("outside schematic");
      } else {
         return this.wrappedVoxelSpace.replace(replacement, x, y, z, mask);
      }
   }

   @Override
   public void pasteFrom(@Nonnull VoxelSpace<T> source) {
      for (int x = source.minX(); x < source.maxX(); x++) {
         for (int y = source.minY(); y < source.maxY(); y++) {
            for (int z = source.minZ(); z < source.maxZ(); z++) {
               this.set(source.getContent(x, y, z), x, y, z);
            }
         }
      }
   }

   @Override
   public int getOriginX() {
      int offset = this.min.x - this.wrappedVoxelSpace.minX();
      return this.wrappedVoxelSpace.getOriginX() - offset;
   }

   @Override
   public int getOriginY() {
      int offset = this.min.y - this.wrappedVoxelSpace.minY();
      return this.wrappedVoxelSpace.getOriginY() - offset;
   }

   @Override
   public int getOriginZ() {
      int offset = this.min.z - this.wrappedVoxelSpace.minZ();
      return this.wrappedVoxelSpace.getOriginZ() - offset;
   }

   @Nonnull
   @Override
   public String getName() {
      return "window_to_" + this.wrappedVoxelSpace.getName();
   }

   @Override
   public boolean isInsideSpace(int x, int y, int z) {
      return x >= this.minX() && x < this.maxX() && y >= this.minY() && y < this.maxY() && z >= this.minZ() && z < this.maxZ();
   }

   @Override
   public boolean isInsideSpace(@Nonnull Vector3i position) {
      return this.isInsideSpace(position.x, position.y, position.z);
   }

   @Override
   public void forEach(@Nonnull VoxelConsumer<? super T> action) {
      for (int x = this.minX(); x < this.maxX(); x++) {
         for (int y = this.minY(); y < this.maxY(); y++) {
            for (int z = this.minZ(); z < this.maxZ(); z++) {
               action.accept(this.getContent(x, y, z), x, y, z);
            }
         }
      }
   }

   @Override
   public int minX() {
      return this.min.x;
   }

   @Override
   public int maxX() {
      return this.max.x;
   }

   @Override
   public int minY() {
      return this.min.y;
   }

   @Override
   public int maxY() {
      return this.max.y;
   }

   @Override
   public int minZ() {
      return this.min.z;
   }

   @Override
   public int maxZ() {
      return this.max.z;
   }

   @Override
   public int sizeX() {
      return this.max.x - this.min.x;
   }

   @Override
   public int sizeY() {
      return this.max.y - this.min.y;
   }

   @Override
   public int sizeZ() {
      return this.max.z - this.min.z;
   }

   @Nonnull
   @Override
   public String toString() {
      return "WindowVoxelSpace{wrappedVoxelSpace=" + this.wrappedVoxelSpace + ", min=" + this.min + ", max=" + this.max + "}";
   }
}
