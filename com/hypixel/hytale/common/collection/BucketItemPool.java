package com.hypixel.hytale.common.collection;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nonnull;

public class BucketItemPool<E> {
   @Nonnull
   protected final List<BucketItem<E>> pool = new ObjectArrayList();

   public void deallocate(BucketItem<E>[] entityHolders, int count) {
      this.pool.addAll(Arrays.asList(entityHolders).subList(0, count));
   }

   public BucketItem<E> allocate(E reference, double squaredDistance) {
      int l = this.pool.size();
      BucketItem<E> holder = l == 0 ? new BucketItem<>() : this.pool.remove(l - 1);
      return holder.set(reference, squaredDistance);
   }
}
