package com.hypixel.hytale.builtin.buildertools.scriptedbrushes;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.accessor.ChunkAccessor;
import com.hypixel.hytale.server.core.universe.world.accessor.LocalCachedChunkAccessor;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import javax.annotation.Nonnull;

public class BrushConfigChunkAccessor extends LocalCachedChunkAccessor {
   private final BrushConfigEditStore editOperation;

   @Nonnull
   public static BrushConfigChunkAccessor atWorldCoords(
      BrushConfigEditStore editOperation, ChunkAccessor<WorldChunk> delegate, int centerX, int centerZ, int blockRadius
   ) {
      int chunkRadius = ChunkUtil.chunkCoordinate(blockRadius) + 1;
      return atChunkCoords(editOperation, delegate, ChunkUtil.chunkCoordinate(centerX), ChunkUtil.chunkCoordinate(centerZ), chunkRadius);
   }

   @Nonnull
   public static BrushConfigChunkAccessor atChunkCoords(
      BrushConfigEditStore editOperation, ChunkAccessor<WorldChunk> delegate, int centerX, int centerZ, int chunkRadius
   ) {
      return new BrushConfigChunkAccessor(editOperation, delegate, centerX, centerZ, chunkRadius);
   }

   protected BrushConfigChunkAccessor(BrushConfigEditStore editOperation, ChunkAccessor<WorldChunk> delegate, int centerX, int centerZ, int radius) {
      super(delegate, centerX, centerZ, radius);
      this.editOperation = editOperation;
   }

   @Override
   public int getBlock(@Nonnull Vector3i pos) {
      return this.editOperation.getAfter().hasBlockAtWorldPos(pos.x, pos.y, pos.z)
         ? this.editOperation.getAfter().getBlockAtWorldPos(pos.x, pos.y, pos.z)
         : this.getBlockIgnoringHistory(pos);
   }

   @Override
   public int getBlock(int x, int y, int z) {
      return this.editOperation.getAfter().hasBlockAtWorldPos(x, y, z)
         ? this.editOperation.getAfter().getBlockAtWorldPos(x, y, z)
         : this.getBlockIgnoringHistory(x, y, z);
   }

   public int getBlockIgnoringHistory(@Nonnull Vector3i pos) {
      return this.getBlockIgnoringHistory(pos.x, pos.y, pos.z);
   }

   public int getBlockIgnoringHistory(int x, int y, int z) {
      return this.editOperation.getBefore().hasBlockAtWorldPos(x, y, z)
         ? this.editOperation.getBefore().getBlockAtWorldPos(x, y, z)
         : this.getChunk(ChunkUtil.indexChunkFromBlock(x, z)).getBlock(x, y, z);
   }

   @Override
   public int getFluidId(int x, int y, int z) {
      return this.editOperation.getFluid(x, y, z);
   }
}
