package com.hypixel.hytale.server.core.universe.world.accessor;

import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockPosition;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Deprecated
public interface IChunkAccessorSync<WorldChunk extends BlockAccessor> {
   @Nullable
   WorldChunk getChunkIfInMemory(long var1);

   @Nullable
   WorldChunk loadChunkIfInMemory(long var1);

   @Nullable
   WorldChunk getChunkIfLoaded(long var1);

   @Nullable
   WorldChunk getChunkIfNonTicking(long var1);

   @Nullable
   WorldChunk getChunk(long var1);

   @Nullable
   WorldChunk getNonTickingChunk(long var1);

   default int getBlock(@Nonnull Vector3i pos) {
      return this.getBlock(pos.getX(), pos.getY(), pos.getZ());
   }

   default int getBlock(int x, int y, int z) {
      long chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
      WorldChunk worldChunk = this.getChunk(chunkIndex);
      return worldChunk == null ? 0 : worldChunk.getBlock(x, y, z);
   }

   @Nullable
   default BlockType getBlockType(@Nonnull Vector3i pos) {
      return this.getBlockType(pos.getX(), pos.getY(), pos.getZ());
   }

   @Nullable
   default BlockType getBlockType(int x, int y, int z) {
      long chunkIndex = ChunkUtil.indexChunkFromBlock(x, z);
      WorldChunk worldChunk = this.getChunk(chunkIndex);
      if (worldChunk == null) {
         return null;
      } else {
         int blockId = worldChunk.getBlock(x, y, z);
         return BlockType.getAssetMap().getAsset(blockId);
      }
   }

   default void setBlock(int x, int y, int z, String blockTypeKey) {
      this.setBlock(x, y, z, blockTypeKey, 0);
   }

   default void setBlock(int x, int y, int z, String blockTypeKey, int settings) {
      this.getChunk(ChunkUtil.indexChunkFromBlock(x, z)).setBlock(x, y, z, blockTypeKey, settings);
   }

   default boolean breakBlock(int x, int y, int z, int settings) {
      return this.getChunk(ChunkUtil.indexChunkFromBlock(x, z)).breakBlock(x, y, z, settings);
   }

   default boolean testBlockTypes(
      int x, int y, int z, @Nonnull BlockType blockTypeToTest, int rotation, @Nonnull IChunkAccessorSync.TestBlockFunction predicate
   ) {
      return this.getChunk(ChunkUtil.indexChunkFromBlock(x, z)).testBlockTypes(x, y, z, blockTypeToTest, rotation, predicate);
   }

   default boolean testPlaceBlock(int x, int y, int z, @Nonnull BlockType blockTypeToTest, int rotation) {
      return this.getChunk(ChunkUtil.indexChunkFromBlock(x, z)).testPlaceBlock(x, y, z, blockTypeToTest, rotation);
   }

   default boolean testPlaceBlock(
      int x, int y, int z, @Nonnull BlockType blockTypeToTest, int rotation, @Nonnull IChunkAccessorSync.TestBlockFunction predicate
   ) {
      return this.getChunk(ChunkUtil.indexChunkFromBlock(x, z)).testPlaceBlock(x, y, z, blockTypeToTest, rotation, predicate);
   }

   @Nullable
   @Deprecated
   default BlockState getState(int x, int y, int z, boolean followFiller) {
      WorldChunk chunk = this.getChunk(ChunkUtil.indexChunkFromBlock(x, z));
      if (followFiller) {
         int filler = chunk.getFiller(x, y, z);
         if (filler != 0) {
            x -= FillerBlockUtil.unpackX(filler);
            y -= FillerBlockUtil.unpackY(filler);
            z -= FillerBlockUtil.unpackZ(filler);
         }
      }

      return y >= 0 && y < 320 ? chunk.getState(x, y, z) : null;
   }

   @Nullable
   default Holder<ChunkStore> getBlockComponentHolder(int x, int y, int z) {
      return y >= 0 && y < 320 ? this.getChunk(ChunkUtil.indexChunkFromBlock(x, z)).getBlockComponentHolder(x, y, z) : null;
   }

   default void setBlockInteractionState(@Nonnull Vector3i blockPosition, @Nonnull BlockType blockType, @Nonnull String state) {
      this.getChunk(ChunkUtil.indexChunkFromBlock(blockPosition.x, blockPosition.z)).setBlockInteractionState(blockPosition, blockType, state);
   }

   @Nonnull
   @Deprecated(forRemoval = true)
   default BlockPosition getBaseBlock(@Nonnull BlockPosition position) {
      WorldChunk chunk = this.getNonTickingChunk(ChunkUtil.indexChunkFromBlock(position.x, position.z));
      int filler = chunk.getFiller(position.x, position.y, position.z);
      return filler != 0
         ? new BlockPosition(
            position.x - FillerBlockUtil.unpackX(filler), position.y - FillerBlockUtil.unpackY(filler), position.z - FillerBlockUtil.unpackZ(filler)
         )
         : position;
   }

   default int getBlockRotationIndex(int x, int y, int z) {
      return this.getChunk(ChunkUtil.indexChunkFromBlock(x, z)).getRotationIndex(x, y, z);
   }

   @FunctionalInterface
   public interface TestBlockFunction {
      boolean test(int var1, int var2, int var3, BlockType var4, int var5, int var6);
   }
}
