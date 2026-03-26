package com.hypixel.hytale.server.core.universe.world.chunk;

import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.protocol.BlockParticleEvent;
import com.hypixel.hytale.protocol.Opacity;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.WorldNotificationHandler;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;

public class BlockOperations {
   public static void updateBlockArea(ChunkStore chunkStore, BlockSection section, BlockType blockType, int rotation, int x, int y, int z) {
      IndexedLookupTableAssetMap<String, BlockBoundingBoxes> boundingBoxAssets = BlockBoundingBoxes.getAssetMap();
      BlockBoundingBoxes boundingBox = boundingBoxAssets.getAsset(blockType.getHitboxTypeIndex());
      if (boundingBox != null) {
         FillerBlockUtil.forEachFillerBlock(0.0F, 1, boundingBox.get(rotation), (x1, y1, z1) -> {
            int bx = x + x1;
            int by = y + y1;
            int bz = z + z1;
            if (ChunkUtil.isSameChunkSection(bx, by, bz, x, y, z)) {
               section.setTicking(bx, by, bz, true);
            } else {
               chunkStore.getChunkSectionReferenceAtBlockAsync(bx, by, bz, 3).thenAccept(ref -> {
                  if (ref != null && ref.isValid()) {
                     BlockSection otherSection = ref.getStore().getComponent((Ref<ChunkStore>)ref, BlockSection.getComponentType());
                     if (otherSection != null) {
                        otherSection.setTicking(bx, by, bz, true);
                     }
                  }
               });
            }
         });
      }
   }

   public static short updateBlockHeight(BlockChunk blockChunk, int newBlockId, BlockType newBlock, int x, int y, int z, short oldHeight) {
      short newHeight = oldHeight;
      if (oldHeight <= y) {
         if (oldHeight == y && newBlockId == 0) {
            newHeight = blockChunk.updateHeight(x, z, (short)y);
         } else if (oldHeight < y && newBlockId != 0 && newBlock.getOpacity() != Opacity.Transparent) {
            newHeight = (short)y;
            blockChunk.setHeight(x, z, newHeight);
         }
      }

      return newHeight;
   }

   public static void spawnBlockParticles(ChunkStore chunkStore, int oldBlockId, int newBlockId, int x, int y, int z, boolean isPhysics) {
      WorldNotificationHandler notificationHandler = chunkStore.getWorld().getNotificationHandler();
      if (oldBlockId == 0 && newBlockId != 0) {
         notificationHandler.sendBlockParticle(x + 0.5, y + 0.5, z + 0.5, newBlockId, BlockParticleEvent.Build);
      } else if (oldBlockId != 0 && newBlockId == 0) {
         BlockParticleEvent particleType = isPhysics ? BlockParticleEvent.Physics : BlockParticleEvent.Break;
         notificationHandler.sendBlockParticle(x + 0.5, y + 0.5, z + 0.5, oldBlockId, particleType);
      } else {
         notificationHandler.sendBlockParticle(x + 0.5, y + 0.5, z + 0.5, oldBlockId, BlockParticleEvent.Break);
         notificationHandler.sendBlockParticle(x + 0.5, y + 0.5, z + 0.5, newBlockId, BlockParticleEvent.Build);
      }
   }
}
