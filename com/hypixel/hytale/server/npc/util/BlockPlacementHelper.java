package com.hypixel.hytale.server.npc.util;

import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import javax.annotation.Nonnull;

public class BlockPlacementHelper {
   public static boolean canPlaceUnitBlock(@Nonnull World world, BlockType placedBlockType, boolean allowEmptyMaterials, int x, int y, int z) {
      WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(x, z));
      if (chunk == null) {
         return false;
      } else {
         int target = chunk.getBlock(x, y, z);
         BlockType targetBlockType = BlockType.getAssetMap().getAsset(target);
         if (!testBlock(placedBlockType, targetBlockType, allowEmptyMaterials)) {
            return false;
         } else {
            target = chunk.getBlock(x, y - 1, z);
            targetBlockType = BlockType.getAssetMap().getAsset(target);
            int filler = chunk.getFiller(x, y - 1, z);
            int rotation = chunk.getRotationIndex(x, y - 1, z);
            return testSupportingBlock(targetBlockType, rotation, filler);
         }
      }
   }

   public static boolean canPlaceBlock(
      @Nonnull World world, @Nonnull BlockType placedBlockType, int rotationIndex, boolean allowEmptyMaterials, int x, int y, int z
   ) {
      return world.testBlockTypes(
         x,
         y,
         z,
         placedBlockType,
         rotationIndex,
         (blockX, blockY, blockZ, blockType, rotation, filler) -> testBlock(placedBlockType, blockType, allowEmptyMaterials)
      );
   }

   public static boolean testBlock(BlockType placedBlockType, @Nonnull BlockType blockType, boolean allowEmptyMaterials) {
      if (blockType == BlockType.EMPTY) {
         return true;
      } else {
         return allowEmptyMaterials && blockType.getMaterial() == BlockMaterial.Empty ? true : true;
      }
   }

   public static boolean testSupportingBlock(@Nonnull BlockType blockType, int rotation, int filler) {
      Box targetHitbox = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex()).get(rotation).getBoundingBox();
      return blockType != BlockType.EMPTY
         && blockType != BlockType.UNKNOWN
         && blockType.getMaterial() == BlockMaterial.Solid
         && filler == 0
         && targetHitbox.isUnitBox();
   }
}
