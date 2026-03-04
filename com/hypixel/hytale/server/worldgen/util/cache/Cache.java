package com.hypixel.hytale.server.worldgen.util.cache;

import javax.annotation.Nullable;

public interface Cache<K, V> {
   void shutdown();

   void cleanup();

   @Nullable
   V get(K var1);
}
