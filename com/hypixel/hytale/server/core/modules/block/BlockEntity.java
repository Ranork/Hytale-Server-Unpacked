package com.hypixel.hytale.server.core.modules.block;

import com.hypixel.hytale.component.AddReason;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.RemoveReason;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockComponentChunk;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockEntity {
   public static void setBlockEntity(
      @Nonnull ComponentAccessor<ChunkStore> accessor,
      @Nonnull Ref<ChunkStore> chunkRef,
      @Nonnull BlockComponentChunk componentChunk,
      int x,
      int y,
      int z,
      @Nonnull BlockType blockType,
      int rotation,
      @Nullable Holder<ChunkStore> holder
   ) {
      int index = ChunkUtil.indexBlockInColumn(x, y, z);
      if (holder == null) {
         Ref<ChunkStore> reference = componentChunk.getEntityReference(index);
         if (reference != null) {
            accessor.removeEntity(reference, ChunkStore.REGISTRY.newHolder(), RemoveReason.REMOVE);
         } else {
            componentChunk.removeEntityHolder(index);
         }
      } else if (accessor.getArchetype(chunkRef).contains(ChunkStore.REGISTRY.getNonTickingComponentType())) {
         Holder<ChunkStore> oldHolder = componentChunk.removeEntityHolder(index);
         BlockReplaceEvent event = new BlockReplaceEvent(chunkRef, x, y, z, holder, x, y, z);
         BlockBoundingBoxes.RotatedVariantBoxes hitbox = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex()).get(rotation);
         FillerBlockUtil.forEachFillerBlock(hitbox, (x1, y1, z1) -> {
            if (x1 != 0 || y1 != 0 || z1 != 0) {
               int ox = x + x1;
               int oy = y + y1;
               int oz = z + z1;
               if (ChunkUtil.isSameChunk(x, z, ox, oz)) {
                  Holder<ChunkStore> otherHolder = componentChunk.getEntityHolder(ChunkUtil.indexBlockInColumn(ox, oy, oz));
                  if (otherHolder != null) {
                     event.next(ox, oy, oz);
                     accessor.invoke(otherHolder, event);
                  }
               } else {
                  long chunkIndex = ChunkUtil.indexChunkFromBlock(ox, oz);
                  Ref<ChunkStore> otherChunk = accessor.getExternalData().getChunkReference(chunkIndex);
                  if (otherChunk != null) {
                     BlockComponentChunk otherComponentChunk = accessor.getComponent(otherChunk, BlockComponentChunk.getComponentType());
                     if (otherComponentChunk != null) {
                        Ref<ChunkStore> otherReference = otherComponentChunk.getEntityReference(ChunkUtil.indexBlockInColumn(ox, oy, oz));
                        if (otherReference != null) {
                           event.next(ox, oy, oz);
                           accessor.invoke(otherReference, event);
                        }

                        Holder<ChunkStore> otherHolder = otherComponentChunk.getEntityHolder(ChunkUtil.indexBlockInColumn(ox, oy, oz));
                        if (otherHolder != null) {
                           event.next(ox, oy, oz);
                           accessor.invoke(otherHolder, event);
                        }
                     }
                  }
               }
            } else if (oldHolder != null) {
               event.next(x + x1, y + y1, z + z1);
               accessor.invoke(oldHolder, event);
            }
         });
         holder.putComponent(BlockModule.BlockStateInfo.getComponentType(), new BlockModule.BlockStateInfo(index, chunkRef));
         componentChunk.addEntityHolder(index, holder);
      } else {
         Ref<ChunkStore> oldReference = componentChunk.getEntityReference(index);
         BlockReplaceEvent event = new BlockReplaceEvent(chunkRef, x, y, z, holder, x, y, z);
         BlockBoundingBoxes.RotatedVariantBoxes hitbox = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex()).get(rotation);
         FillerBlockUtil.forEachFillerBlock(hitbox, (x1, y1, z1) -> {
            if (x1 != 0 || y1 != 0 || z1 != 0) {
               int ox = x + x1;
               int oy = y + y1;
               int oz = z + z1;
               if (ChunkUtil.isSameChunk(x, z, ox, oz)) {
                  Ref<ChunkStore> otherReference = componentChunk.getEntityReference(ChunkUtil.indexBlockInColumn(ox, oy, oz));
                  if (otherReference != null) {
                     event.next(x + x1, y + y1, z + z1);
                     accessor.invoke(otherReference, event);
                  }
               } else {
                  long chunkIndex = ChunkUtil.indexChunkFromBlock(ox, oz);
                  Ref<ChunkStore> otherChunk = accessor.getExternalData().getChunkReference(chunkIndex);
                  if (otherChunk != null) {
                     BlockComponentChunk otherComponentChunk = accessor.getComponent(otherChunk, BlockComponentChunk.getComponentType());
                     if (otherComponentChunk != null) {
                        Ref<ChunkStore> otherReference = otherComponentChunk.getEntityReference(ChunkUtil.indexBlockInColumn(ox, oy, oz));
                        if (otherReference != null) {
                           event.next(ox, oy, oz);
                           accessor.invoke(otherReference, event);
                        }

                        Holder<ChunkStore> otherHolder = otherComponentChunk.getEntityHolder(ChunkUtil.indexBlockInColumn(ox, oy, oz));
                        if (otherHolder != null) {
                           event.next(ox, oy, oz);
                           accessor.invoke(otherHolder, event);
                        }
                     }
                  }
               }
            } else if (oldReference != null) {
               event.next(x + x1, y + y1, z + z1);
               accessor.invoke(oldReference, event);
            }
         });
         if (oldReference != null) {
            accessor.removeEntity(oldReference, ChunkStore.REGISTRY.newHolder(), RemoveReason.REMOVE);
         } else {
            componentChunk.removeEntityHolder(index);
         }

         holder.putComponent(BlockModule.BlockStateInfo.getComponentType(), new BlockModule.BlockStateInfo(index, chunkRef));
         accessor.addEntity(holder, AddReason.SPAWN);
      }
   }
}
