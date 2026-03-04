package com.hypixel.hytale.server.worldgen.cache;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.server.worldgen.util.ObjectPool;
import com.hypixel.hytale.server.worldgen.util.cache.SizedTimeoutCache;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class CoordinateCache<T> {
   @Nonnull
   private final SizedTimeoutCache<CoordinateCache.CoordinateKey, T> cache;
   @Nonnull
   private final ObjectPool<CoordinateCache.CoordinateKey> vectorPool;

   public CoordinateCache(int maxSize, long expireAfterSeconds) {
      this.vectorPool = new ObjectPool<>(maxSize, CoordinateCache.CoordinateKey::new);
      this.cache = new SizedTimeoutCache<>(expireAfterSeconds, TimeUnit.SECONDS, maxSize, key -> {
         int x = ChunkUtil.xOfChunkIndex(key.coord);
         int z = ChunkUtil.zOfChunkIndex(key.coord);
         return this.compute(key.seed, x, z);
      }, (key, value) -> {
         this.vectorPool.recycle(key);
         this.onRemoval(value);
      });
   }

   @Nullable
   public T get(int seed, int x, int y) {
      return this.cache.getWithReusedKey(this.localKey().setLocation(seed, x, y), this.vectorPool);
   }

   protected abstract CoordinateCache.CoordinateKey localKey();

   protected abstract T compute(int var1, int var2, int var3);

   protected abstract void onRemoval(T var1);

   public static class CoordinateKey implements Function<CoordinateCache.CoordinateKey, CoordinateCache.CoordinateKey> {
      private int seed;
      private long coord;
      private int hash;

      public CoordinateKey() {
         this(0, 0, 0);
      }

      public CoordinateKey(int seed, int x, int y) {
         this.setLocation(seed, x, y);
      }

      public int seed() {
         return this.seed;
      }

      public long coord() {
         return this.coord;
      }

      @Nonnull
      public CoordinateCache.CoordinateKey setLocation(int seed, int x, int y) {
         this.seed = seed;
         this.coord = ChunkUtil.indexChunk(x, y);
         this.hash = (int)HashUtil.hash(seed, this.coord);
         return this;
      }

      @Nonnull
      public CoordinateCache.CoordinateKey apply(@Nonnull CoordinateCache.CoordinateKey cachedKey) {
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
         CoordinateCache.CoordinateKey coordinateKey = (CoordinateCache.CoordinateKey)o;
         return this.seed == coordinateKey.seed && this.coord == coordinateKey.coord;
      }
   }
}
