package com.hypixel.hytale.builtin.mounts;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3f;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.protocol.BlockMountType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.mountpoints.BlockMountPoint;
import com.hypixel.hytale.server.core.modules.block.BlockModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;

public final class BlockMountAPI {
   private BlockMountAPI() {
   }

   @Nonnull
   public static BlockMountAPI.BlockMountResult mountOnBlock(
      @Nonnull Ref<EntityStore> entity, @Nonnull CommandBuffer<EntityStore> commandBuffer, @Nonnull Vector3i targetBlock, @Nonnull Vector3f interactPos
   ) {
      MountedComponent existingMounted = commandBuffer.getComponent(entity, MountedComponent.getComponentType());
      if (existingMounted != null) {
         return BlockMountAPI.DidNotMount.ALREADY_MOUNTED;
      } else {
         World world = entity.getStore().getExternalData().getWorld();
         WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(targetBlock.x, targetBlock.z));
         if (chunk == null) {
            return BlockMountAPI.DidNotMount.CHUNK_NOT_FOUND;
         } else {
            Ref<ChunkStore> chunkRef = chunk.getReference();
            if (chunkRef == null) {
               return BlockMountAPI.DidNotMount.CHUNK_REF_NOT_FOUND;
            } else {
               ChunkStore chunkStore = world.getChunkStore();
               BlockComponentChunk blockComponentChunk = chunkStore.getStore().getComponent(chunkRef, BlockComponentChunk.getComponentType());
               if (blockComponentChunk == null) {
                  return BlockMountAPI.DidNotMount.CHUNK_REF_NOT_FOUND;
               } else {
                  BlockType blockType = world.getBlockType(targetBlock);
                  if (blockType == null) {
                     return BlockMountAPI.DidNotMount.INVALID_BLOCK;
                  } else {
                     int rotationIndex = chunk.getRotationIndex(targetBlock.x, targetBlock.y, targetBlock.z);
                     int blockIndex = ChunkUtil.indexBlockInColumn(targetBlock.x, targetBlock.y, targetBlock.z);
                     Ref<ChunkStore> blockRef = blockComponentChunk.getEntityReference(blockIndex);
                     if (blockRef == null || !blockRef.isValid()) {
                        Holder<ChunkStore> blockHolder = ChunkStore.REGISTRY.newHolder();
                        blockHolder.putComponent(BlockModule.BlockStateInfo.getComponentType(), new BlockModule.BlockStateInfo(blockIndex, chunkRef));
                        blockRef = world.getChunkStore().getStore().addEntity(blockHolder, AddReason.SPAWN);
                        if (blockRef == null || !blockRef.isValid()) {
                           return BlockMountAPI.DidNotMount.BLOCK_REF_NOT_FOUND;
                        }
                     }

                     BlockMountType blockMountType = null;
                     BlockMountPoint[] mountPointsForBlock = null;
                     if (blockType.getSeats() != null) {
                        blockMountType = BlockMountType.Seat;
                        mountPointsForBlock = blockType.getSeats().getRotated(rotationIndex);
                     } else {
                        if (blockType.getBeds() == null) {
                           return BlockMountAPI.DidNotMount.UNKNOWN_BLOCKMOUNT_TYPE;
                        }

                        blockMountType = BlockMountType.Bed;
                        mountPointsForBlock = blockType.getBeds().getRotated(rotationIndex);
                     }

                     BlockMountComponent blockMountComponent = world.getChunkStore().getStore().getComponent(blockRef, BlockMountComponent.getComponentType());
                     if (blockMountComponent == null) {
                        blockMountComponent = new BlockMountComponent(blockMountType, targetBlock, blockType, rotationIndex);
                        world.getChunkStore().getStore().addComponent(blockRef, BlockMountComponent.getComponentType(), blockMountComponent);
                     }

                     if (mountPointsForBlock != null && mountPointsForBlock.length != 0) {
                        BlockMountPoint pickedMountPoint = blockMountComponent.findAvailableSeat(targetBlock, mountPointsForBlock, interactPos);
                        if (pickedMountPoint == null) {
                           return BlockMountAPI.DidNotMount.NO_MOUNT_POINT_FOUND;
                        } else {
                           TransformComponent transformComponent = commandBuffer.getComponent(entity, TransformComponent.getComponentType());
                           if (transformComponent != null) {
                              Vector3f position = pickedMountPoint.computeWorldSpacePosition(blockMountComponent.getBlockPos());
                              Vector3f rotationEuler = pickedMountPoint.computeRotationEuler(blockMountComponent.getExpectedRotation());
                              transformComponent.setPosition(position.toVector3d());
                              transformComponent.setRotation(rotationEuler);
                           }

                           MountedComponent mountedComponent = new MountedComponent(blockRef, new Vector3f(0.0F, 0.0F, 0.0F), blockMountType);
                           commandBuffer.addComponent(entity, MountedComponent.getComponentType(), mountedComponent);
                           blockMountComponent.putSeatedEntity(pickedMountPoint, entity);
                           return new BlockMountAPI.Mounted(blockType, mountedComponent);
                        }
                     } else {
                        return BlockMountAPI.DidNotMount.NO_MOUNT_POINT_FOUND;
                     }
                  }
               }
            }
         }
      }
   }

   public sealed interface BlockMountResult permits BlockMountAPI.Mounted, BlockMountAPI.DidNotMount {
   }

   public static enum DidNotMount implements BlockMountAPI.BlockMountResult {
      CHUNK_NOT_FOUND,
      CHUNK_REF_NOT_FOUND,
      BLOCK_REF_NOT_FOUND,
      INVALID_BLOCK,
      ALREADY_MOUNTED,
      UNKNOWN_BLOCKMOUNT_TYPE,
      NO_MOUNT_POINT_FOUND;
   }

   public record Mounted(BlockType blockType, MountedComponent component) implements BlockMountAPI.BlockMountResult {
   }
}
