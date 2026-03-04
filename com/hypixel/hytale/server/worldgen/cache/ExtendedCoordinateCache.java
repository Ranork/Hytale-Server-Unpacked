package com.hypixel.hytale.server.worldgen.cache;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.server.worldgen.util.ObjectPool;
import com.hypixel.hytale.server.worldgen.util.cache.SizedTimeoutCache;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class ExtendedCoordinateCache<K, T> {
   @Nonnull
   private final SizedTimeoutCache<ExtendedCoordinateCache.ExtendedCoordinateKey<K>, T> cache;
   @Nonnull
   private final ExtendedCoordinateCache.ExtendedCoordinateObjectFunction<K, T> loader;
   @Nonnull
   private final ObjectPool<ExtendedCoordinateCache.ExtendedCoordinateKey<K>> vectorPool;

   public ExtendedCoordinateCache(
      @Nonnull ExtendedCoordinateCache.ExtendedCoordinateObjectFunction<K, T> loader,
      @Nullable ExtendedCoordinateCache.ExtendedCoordinateRemovalListener<T> removalListener,
      int maxSize,
      long expireAfterSeconds
   ) {
      this.loader = loader;
      this.vectorPool = new ObjectPool<>(maxSize, ExtendedCoordinateCache.ExtendedCoordinateKey::new);
      this.cache = new SizedTimeoutCache<>(expireAfterSeconds, TimeUnit.SECONDS, maxSize, key -> {
         int x = ChunkUtil.xOfChunkIndex(key.coord);
         int z = ChunkUtil.zOfChunkIndex(key.coord);
         return loader.compute(key.k, key.seed, x, z);
      }, (key, value) -> {
         this.vectorPool.recycle(key);
         if (removalListener != null) {
            removalListener.onRemoval(value);
         }
      });
   }

   @Nullable
   public T get(@Nonnull K k, int seed, int x, int y) {
      return this.cache.getWithReusedKey(this.localKey().setLocation(k, seed, x, y), this.vectorPool);
   }

   protected abstract ExtendedCoordinateCache.ExtendedCoordinateKey<K> localKey();

   public static class ExtendedCoordinateKey<K>
      implements Function<ExtendedCoordinateCache.ExtendedCoordinateKey<K>, ExtendedCoordinateCache.ExtendedCoordinateKey<K>> {
      @Nullable
      private K k;
      private int seed;
      private long coord;
      private int hash;

      public ExtendedCoordinateKey() {
         this(null, 0, 0, 0);
      }

      public ExtendedCoordinateKey(@Nullable K k, int seed, int x, int y) {
         this.k = k;
         this.seed = seed;
         this.coord = ChunkUtil.indexChunk(x, y);
         this.hash = 31 * (k != null ? k.hashCode() : 0) + (int)HashUtil.hash(seed, this.coord);
      }

      @Nonnull
      public ExtendedCoordinateCache.ExtendedCoordinateKey<K> setLocation(@Nonnull K k, int seed, int x, int y) {
         this.k = k;
         this.seed = seed;
         this.coord = ChunkUtil.indexChunk(x, y);
         this.hash = 31 * k.hashCode() + (int)HashUtil.hash(seed, this.coord);
         return this;
      }

      @Nonnull
      public ExtendedCoordinateCache.ExtendedCoordinateKey<K> apply(@Nonnull ExtendedCoordinateCache.ExtendedCoordinateKey<K> cachedKey) {
         this.k = cachedKey.k;
         this.seed = cachedKey.seed;
         this.coord = cachedKey.coord;
         this.hash = cachedKey.hash;
         return this;
      }

      @Override
      public int hashCode() {
         return this.hash;
      }

      @Override
      public boolean equals(Object o) {
         ExtendedCoordinateCache.ExtendedCoordinateKey<?> that = (ExtendedCoordinateCache.ExtendedCoordinateKey<?>)o;
         return this.seed == that.seed && this.coord == that.coord && this.k.equals(that.k);
      }
   }

   @FunctionalInterface
   public interface ExtendedCoordinateObjectFunction<K, T> {
      T compute(K var1, int var2, int var3, int var4);
   }

   @FunctionalInterface
   public interface ExtendedCoordinateRemovalListener<T> {
      void onRemoval(T var1);
   }
}
