package com.hypixel.hytale.builtin.adventure.memories.memories;

import com.hypixel.hytale.builtin.adventure.memories.MemoriesPlugin;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import java.util.Map;
import java.util.Set;

public abstract class MemoryProvider<T extends Memory> {
   private final String id;
   private final BuilderCodec<T> codec;
   private final double defaultRadius;

   public MemoryProvider(String id, BuilderCodec<T> codec, double defaultRadius) {
      this.id = id;
      this.codec = codec;
      this.defaultRadius = defaultRadius;
   }

   public String getId() {
      return this.id;
   }

   public BuilderCodec<T> getCodec() {
      return this.codec;
   }

   public double getCollectionRadius() {
      return MemoriesPlugin.get().getConfig().getCollectionRadius().getOrDefault(this.id, this.defaultRadius);
   }

   public abstract Map<String, Set<Memory>> getAllMemories();
}
