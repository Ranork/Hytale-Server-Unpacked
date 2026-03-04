package com.hypixel.hytale.server.core.universe.world.accessor;

import com.hypixel.hytale.math.util.ChunkUtil;

@Deprecated
public interface ChunkAccessor<WorldChunk extends BlockAccessor> extends IChunkAccessorSync<WorldChunk> {
   default int getFluidId(int x, int y, int z) {
      WorldChunk chunk = this.getChunk(ChunkUtil.indexChunkFromBlock(x, z));
      return chunk != null ? chunk.getFluidId(x, y, z) : 0;
   }

   default boolean performBlockUpdate(int x, int y, int z) {
      return this.performBlockUpdate(x, y, z, true);
   }

   default boolean performBlockUpdate(int x, int y, int z, boolean allowPartialLoad) {
      boolean success = true;

      for (int ix = -1; ix < 2; ix++) {
         int wx = x + ix;

         for (int iz = -1; iz < 2; iz++) {
            int wz = z + iz;
            WorldChunk worldChunk = allowPartialLoad
               ? this.getNonTickingChunk(ChunkUtil.indexChunkFromBlock(wx, wz))
               : this.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(wx, wz));
            if (worldChunk == null) {
               success = false;
            } else {
               for (int iy = -1; iy < 2; iy++) {
                  int wy = y + iy;
                  worldChunk.setTicking(wx, wy, wz, true);
               }
            }
         }
      }

      return success;
   }
}
