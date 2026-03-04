package com.hypixel.hytale.server.worldgen.cache;

import com.hypixel.hytale.server.worldgen.container.UniquePrefabContainer;
import com.hypixel.hytale.server.worldgen.util.cache.SizedTimeoutCache;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UniquePrefabCache {
   @Nonnull
   protected final SizedTimeoutCache<Integer, UniquePrefabContainer.UniquePrefabEntry[]> cache;

   public UniquePrefabCache(@Nonnull UniquePrefabCache.UniquePrefabFunction function, int maxSize, long expireAfterSeconds) {
      this.cache = new SizedTimeoutCache<>(expireAfterSeconds, TimeUnit.SECONDS, maxSize, function::get, null);
   }

   @Nullable
   public UniquePrefabContainer.UniquePrefabEntry[] get(int seed) {
      try {
         return this.cache.get(seed);
      } catch (Exception var3) {
         throw new Error("Failed to receive UniquePrefabEntry for " + seed, var3);
      }
   }

   @FunctionalInterface
   public interface UniquePrefabFunction {
      UniquePrefabContainer.UniquePrefabEntry[] get(int var1);
   }
}
