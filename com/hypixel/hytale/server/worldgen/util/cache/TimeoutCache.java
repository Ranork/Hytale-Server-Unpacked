package com.hypixel.hytale.server.worldgen.util.cache;

import com.hypixel.hytale.server.core.HytaleServer;
import java.lang.ref.WeakReference;
import java.lang.ref.Cleaner.Cleanable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TimeoutCache<K, V> implements Cache<K, V> {
   private final Map<K, TimeoutCache.CacheEntry<V>> map = new ConcurrentHashMap<>();
   private final long timeout;
   @Nonnull
   private final Function<K, V> func;
   @Nullable
   private final BiConsumer<K, V> destroyer;
   @Nonnull
   private final ScheduledFuture<?> future;
   @Nonnull
   private final Cleanable cleanable;

   public TimeoutCache(long expire, @Nonnull TimeUnit unit, @Nonnull Function<K, V> func, @Nullable BiConsumer<K, V> destroyer) {
      this.timeout = unit.toNanos(expire);
      this.func = func;
      this.destroyer = destroyer;
      this.future = HytaleServer.SCHEDULED_EXECUTOR.scheduleWithFixedDelay(new CleanupRunnable(new WeakReference<>(this)), expire, expire, unit);
      this.cleanable = CleanupFutureAction.CLEANER.register(this, new CleanupFutureAction(this.future));
   }

   @Override
   public void cleanup() {
      long expire = System.nanoTime() - this.timeout;

      for (Entry<K, TimeoutCache.CacheEntry<V>> entry : this.map.entrySet()) {
         TimeoutCache.CacheEntry<V> cacheEntry = entry.getValue();
         if (cacheEntry.timestamp < expire) {
            K key = entry.getKey();
            if (this.map.remove(key, entry.getValue()) && this.destroyer != null) {
               this.destroyer.accept(key, cacheEntry.value);
            }
         }
      }
   }

   @Override
   public void shutdown() {
      this.cleanable.clean();
      Iterator<Entry<K, TimeoutCache.CacheEntry<V>>> iterator = this.map.entrySet().iterator();

      while (iterator.hasNext()) {
         Entry<K, TimeoutCache.CacheEntry<V>> entry = iterator.next();
         K key = entry.getKey();
         TimeoutCache.CacheEntry<V> cacheEntry = entry.getValue();
         if (this.map.remove(key, cacheEntry)) {
            iterator.remove();
            if (this.destroyer != null) {
               this.destroyer.accept(key, cacheEntry.value);
            }
         }
      }
   }

   @Override
   public V get(K key) {
      if (this.future.isCancelled()) {
         throw new IllegalStateException("Cache has been shutdown!");
      } else {
         TimeoutCache.CacheEntry<V> cacheEntry = this.map.compute(key, (k, v) -> {
            if (v != null) {
               v.timestamp = System.nanoTime();
               return v;
            } else {
               return new TimeoutCache.CacheEntry<>(this.func.apply((K)k));
            }
         });
         return cacheEntry.value;
      }
   }

   private static class CacheEntry<V> {
      private final V value;
      private long timestamp;

      public CacheEntry(V value) {
         this.value = value;
         this.timestamp = System.nanoTime();
      }
   }
}
