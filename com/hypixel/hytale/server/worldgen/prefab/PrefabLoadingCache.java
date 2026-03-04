package com.hypixel.hytale.server.worldgen.prefab;

import com.hypixel.hytale.server.core.prefab.selection.buffer.PrefabBufferUtil;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.IPrefabBuffer;
import com.hypixel.hytale.server.core.prefab.selection.buffer.impl.PrefabBuffer;
import com.hypixel.hytale.server.worldgen.loader.WorldGenPrefabSupplier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import javax.annotation.Nonnull;

public class PrefabLoadingCache {
   private final Map<WorldGenPrefabSupplier, PrefabBuffer> cache = new ConcurrentHashMap<>();
   private final Function<WorldGenPrefabSupplier, PrefabBuffer> loader = p -> PrefabBufferUtil.loadBuffer(p.getPath());

   @Nonnull
   public IPrefabBuffer getPrefabAccessor(WorldGenPrefabSupplier prefabSupplier) {
      return this.cache.computeIfAbsent(prefabSupplier, this.loader).newAccess();
   }

   public void clear() {
      this.cache.values().removeIf(buffer -> {
         buffer.release();
         return true;
      });
   }

   @Nonnull
   @Override
   public String toString() {
      return "PrefabLoadingCache{cache=" + this.cache + "}";
   }
}
