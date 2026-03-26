package com.hypixel.hytale.common.collection;

import java.util.Arrays;
import javax.annotation.Nonnull;

public class BucketItemPool<E> {
   @Nonnull
   protected BucketItem<E>[] pool = new BucketItem[16];
   protected int size;

   public void deallocate(BucketItem<E>[] entityHolders, int count) {
      int required = this.size + count;
      if (required > this.pool.length) {
         this.pool = Arrays.copyOf(this.pool, Math.max(required, this.pool.length << 1));
      }

      System.arraycopy(entityHolders, 0, this.pool, this.size, count);
      this.size += count;
   }

   public BucketItem<E> allocate(E reference, double squaredDistance) {
      if (this.size == 0) {
         return new BucketItem<E>().set(reference, squaredDistance);
      } else {
         BucketItem<E> holder = this.pool[--this.size];
         this.pool[this.size] = null;
         return holder.set(reference, squaredDistance);
      }
   }
}
