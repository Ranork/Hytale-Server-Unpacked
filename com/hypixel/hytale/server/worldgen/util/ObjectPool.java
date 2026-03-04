package com.hypixel.hytale.server.worldgen.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nonnull;

public class ObjectPool<T extends Function<T, T>> implements Function<T, T> {
   @Nonnull
   private final BlockingQueue<T> items;
   private final Supplier<T> supplier;

   public ObjectPool(int size, Supplier<T> supplier) {
      this.items = new ArrayBlockingQueue<>(size);
      this.supplier = supplier;
   }

   public T acquire() {
      T v = this.items.poll();
      return v == null ? this.supplier.get() : v;
   }

   public <K extends T> void recycle(@Nonnull K v) {
      this.items.offer((T)v);
   }

   public int size() {
      return this.items.size();
   }

   public T apply(T cachedKey) {
      return this.acquire().apply(cachedKey);
   }
}
