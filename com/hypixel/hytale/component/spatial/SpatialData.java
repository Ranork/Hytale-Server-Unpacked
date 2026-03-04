package com.hypixel.hytale.component.spatial;

import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.math.vector.Vector3d;
import it.unimi.dsi.fastutil.ints.IntArrays;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SpatialData<T> {
   public static final Vector3d[] EMPTY_VECTOR_ARRAY = new Vector3d[0];
   @Nonnull
   private int[] indexes = ArrayUtil.EMPTY_INT_ARRAY;
   @Nonnull
   private long[] moroton = ArrayUtil.EMPTY_LONG_ARRAY;
   private Vector3d[] vectors = EMPTY_VECTOR_ARRAY;
   @Nonnull
   private T[] data = (T[])ArrayUtil.emptyArray();
   private int size;

   public int size() {
      return this.size;
   }

   public int getSortedIndex(int i) {
      return this.indexes[i];
   }

   @Nonnull
   public Vector3d getVector(int i) {
      return this.vectors[i];
   }

   @Nonnull
   public T getData(int i) {
      return this.data[i];
   }

   public void add(@Nonnull Vector3d vector, @Nonnull T value) {
      Objects.requireNonNull(value);
      if (this.vectors.length < this.size + 1) {
         int newLength = ArrayUtil.grow(this.size);
         this.indexes = Arrays.copyOf(this.indexes, newLength);
         this.vectors = Arrays.copyOf(this.vectors, newLength);
         this.data = (T[])Arrays.copyOf(this.data, newLength);

         for (int i = this.size; i < newLength; i++) {
            this.vectors[i] = new Vector3d();
         }
      }

      int index = this.size++;
      this.indexes[index] = index;
      this.vectors[index].assign(vector);
      this.data[index] = value;
   }

   public void addCapacity(int additionalSize) {
      int newSize = this.size + additionalSize;
      if (this.vectors.length < newSize) {
         int newLength = ArrayUtil.grow(newSize);
         this.indexes = Arrays.copyOf(this.indexes, newLength);
         this.vectors = Arrays.copyOf(this.vectors, newLength);
         this.data = (T[])Arrays.copyOf(this.data, newLength);

         for (int i = this.size; i < newLength; i++) {
            this.vectors[i] = new Vector3d();
         }
      }
   }

   public void append(@Nonnull Vector3d vector, @Nonnull T value) {
      Objects.requireNonNull(value);
      int index = this.size++;
      this.indexes[index] = index;
      this.vectors[index].assign(vector);
      this.data[index] = value;
   }

   public void sort() {
      IntArrays.quickSort(this.indexes, 0, this.size, (i1, i2) -> {
         Vector3d v1 = this.vectors[i1];
         Vector3d v2 = this.vectors[i2];
         int xComp = Double.compare(v1.x, v2.x);
         if (xComp != 0) {
            return xComp;
         } else {
            int zComp = Double.compare(v1.z, v2.z);
            return zComp != 0 ? zComp : Double.compare(v1.y, v2.y);
         }
      });
   }

   public void sortMorton() {
      double minX = Double.POSITIVE_INFINITY;
      double minY = Double.POSITIVE_INFINITY;
      double minZ = Double.POSITIVE_INFINITY;
      double maxX = Double.NEGATIVE_INFINITY;
      double maxY = Double.NEGATIVE_INFINITY;
      double maxZ = Double.NEGATIVE_INFINITY;

      for (int i = 0; i < this.size; i++) {
         Vector3d v = this.vectors[i];
         if (v.x < minX) {
            minX = v.x;
         }

         if (v.y < minY) {
            minY = v.y;
         }

         if (v.z < minZ) {
            minZ = v.z;
         }

         if (v.x > maxX) {
            maxX = v.x;
         }

         if (v.y > maxY) {
            maxY = v.y;
         }

         if (v.z > maxZ) {
            maxZ = v.z;
         }
      }

      this.moroton = this.moroton.length < this.size ? Arrays.copyOf(this.moroton, this.size) : this.moroton;

      for (int i = 0; i < this.size; i++) {
         Vector3d vx = this.vectors[i];
         this.moroton[i] = Long.reverse(MortonCode.encode(vx.x, vx.y, vx.z, minX, minY, minZ, maxX, maxY, maxZ));
      }

      IntArrays.quickSort(this.indexes, 0, this.size, (i1, i2) -> Long.compare(this.moroton[i1], this.moroton[i2]));
   }

   public void clear() {
      Arrays.fill(this.data, 0, this.size, null);
      this.size = 0;
   }
}
