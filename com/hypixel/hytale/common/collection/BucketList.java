package com.hypixel.hytale.common.collection;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import java.util.Comparator;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BucketList<E> {
   public static final int INITIAL_BUCKET_ITEM_ARRAY_SIZE = 4;
   public static final Comparator<BucketItem<?>> CLOSER_TO_SELF = Comparator.comparingDouble(bucketItem -> bucketItem.squaredDistance);
   protected static final byte[] EMPTY_INDICES = new byte[]{-1};
   protected BucketItemPool<E> bucketItemPool;
   @Nullable
   protected BucketList.Bucket<E>[] buckets;
   protected byte[] bucketIndices = EMPTY_INDICES;
   protected int bucketCount;
   protected int squaredMaxDistance;

   public BucketList(BucketItemPool<E> bucketItemPool) {
      this.bucketItemPool = bucketItemPool;
   }

   public void setBucketItemPool(@Nonnull BucketItemPool<E> bucketItemPool) {
      this.clear();
      this.bucketItemPool = bucketItemPool;
   }

   public void clear() {
      if (this.buckets != null && this.bucketItemPool != null) {
         for (BucketList.Bucket<E> bucket : this.buckets) {
            bucket.clear(this.bucketItemPool);
         }
      }
   }

   public void reset() {
      this.clear();
      this.buckets = null;
      this.bucketCount = 0;
      this.bucketIndices = EMPTY_INDICES;
   }

   public void configure(@Nonnull int[] bucketRanges) {
      this.configure(bucketRanges, 4);
   }

   public void configure(@Nonnull int[] bucketRanges, int initialBucketItemArraySize) {
      if (bucketRanges == null) {
         throw new IllegalArgumentException("bucketRanges can't be null");
      } else if (bucketRanges.length <= 0) {
         throw new IllegalArgumentException("bucketRanges can't beempty");
      } else {
         int[] copyRanges = (int[])bucketRanges.clone();
         IntArrays.quickSort(copyRanges);
         if (copyRanges[0] <= 0) {
            throw new IllegalArgumentException("bucketRanges entries must be >0");
         } else {
            this.configureWithPreSortedArray(copyRanges, initialBucketItemArraySize);
         }
      }
   }

   public void configureWithPreSortedArray(@Nonnull int[] bucketRanges) {
      this.configureWithPreSortedArray(bucketRanges, 4);
   }

   public void configureWithPreSortedArray(@Nonnull int[] bucketRanges, int initialBucketItemArraySize) {
      this.clear();
      this.bucketCount = bucketRanges.length;
      this.squaredMaxDistance = bucketRanges[this.bucketCount - 1];
      this.squaredMaxDistance = this.squaredMaxDistance * this.squaredMaxDistance;
      this.buckets = new BucketList.Bucket[this.bucketCount];
      this.bucketIndices = new byte[this.squaredMaxDistance + 1];
      int inner = 0;

      for (int i = 0; i < this.bucketCount; i++) {
         int outer = bucketRanges[i] * bucketRanges[i];
         this.buckets[i] = new BucketList.Bucket<>(initialBucketItemArraySize);

         for (int j = inner; j < outer; j++) {
            this.bucketIndices[j] = (byte)i;
         }

         inner = outer;
      }

      this.bucketIndices[this.bucketIndices.length - 1] = -1;
   }

   public void configureWithPresortedArray(@Nonnull IntArrayList bucketRanges, int initialBucketItemArraySize) {
      this.configureWithPreSortedArray(bucketRanges.toIntArray(), initialBucketItemArraySize);
   }

   public boolean add(@Nonnull E item, double squaredDistance) {
      int bucketIndex = this.getFirstBucketIndex((int)squaredDistance);
      if (bucketIndex < 0) {
         return false;
      } else {
         BucketItem<E> bucketItem = this.bucketItemPool.allocate(item, squaredDistance);
         this.buckets[bucketIndex].add(bucketItem);
         return true;
      }
   }

   public int getBucketCount() {
      return this.buckets != null ? this.buckets.length : 0;
   }

   @Nullable
   public BucketList.Bucket<E> getBucket(int index) {
      return index >= 0 && index < this.getBucketCount() ? this.buckets[index] : null;
   }

   public int getFirstBucketIndex(int distanceSquared) {
      if (distanceSquared == 0) {
         return this.bucketIndices[0];
      } else {
         distanceSquared = Math.min(distanceSquared, this.squaredMaxDistance);
         return distanceSquared <= 0 ? -1 : this.bucketIndices[distanceSquared];
      }
   }

   public int getLastBucketIndex(int distanceSquared) {
      int d = Math.min(distanceSquared, this.squaredMaxDistance) - 1;
      return d < 0 ? -1 : this.bucketIndices[d];
   }

   @Nullable
   public E getClosestInRange(int minRange, int maxRange, @Nonnull Predicate<E> filter, @Nonnull BucketList.SortBufferProvider sortBufferProvider) {
      int minRangeSquared = minRange * minRange;
      int startBucket = this.getFirstBucketIndex(minRangeSquared);
      if (startBucket < 0) {
         return null;
      } else {
         int maxRangeSquared = maxRange * maxRange;
         int endBucket = this.getLastBucketIndex(maxRangeSquared);

         for (int i = startBucket; i <= endBucket; i++) {
            BucketList.Bucket<E> bucket = this.buckets[i];
            if (!bucket.isEmpty) {
               if (bucket.isUnsorted) {
                  bucket.sort(sortBufferProvider);
               }

               BucketItem<E>[] entityHolders = bucket.bucketItems;
               int i1 = 0;

               for (int entityHoldersSize = bucket.size; i1 < entityHoldersSize; i1++) {
                  BucketItem<E> holder = entityHolders[i1];
                  double squaredDistance = holder.squaredDistance;
                  if (!(squaredDistance < minRangeSquared)) {
                     if (squaredDistance >= maxRangeSquared) {
                        return null;
                     }

                     E item = holder.item;
                     if (item != null && filter.test(item)) {
                        return item;
                     }
                  }
               }
            }
         }

         return null;
      }
   }

   public static void addBucketDistance(@Nonnull IntArrayList bucketRanges, int maxBucketCount, int distance) {
      addBucketDistance(bucketRanges, maxBucketCount, distance, -1);
   }

   public static void addBucketDistance(@Nonnull IntArrayList bucketRanges, int maxBucketCount, int distance, int keepDistance) {
      if (distance >= 1) {
         int i = 0;

         int length;
         for (length = bucketRanges.size(); i < length; i++) {
            int v = bucketRanges.getInt(i);
            if (v == distance) {
               return;
            }

            if (v > distance) {
               break;
            }
         }

         bucketRanges.add(i, distance);
         if (++length > maxBucketCount) {
            int middle = bucketRanges.getInt(0);
            int innerArea = area(0, middle);
            int area = Integer.MAX_VALUE;
            int pos = -1;

            for (int var13 = 1; var13 < length; var13++) {
               int outer = bucketRanges.getInt(var13);
               int outerArea = area(middle, outer);
               int sumAreas = innerArea + outerArea;
               if (sumAreas <= area && middle != keepDistance) {
                  pos = var13 - 1;
                  area = sumAreas;
               }

               middle = outer;
               innerArea = outerArea;
            }

            bucketRanges.removeInt(pos);
         }
      }
   }

   protected static int area(int inner, int outer) {
      return outer * outer - inner * inner;
   }

   public static class Bucket<E> {
      protected BucketItem<E>[] bucketItems;
      protected int size;
      protected boolean isUnsorted;
      protected boolean isEmpty;

      public Bucket(int initialBucketArraySize) {
         this.bucketItems = new BucketItem[initialBucketArraySize];
         this.size = 0;
         this.isUnsorted = false;
         this.isEmpty = true;
      }

      public BucketItem<E>[] getItems() {
         return this.bucketItems;
      }

      public int size() {
         return this.size;
      }

      public boolean isUnsorted() {
         return this.isUnsorted;
      }

      public boolean isEmpty() {
         return this.isEmpty;
      }

      public void clear(@Nonnull BucketItemPool<E> pool) {
         if (!this.isEmpty) {
            pool.deallocate(this.bucketItems, this.size);

            for (int i = 0; i < this.size; i++) {
               this.bucketItems[i] = null;
            }

            this.size = 0;
            this.isUnsorted = false;
            this.isEmpty = true;
         }
      }

      public void add(@Nonnull BucketItem<E> item) {
         this.isEmpty = false;
         if (this.size == this.bucketItems.length) {
            this.bucketItems = (BucketItem<E>[])ObjectArrays.grow(this.bucketItems, this.size + 1);
         }

         this.bucketItems[this.size++] = item;
         this.isUnsorted = true;
      }

      public void sort(@Nonnull BucketList.SortBufferProvider sortBufferProvider) {
         this.isUnsorted = false;
         if (this.size > 1) {
            BucketItem[] sortBuffer = sortBufferProvider.apply(this.size);
            System.arraycopy(this.bucketItems, 0, sortBuffer, 0, this.size);
            ObjectArrays.mergeSort(this.bucketItems, 0, this.size, BucketList.CLOSER_TO_SELF, sortBuffer);
         }
      }
   }

   public static class SortBufferProvider implements IntFunction<BucketItem[]> {
      protected BucketItem[] buffer = new BucketItem[4];

      public BucketItem[] apply(int size) {
         if (size <= this.buffer.length) {
            return this.buffer;
         } else {
            this.buffer = (BucketItem[])ObjectArrays.grow(this.buffer, size);
            return this.buffer;
         }
      }
   }
}
