package com.hypixel.hytale.server.core.prefab.selection.buffer.impl;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.lang.foreign.MemorySegment;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PrefabBufferColumn {
   private final int blockCount;
   @Nonnull
   private final MemorySegment memorySegment;
   private final Holder<EntityStore>[] entityHolders;
   private final Int2ObjectMap<Holder<ChunkStore>> blockComponents;

   public PrefabBufferColumn(
      int blockCount, @Nonnull MemorySegment memorySegment, Holder<EntityStore>[] entityHolders, Int2ObjectMap<Holder<ChunkStore>> blockComponents
   ) {
      this.blockCount = blockCount;
      this.memorySegment = memorySegment;
      this.entityHolders = entityHolders;
      this.blockComponents = blockComponents;
   }

   public int getBlockCount() {
      return this.blockCount;
   }

   @Nonnull
   public MemorySegment getMemorySegment() {
      return this.memorySegment;
   }

   @Nullable
   public Holder<EntityStore>[] getEntityHolders() {
      return this.entityHolders;
   }

   public Int2ObjectMap<Holder<ChunkStore>> getBlockComponents() {
      return this.blockComponents;
   }
}
