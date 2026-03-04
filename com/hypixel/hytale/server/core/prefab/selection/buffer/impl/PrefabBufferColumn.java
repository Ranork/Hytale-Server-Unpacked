package com.hypixel.hytale.server.core.prefab.selection.buffer.impl;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import javax.annotation.Nullable;

public class PrefabBufferColumn {
   private final int readerIndex;
   private final Holder<EntityStore>[] entityHolders;
   private final Int2ObjectMap<Holder<ChunkStore>> blockComponents;

   public PrefabBufferColumn(int readerIndex, Holder<EntityStore>[] entityHolders, Int2ObjectMap<Holder<ChunkStore>> blockComponents) {
      this.readerIndex = readerIndex;
      this.entityHolders = entityHolders;
      this.blockComponents = blockComponents;
   }

   public int getReaderIndex() {
      return this.readerIndex;
   }

   @Nullable
   public Holder<EntityStore>[] getEntityHolders() {
      return this.entityHolders;
   }

   public Int2ObjectMap<Holder<ChunkStore>> getBlockComponents() {
      return this.blockComponents;
   }
}
