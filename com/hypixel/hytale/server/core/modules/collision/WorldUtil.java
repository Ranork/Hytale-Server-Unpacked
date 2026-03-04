package com.hypixel.hytale.server.core.modules.collision;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blockhitbox.BlockBoundingBoxes;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.universe.world.chunk.BlockChunk;
import com.hypixel.hytale.server.core.universe.world.chunk.ChunkColumn;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import com.hypixel.hytale.server.core.universe.world.storage.ChunkStore;
import com.hypixel.hytale.server.core.util.FillerBlockUtil;
import javax.annotation.Nonnull;

public final class WorldUtil {
   public static boolean isFluidOnlyBlock(@Nonnull BlockType blockType, int fluidId) {
      return blockType.getMaterial() == BlockMaterial.Empty && fluidId != 0;
   }

   public static boolean isSolidOnlyBlock(@Nonnull BlockType blockType, int fluidId) {
      return blockType.getMaterial() == BlockMaterial.Solid && fluidId == 0;
   }

   public static boolean isEmptyOnlyBlock(@Nonnull BlockType blockType, int fluidId) {
      return blockType.getMaterial() == BlockMaterial.Empty && fluidId == 0;
   }

   public static int getFluidIdAtPosition(@Nonnull ComponentAccessor<ChunkStore> chunkStore, @Nonnull ChunkColumn chunkColumnComponent, int x, int y, int z) {
      if (y >= 0 && y < 320) {
         Ref<ChunkStore> sectionRef = chunkColumnComponent.getSection(ChunkUtil.chunkCoordinate(y));
         if (sectionRef != null && sectionRef.isValid()) {
            FluidSection fluidSectionComponent = chunkStore.getComponent(sectionRef, FluidSection.getComponentType());
            return fluidSectionComponent == null ? 0 : fluidSectionComponent.getFluidId(x, y, z);
         } else {
            return 0;
         }
      } else {
         return 0;
      }
   }

