package com.hypixel.hytale.server.core.universe.world.worldgen;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class GeneratedBlockStateChunk {
   private final Int2ObjectMap<Holder<ChunkStore>> mapping = new Int2ObjectOpenHashMap();

   public Holder<ChunkStore> getState(int x, int y, int z) {
      return (Holder<ChunkStore>)this.mapping.get(ChunkUtil.indexBlockInColumn(x, y, z));
   }

   public void setState(int x, int y, int z, @Nullable Holder<ChunkStore> state) {
      int index = ChunkUtil.indexBlockInColumn(x, y, z);
      if (state == null) {
         this.mapping.remove(index);
      } else {
         this.mapping.put(index, state);
      }
   }

   @Nonnull
   public BlockComponentChunk toBlockComponentChunk() {
      return new BlockComponentChunk(this.mapping, new Int2ReferenceOpenHashMap());
   }
}
