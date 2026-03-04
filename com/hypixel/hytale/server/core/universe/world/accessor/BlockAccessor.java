package com.hypixel.hytale.server.core.universe.world.accessor;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.function.predicate.TriIntPredicate;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.Rotation;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.RotationTuple;
import com.hypixel.hytale.server.core.universe.world.meta.BlockState;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface BlockAccessor {
   int getX();

   int getZ();

   ChunkAccessor getChunkAccessor();

   int getBlock(int var1, int var2, int var3);

   default int getBlock(@Nonnull Vector3i pos) {
      return this.getBlock(pos.getX(), pos.getY(), pos.getZ());
   }

   boolean setBlock(int var1, int var2, int var3, int var4, BlockType var5, int var6, int var7, int var8);

   default boolean setBlock(int x, int y, int z, int id, BlockType blockType) {
      return this.setBlock(x, y, z, id, blockType, 0, 0, 0);
   }

   default boolean setBlock(int x, int y, int z, String blockTypeKey) {
      return this.setBlock(x, y, z, blockTypeKey, 0);
   }

   default boolean setBlock(int x, int y, int z, String blockTypeKey, int settings) {
      int index = BlockType.getAssetMap().getIndex(blockTypeKey);
      if (index == Integer.MIN_VALUE) {
         throw new IllegalArgumentException("Unknown key! " + blockTypeKey);
      } else {
         return this.setBlock(x, y, z, index, settings);
      }
   }

   default boolean setBlock(int x, int y, int z, int id) {
      return this.setBlock(x, y, z, id, 0);
   }

   default boolean setBlock(int x, int y, int z, int id, int settings) {
      return this.setBlock(x, y, z, id, BlockType.getAssetMap().getAsset(id), 0, 0, settings);
   }

   default boolean setBlock(int x, int y, int z, @Nonnull BlockType blockType) {
      return this.setBlock(x, y, z, blockType, 0);
   }

   default boolean setBlock(int x, int y, int z, @Nonnull BlockType blockType, int settings) {
      String key = blockType.getId();
      int index = BlockType.getAssetMap().getIndex(key);
      if (index == Integer.MIN_VALUE) {
         throw new IllegalArgumentException("Unknown key! " + key);
      } else {
         return this.setBlock(x, y, z, index, blockType, 0, 0, settings);
      }
   }

   default boolean breakBlock(int x, int y, int z, int filler, int settings) {
      if ((settings & 16) == 0) {
         x -= FillerBlockUtil.unpackX(filler);
         y -= FillerBlockUtil.unpackY(filler);
         z -= FillerBlockUtil.unpackZ(filler);
      }

      return this.setBlock(x, y, z, 0, BlockType.EMPTY, 0, 0, settings);
   }

   default boolean breakBlock(int x, int y, int z) {
      return this.breakBlock(x, y, z, 0);
   }

   default boolean breakBlock(int x, int y, int z, int settings) {
      return this.breakBlock(x, y, z, 0, settings);
   }

   default boolean testBlocks(int x, int y, int z, @Nonnull BlockType blockTypeToTest, int rotation, @Nonnull TriIntPredicate predicate) {
      int worldX = (this.getX() << 5) + (x & 31);
      int worldZ = (this.getZ() << 5) + (z & 31);
      return FillerBlockUtil.testFillerBlocks(BlockBoundingBoxes.getAssetMap().getAsset(blockTypeToTest.getHitboxTypeIndex()).get(rotation), (x1, y1, z1) -> {
         int blockX = worldX + x1;
         int blockY = y + y1;
         int blockZ = worldZ + z1;
         return predicate.test(blockX, blockY, blockZ);
      });
   }

   default boolean testBlockTypes(
      int x, int y, int z, @Nonnull BlockType blockTypeToTest, int rotation, @Nonnull IChunkAccessorSync.TestBlockFunction predicate
   ) {
      int worldX = (this.getX() << 5) + (x & 31);
      int worldZ = (this.getZ() << 5) + (z & 31);
      return this.testBlocks(x, y, z, blockTypeToTest, rotation, (blockX, blockY, blockZ) -> {
         boolean sameChunk = ChunkUtil.isSameChunk(worldX, worldZ, blockX, blockZ);
         int block;
         int otherRotation;
         int filler;
         if (sameChunk) {
            block = this.getBlock(blockX, blockY, blockZ);
            otherRotation = this.getRotationIndex(blockX, blockY, blockZ);
            filler = this.getFiller(blockX, blockY, blockZ);
         } else {
            BlockAccessor chunk = this.getChunkAccessor().getNonTickingChunk(ChunkUtil.indexChunkFromBlock(blockX, blockZ));
            block = chunk.getBlock(blockX, blockY, blockZ);
            otherRotation = chunk.getRotationIndex(blockX, blockY, blockZ);
            filler = chunk.getFiller(blockX, blockY, blockZ);
         }

         return predicate.test(blockX, blockY, blockZ, BlockType.getAssetMap().getAsset(block), otherRotation, filler);
      });
   }

   default boolean placeBlock(
      int x, int y, int z, String originalBlockTypeKey, @Nonnull Rotation yaw, @Nonnull Rotation pitch, @Nonnull Rotation roll, int settings
   ) {
      return this.placeBlock(x, y, z, originalBlockTypeKey, RotationTuple.of(yaw, pitch, roll), settings, true);
   }

   default boolean placeBlock(int x, int y, int z, String originalBlockTypeKey, @Nonnull RotationTuple rotationTuple, int settings, boolean validatePlacement) {
      BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
      BlockType placedBlockType = assetMap.getAsset(originalBlockTypeKey);
      int rotationIndex = rotationTuple.index();
      if (validatePlacement && !this.testPlaceBlock(x, y, z, placedBlockType, rotationIndex)) {
         return false;
      } else {
         int setBlockSettings = 0;
         if ((settings & 2) != 0) {
            setBlockSettings |= 256;
         }

         this.setBlock(x, y, z, assetMap.getIndex(originalBlockTypeKey), placedBlockType, rotationIndex, 0, setBlockSettings);
         return true;
      }
   }

   default boolean placeBlock(int x, int y, int z, String blockTypeKey, @Nonnull Rotation yaw, @Nonnull Rotation pitch, @Nonnull Rotation roll) {
      return this.placeBlock(x, y, z, blockTypeKey, yaw, pitch, roll, 0);
   }

   default boolean testPlaceBlock(int x, int y, int z, @Nonnull BlockType blockTypeToTest, int rotationIndex) {
      return this.testPlaceBlock(x, y, z, blockTypeToTest, rotationIndex, (x1, y1, z1, blockType, rotation, filler) -> false);
   }

   default boolean testPlaceBlock(
      int x, int y, int z, @Nonnull BlockType blockTypeToTest, int rotationIndex, @Nonnull IChunkAccessorSync.TestBlockFunction filter
   ) {
      return this.testBlockTypes(x, y, z, blockTypeToTest, rotationIndex, (blockX, blockY, blockZ, blockType, rotation, filler) -> {
         if (blockType == BlockType.EMPTY) {
            return true;
         } else if (blockType.getMaterial() == BlockMaterial.Empty) {
            return true;
         } else {
            return filler != 0 && blockType.isUnknown() ? true : filter.test(blockX, blockY, blockZ, blockType, rotation, filler);
         }
      });
   }

   @Nullable
   default BlockType getBlockType(int x, int y, int z) {
      return BlockType.getAssetMap().getAsset(this.getBlock(x, y, z));
   }

   @Nullable
   default BlockType getBlockType(@Nonnull Vector3i block) {
      return this.getBlockType(block.getX(), block.getY(), block.getZ());
   }

   boolean setTicking(int var1, int var2, int var3, boolean var4);

   boolean isTicking(int var1, int var2, int var3);

   @Nullable
   @Deprecated
   BlockState getState(int var1, int var2, int var3);

   @Nullable
   @Deprecated
   Holder<ChunkStore> getBlockComponentHolder(int var1, int var2, int var3);

   @Deprecated
   void setState(int var1, int var2, int var3, BlockState var4, boolean var5);

   @Deprecated
   default void setState(int x, int y, int z, BlockState state) {
      this.setState(x, y, z, state, true);
   }

   default void setBlockInteractionState(@Nonnull Vector3i blockPosition, @Nonnull BlockType blockType, @Nonnull String state) {
      this.setBlockInteractionState(blockPosition.x, blockPosition.y, blockPosition.z, blockType, state, false);
   }

   default void setBlockInteractionState(int x, int y, int z, @Nonnull BlockType blockType, @Nonnull String state, boolean force) {
      if (blockType.getData() != null) {
         String currentState = getCurrentInteractionState(blockType);
         if (force || currentState == null || !currentState.equals(state)) {
            BlockType newState = blockType.getBlockForState(state);
            if (newState != null) {
               int settings = 198;
               int currentRotation = this.getRotationIndex(x, y, z);
               this.setBlock(x, y, z, BlockType.getAssetMap().getIndex(newState.getId()), newState, currentRotation, 0, 198);
            }
         }
      }
   }

   @Nullable
   static String getCurrentInteractionState(@Nonnull BlockType blockType) {
      return blockType.getState() != null ? blockType.getStateForBlock(blockType) : null;
   }

   @Deprecated(forRemoval = true)
   int getFluidId(int var1, int var2, int var3);

   @Deprecated(forRemoval = true)
   byte getFluidLevel(int var1, int var2, int var3);

   @Deprecated(forRemoval = true)
   int getSupportValue(int var1, int var2, int var3);

   @Deprecated(forRemoval = true)
   int getFiller(int var1, int var2, int var3);

   @Deprecated(forRemoval = true)
   int getRotationIndex(int var1, int var2, int var3);

   @Deprecated(forRemoval = true)
   default RotationTuple getRotation(int x, int y, int z) {
      return RotationTuple.get(this.getRotationIndex(x, y, z));
   }
}
