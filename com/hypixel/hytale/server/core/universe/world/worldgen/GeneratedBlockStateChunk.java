package com.hypixel.hytale.server.core.universe.world.worldgen;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
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
         BlockState blockState = BlockState.getBlockState(state);
         if (blockState != null) {
            blockState.setPosition(new Vector3i(x, y, z));
         }

         this.mapping.put(index, state);
      }
   }

   @Nonnull
   public BlockComponentChunk toBlockComponentChunk() {
      return new BlockComponentChunk(this.mapping, new Int2ObjectOpenHashMap());
   }
}