   public static long getPackedMaterialAndFluidAtPosition(
      @Nonnull Ref<ChunkStore> chunkRef, @Nonnull ComponentAccessor<ChunkStore> chunkStore, double x, double y, double z
   ) {
      if (!(y < 0.0) && !(y >= 320.0)) {
         int blockX = MathUtil.floor(x);
         int blockY = MathUtil.floor(y);
         int blockZ = MathUtil.floor(z);
         ChunkColumn chunkColumnComponent = chunkStore.getComponent(chunkRef, ChunkColumn.getComponentType());
         if (chunkColumnComponent == null) {
            return MathUtil.packLong(BlockMaterial.Empty.ordinal(), 0);
         } else {
            BlockChunk blockChunkComponent = chunkStore.getComponent(chunkRef, BlockChunk.getComponentType());
            if (blockChunkComponent == null) {
               return MathUtil.packLong(BlockMaterial.Empty.ordinal(), 0);
            } else {
               BlockSection blockSection = blockChunkComponent.getSectionAtBlockY(blockY);
               int fluidId = 0;
               Ref<ChunkStore> sectionRef = chunkColumnComponent.getSection(ChunkUtil.chunkCoordinate(y));
               if (sectionRef != null && sectionRef.isValid()) {
                  FluidSection fluidSectionComponent = chunkStore.getComponent(sectionRef, FluidSection.getComponentType());
                  if (fluidSectionComponent != null) {
                     fluidId = fluidSectionComponent.getFluidId(blockX, blockY, blockZ);
                     if (fluidId != 0) {
                        Fluid fluid = Fluid.getAssetMap().getAsset(fluidId);
                        if (fluid != null) {
                           double yTest = y - blockY;
                           if (yTest > (double)fluidSectionComponent.getFluidLevel(blockX, blockY, blockZ) / fluid.getMaxFluidLevel()) {
                              fluidId = 0;
                           }
                        }
                     }
                  }
               }

               int blockId = blockSection.get(blockX, blockY, blockZ);
               if (blockId == 0) {
                  return MathUtil.packLong(BlockMaterial.Empty.ordinal(), fluidId);
               } else {
                  BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
                  if (blockType != null && !blockType.isUnknown()) {
                     double relativeY = y - blockY;
                     String blockTypeKey = blockType.getId();
                     BlockType blockTypeAsset = BlockType.getAssetMap().getAsset(blockTypeKey);
                     if (blockTypeAsset == null) {
                        return MathUtil.packLong(BlockMaterial.Empty.ordinal(), fluidId);
                     } else {
                        BlockMaterial blockTypeMaterial = blockType.getMaterial();
                        int filler = blockSection.getFiller(blockX, blockY, blockZ);
                        int rotation = blockSection.getRotationIndex(blockX, blockY, blockZ);
                        if (filler != 0 && blockTypeAsset.getMaterial() == BlockMaterial.Solid) {
                           BlockBoundingBoxes boundingBoxes = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());
                           if (boundingBoxes == null) {
                              return MathUtil.packLong(BlockMaterial.Empty.ordinal(), fluidId);
                           }

                           BlockBoundingBoxes.RotatedVariantBoxes rotatedBoxes = boundingBoxes.get(rotation);
                           int fillerX = FillerBlockUtil.unpackX(filler);
                           int fillerY = FillerBlockUtil.unpackY(filler);
                           int fillerZ = FillerBlockUtil.unpackZ(filler);
                           if (rotatedBoxes.containsPosition(x - blockX + fillerX, relativeY + fillerY, z - blockZ + fillerZ)) {
                              return MathUtil.packLong(BlockMaterial.Solid.ordinal(), fluidId);
                           }
                        } else if (blockTypeMaterial == BlockMaterial.Solid) {
                           BlockBoundingBoxes boundingBoxesx = BlockBoundingBoxes.getAssetMap().getAsset(blockType.getHitboxTypeIndex());
                           if (boundingBoxesx == null) {
                              return MathUtil.packLong(BlockMaterial.Empty.ordinal(), fluidId);
                           }

                           BlockBoundingBoxes.RotatedVariantBoxes rotatedBoxes = boundingBoxesx.get(rotation);
                           if (rotatedBoxes.containsPosition(x - blockX, relativeY, z - blockZ)) {
                              return MathUtil.packLong(BlockMaterial.Solid.ordinal(), fluidId);
                           }
                        }

                        return MathUtil.packLong(BlockMaterial.Empty.ordinal(), fluidId);
                     }
                  } else {
                     return MathUtil.packLong(BlockMaterial.Empty.ordinal(), fluidId);
                  }
               }
            }
         }
      } else {
         return MathUtil.packLong(BlockMaterial.Empty.ordinal(), 0);
      }
   }

   public static int findFluidBlock(
      @Nonnull ComponentAccessor<ChunkStore> chunkStore,
      @Nonnull ChunkColumn chunkColumnComponent,
      @Nonnull BlockChunk blockChunkComponent,
      int x,
      int y,
      int z,
      boolean allowBubble
   ) {
      if (y >= 0 && y < 320) {
         if (getFluidIdAtPosition(chunkStore, chunkColumnComponent, x, y++, z) != 0) {
            return y;
         } else if (y != 320 && allowBubble) {
            BlockSection blockSection = blockChunkComponent.getSectionAtBlockY(y);
            int blockId = blockSection.get(x, y++, z);
            BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
            BlockMaterial materialLowerBlock = blockType != null ? blockType.getMaterial() : BlockMaterial.Empty;
            if (getFluidIdAtPosition(chunkStore, chunkColumnComponent, x, y++, z) != 0) {
               return y;
            } else if (materialLowerBlock == BlockMaterial.Solid && y != 320) {
               return getFluidIdAtPosition(chunkStore, chunkColumnComponent, x, y++, z) != 0 ? y : -1;
            } else {
               return -1;
            }
         } else {
            return -1;
         }
      } else {
         return -1;
      }
   }

   public static int getWaterLevel(
      @Nonnull ComponentAccessor<ChunkStore> chunkStore,
      @Nonnull ChunkColumn chunkColumnComponent,
      @Nonnull BlockChunk blockChunkComponent,
      int x,
      int z,
      int startY
   ) {
      startY = findFluidBlock(chunkStore, chunkColumnComponent, blockChunkComponent, x, startY, z, true);
      if (startY == -1) {
         return -1;
      } else {
         while (startY + 1 < 320) {
            BlockSection blockSection = blockChunkComponent.getSectionAtBlockY(startY + 1);
            int blockId = blockSection.get(x, startY + 1, z);
            BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
            if (blockType == null) {
               break;
            }

            int fluidId = getFluidIdAtPosition(chunkStore, chunkColumnComponent, x, startY + 1, z);
            if (!isFluidOnlyBlock(blockType, fluidId)) {
               break;
            }

            startY++;
         }

         return startY;
      }
   }

   public static int findFarthestEmptySpaceBelow(
      @Nonnull ComponentAccessor<ChunkStore> chunkStore,
      @Nonnull ChunkColumn chunkColumnComponent,
      @Nonnull BlockChunk blockChunkComponent,
      int x,
      int y,
      int z,
      int yFail
   ) {
      if (y < 0) {
         return yFail;
      } else {
         if (y >= 320) {
            y = 319;
         }

         BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
         int indexSection = ChunkUtil.indexSection(y);

         while (indexSection >= 0) {
            Ref<ChunkStore> sectionRef = chunkColumnComponent.getSection(indexSection);
            FluidSection fluidSectionComponent = chunkStore.getComponent(sectionRef, FluidSection.getComponentType());
            BlockSection chunkSection = blockChunkComponent.getSectionAtIndex(indexSection);
            if (!chunkSection.isSolidAir() || fluidSectionComponent == null || !fluidSectionComponent.isEmpty()) {
               int yBottom = 32 * indexSection--;

               while (y >= yBottom) {
                  int blockId = chunkSection.get(x, y--, z);
                  int fluidId = fluidSectionComponent != null ? fluidSectionComponent.getFluidId(x, y, z) : 0;
                  if (blockId != 0 || fluidId != 0) {
                     BlockType blockType = assetMap.getAsset(blockId);
                     if (blockType != null && !blockType.isUnknown()) {
                        int filler = chunkSection.getFiller(x, y, z);
                        if (filler == 0 && isEmptyOnlyBlock(blockType, fluidId)) {
                           continue;
                        }

                        return y + 2;
                     }

                     return y + 2;
                  }
               }
            } else {
               y = 32 * indexSection - 1;
               if (y <= 0) {
                  return 0;
               }

               indexSection--;
            }
         }

         return 0;
      }
   }

   public static int findFarthestEmptySpaceAbove(
      @Nonnull ComponentAccessor<ChunkStore> chunkStore,
      @Nonnull ChunkColumn chunkColumnComponent,
      @Nonnull BlockChunk blockChunkComponent,
      int x,
      int y,
      int z,
      int yFail
   ) {
      if (y >= 320) {
         return Integer.MAX_VALUE;
      } else if (y < 0) {
         return yFail;
      } else {
         BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
         int sectionCount = blockChunkComponent.getSectionCount();
         int indexSection = ChunkUtil.indexSection(y);

         while (indexSection < sectionCount) {
            Ref<ChunkStore> sectionRef = chunkColumnComponent.getSection(indexSection);
            FluidSection fluidSectionComponent = chunkStore.getComponent(sectionRef, FluidSection.getComponentType());
            BlockSection chunkSection = blockChunkComponent.getSectionAtIndex(indexSection);
            if (!chunkSection.isSolidAir() || fluidSectionComponent == null || !fluidSectionComponent.isEmpty()) {
               int yTop = 32 * ++indexSection;

               while (y < yTop) {
                  int blockId = chunkSection.get(x, y++, z);
                  int fluidId = fluidSectionComponent != null ? fluidSectionComponent.getFluidId(x, y, z) : 0;
                  if (blockId != 0 || fluidId != 0) {
                     BlockType blockType = assetMap.getAsset(blockId);
                     if (blockType != null && !blockType.isUnknown()) {
                        int filler = chunkSection.getFiller(x, y, z);
                        if (filler == 0 && isEmptyOnlyBlock(blockType, fluidId)) {
                           continue;
                        }

                        return y - 1;
                     }

                     return y - 1;
                  }
               }
            } else {
               y = 32 * ++indexSection;
               if (y >= 320) {
                  return 319;
               }
            }
         }

         return Integer.MAX_VALUE;
      }
   }
}
