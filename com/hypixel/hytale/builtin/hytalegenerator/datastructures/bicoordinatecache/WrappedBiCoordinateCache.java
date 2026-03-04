package com.hypixel.hytale.builtin.hytalegenerator.datastructures.bicoordinatecache;

import java.util.Arrays;
import javax.annotation.Nonnull;

public class WrappedBiCoordinateCache<T> implements BiCoordinateCache<T> {
   private final int sizeX;
   private final int sizeZ;
   @Nonnull
   private final T[][] values;
   @Nonnull
   private final boolean[][] populated;
   private int size;

   public WrappedBiCoordinateCache(int sizeX, int sizeZ) {
      if (sizeX >= 0 && sizeZ >= 0) {
         this.sizeX = sizeX;
         this.sizeZ = sizeZ;
         this.values = (T[][])(new Object[sizeX][sizeZ]);
         this.populated = new boolean[sizeX][sizeZ];
         this.size = 0;
      } else {
         throw new IllegalArgumentException("negative size");
      }
   }

   public int localXFrom(int x) {
      return x < 0 ? (x % this.sizeX + this.sizeX - 1) % this.sizeX : x % this.sizeX;
   }

   public int localZFrom(int z) {
      return z < 0 ? (z % this.sizeZ + this.sizeZ - 1) % this.sizeZ : z % this.sizeZ;
   }

   @Override
   public T get(int x, int z) {
      x = this.localXFrom(x);
      z = this.localZFrom(z);
      if (!this.isCached(x, z)) {
         throw new IllegalStateException("accessing coordinates that are not cached: " + x + " " + z);
      } else {
         return this.values[x][z];
      }
   }

   @Override
   public boolean isCached(int x, int z) {
      return this.populated[this.localXFrom(x)][this.localZFrom(z)];
   }

   @Override
   public T save(int x, int z, T value) {
      x = this.localXFrom(x);
      z = this.localZFrom(z);
      this.values[x][z] = value;
      this.populated[x][z] = true;
      this.size++;
      return value;
   }

   @Override
   public void flush(int x, int z) {
      x = this.localXFrom(x);
      z = this.localZFrom(z);
      if (this.populated[x][z]) {
         this.values[x][z] = null;
         this.populated[x][z] = false;
         this.size--;
      }
   }

   @Override
   public void flush() {
      for (int x = 0; x < this.sizeX; x++) {
         for (int z = 0; z < this.sizeZ; z++) {
            this.values[x][z] = null;
            this.populated[x][z] = false;
         }
      }

      this.size = 0;
   }

   @Override
   public int size() {
      return this.size;
   }

   @Nonnull
   @Override
   public String toString() {
      return "WrappedBiCoordinateCache{sizeX="
         + this.sizeX
         + ", sizeZ="
         + this.sizeZ
         + ", values="
         + Arrays.toString((Object[])this.values)
         + ", populated="
         + Arrays.toString((Object[])this.populated)
         + ", size="
         + this.size
         + "}";
   }
}
