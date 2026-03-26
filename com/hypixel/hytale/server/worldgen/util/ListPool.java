package com.hypixel.hytale.server.worldgen.util;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.annotation.Nonnull;

public class ListPool<T> {
   private final int capacity;
   private final T[] empty;
   private final ConcurrentLinkedQueue<ListPool.Resource<T>> pool = new ConcurrentLinkedQueue<>();

   public ListPool(int capacity, T[] empty) {
      this.capacity = capacity;
      this.empty = empty;

      for (int i = 0; i < capacity; i++) {
         this.pool.add(new ListPool.Resource<>(this));
      }
   }

   public T[] emptyArray() {
      return this.empty;
   }

   @Nonnull
   public ListPool.Resource<T> acquire() {
      ListPool.Resource<T> resource = this.pool.poll();
      return resource == null ? new ListPool.Resource<>(this) : resource;
   }

   @Nonnull
   public ListPool.Resource<T> acquire(int capacity) {
      ListPool.Resource<T> resource = this.pool.poll();
      if (resource == null) {
         resource = new ListPool.Resource<>(this);
      }

      resource.ensureCapacity(capacity);
      return resource;
   }

   public void release(@Nonnull ListPool.Resource<T> resource) {
      if (this.pool.size() < this.capacity) {
         resource.clear();
         this.pool.offer(resource);
      }
   }

   public static class Resource<T> extends ObjectArrayList<T> implements AutoCloseable {
      private final ListPool<T> pool;

      public Resource(ListPool<T> pool) {
         this.pool = pool;
      }

      @Nonnull
      public T[] toArray() {
         return (T[])super.toArray(this.pool.empty);
      }

      @Override
      public void close() {
         this.pool.release(this);
      }
   }
}
