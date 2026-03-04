package com.hypixel.hytale.server.core.asset.type.fluid;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.HashUtil;
import com.hypixel.hytale.math.vector.Vector2i;
import com.hypixel.hytale.server.core.asset.type.blocktick.BlockTickStrategy;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.section.BlockSection;
import com.hypixel.hytale.server.core.universe.world.chunk.section.FluidSection;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FiniteFluidTicker extends FluidTicker {
   @Nonnull
   public static BuilderCodec<FiniteFluidTicker> CODEC = BuilderCodec.builder(FiniteFluidTicker.class, FiniteFluidTicker::new, FluidTicker.BASE_CODEC).build();
   @Nonnull
   private static final Vector2i[] DIAG_OFFSETS = new Vector2i[]{new Vector2i(-1, -1), new Vector2i(1, 1), new Vector2i(1, -1), new Vector2i(-1, 1)};
   private static final int MAX_DROP_DISTANCE = 2;
   @Nonnull
   private static final List<List<Vector2i[]>> OFFSETS_LISTS = new ObjectArrayList();
   private static final int RANDOM_VARIANTS = 16;

   @Nonnull
   @Override
   protected FluidTicker.AliveStatus isAlive(
      @Nonnull FluidTicker.Accessor accessor,
      @Nonnull FluidSection fluidSection,
      @Nonnull BlockSection blockSection,
      Fluid fluid,
      int fluidId,
      byte fluidLevel,
      int worldX,
      int worldY,
      int worldZ
   ) {
      return FluidTicker.AliveStatus.ALIVE;
   }

   @Nonnull
   @Override
   protected BlockTickStrategy spread(
      World world,
      long tick,
      @Nonnull FluidTicker.Accessor accessor,
      @Nonnull FluidSection fluidSection,
      BlockSection blockSection,
      @Nonnull Fluid fluid,
      int fluidId,
      byte fluidLevel,
      int worldX,
      int worldY,
      int worldZ
   ) {
      if (worldY == 0) {
         return BlockTickStrategy.SLEEP;
      } else {
         boolean isDifferentSectionBelow = fluidSection.getY() != ChunkUtil.chunkCoordinate(worldY - 1);
         FluidSection belowFluidSection = isDifferentSectionBelow ? accessor.getFluidSectionByBlock(worldX, worldY - 1, worldZ) : fluidSection;
         BlockSection belowBlockSection = isDifferentSectionBelow ? accessor.getBlockSectionByBlock(worldX, worldY - 1, worldZ) : blockSection;
         if (belowFluidSection != null && belowBlockSection != null) {
            int bottomFluidId = belowFluidSection.getFluidId(worldX, worldY - 1, worldZ);
            byte bottomFluidLevel = belowFluidSection.getFluidLevel(worldX, worldY - 1, worldZ);
            return this.spreadDownwards(
                  accessor,
                  fluidSection,
                  blockSection,
                  belowFluidSection,
                  belowBlockSection,
                  worldX,
                  worldY,
                  worldZ,
                  fluid,
                  fluidId,
                  fluidLevel,
                  bottomFluidId,
                  bottomFluidLevel
               )
               ? BlockTickStrategy.CONTINUE
               : this.spreadSideways(tick, accessor, fluidSection, blockSection, worldX, worldY, worldZ, fluid, fluidId, fluidLevel);
         } else {
            return BlockTickStrategy.SLEEP;
         }
      }
   }

   private boolean spreadDownwards(
      @Nonnull FluidTicker.Accessor accessor,
      @Nonnull FluidSection fluidSection,
      BlockSection blockSection,
      @Nonnull FluidSection belowFluidSection,
      @Nonnull BlockSection belowBlockSection,
      int worldX,
      int worldY,
      int worldZ,
      @Nonnull Fluid fluid,
      int fluidId,
      byte fluidLevel,
      int bottomFluidId,
      byte bottomFluidLevel
   ) {
      if (fluidId != bottomFluidId && bottomFluidId != 0) {
         return false;
      } else if (isSolid(BlockType.getAssetMap().getAsset(belowBlockSection.get(worldX, worldY - 1, worldZ)))) {
         return false;
      } else {
         int topY = this.getTopY(accessor, fluidSection, worldX, worldY, worldZ, fluid, fluidId);
         boolean isTopDifferent = ChunkUtil.chunkCoordinate(topY) != fluidSection.getY();
         FluidSection topFluidSection = isTopDifferent ? accessor.getFluidSectionByBlock(worldX, topY, worldZ) : fluidSection;
         BlockSection topBlockSection = isTopDifferent ? accessor.getBlockSectionByBlock(worldX, topY, worldZ) : blockSection;
         int topBlockLevel = topFluidSection.getFluidLevel(worldX, topY, worldZ);
         int transferLevel = Math.min(topBlockLevel, fluid.getMaxFluidLevel() - bottomFluidLevel);
         if (transferLevel == 0) {
            return false;
         } else {
            int newBottomLevel = bottomFluidId == 0 ? transferLevel : bottomFluidLevel + transferLevel;
            belowFluidSection.setFluid(worldX, worldY - 1, worldZ, fluidId, (byte)newBottomLevel);
            setTickingSurrounding(accessor, belowBlockSection, worldX, worldY - 1, worldZ);
            boolean updated;
            if (transferLevel == topBlockLevel) {
               updated = topFluidSection.setFluid(worldX, topY, worldZ, 0, (byte)0);
            } else {
               updated = topFluidSection.setFluid(worldX, topY, worldZ, fluidId, (byte)(topBlockLevel - transferLevel));
            }

            setTickingSurrounding(accessor, topBlockSection, worldX, topY, worldZ);
            return updated;
         }
      }
   }

   @Nonnull
   private BlockTickStrategy spreadSideways(
      long tick,
      @Nonnull FluidTicker.Accessor accessor,
      @Nonnull FluidSection fluidSection,
      BlockSection blockSection,
      int worldX,
      int worldY,
      int worldZ,
      @Nonnull Fluid fluid,
      int fluidId,
      byte fluidLevel
   ) {
      if (fluidLevel == 1) {
         return BlockTickStrategy.SLEEP;
      } else {
         int newLevel = fluidLevel;
         BlockTypeAssetMap<String, BlockType> blockTypeMap = BlockType.getAssetMap();
         long hash = HashUtil.rehash(worldX, worldY, worldZ, 4032035379L);
         int index = OFFSETS_LISTS.size() + (int)((hash + tick) % OFFSETS_LISTS.size());
         List<Vector2i[]> offsetsList = OFFSETS_LISTS.get(index % OFFSETS_LISTS.size());

         for (int idx = 0; idx < offsetsList.size(); idx++) {
            Vector2i[] offsetArray = offsetsList.get(idx);
            int offsets = this.getSpreadOffsets(blockTypeMap, accessor, fluidSection, blockSection, worldX, worldY, worldZ, offsetArray, fluidId, 2);
            boolean spreadDownhill = offsets != 0;

            for (int i = 0; i < offsetArray.length && newLevel != 1; i++) {
               if (!spreadDownhill || (offsets & 1 << i) != 0) {
                  Vector2i offset = offsetArray[i];
                  FiniteFluidTicker.SpreadOutcome spreadOutcome = this.spreadToOffset(
                     accessor, fluidSection, blockSection, offset, worldX, worldY, worldZ, fluid, fluidId, fluidLevel
                  );
                  if (spreadOutcome != null) {
                     switch (spreadOutcome) {
                        case SUCCESS:
                           newLevel--;
                           break;
                        case UNLOADED_CHUNK:
                           return BlockTickStrategy.WAIT_FOR_ADJACENT_CHUNK_LOAD;
                     }
                  }
               }
            }

            if (spreadDownhill) {
               break;
            }
         }

         if (newLevel == fluidLevel) {
            return BlockTickStrategy.SLEEP;
         } else {
            return !this.drainFromTopBlock(accessor, fluidSection, blockSection, worldX, worldY, worldZ, fluid, fluidId, (byte)(fluidLevel - newLevel))
               ? BlockTickStrategy.WAIT_FOR_ADJACENT_CHUNK_LOAD
               : BlockTickStrategy.CONTINUE;
         }
      }
   }

   @Nullable
   private FiniteFluidTicker.SpreadOutcome spreadToOffset(
      @Nonnull FluidTicker.Accessor accessor,
      FluidSection fluidSection,
      BlockSection blockSection,
      @Nonnull Vector2i offset,
      int worldX,
      int worldY,
      int worldZ,
      Fluid fluid,
      int fluidId,
      byte fluidLevel
   ) {
      if (!isOffsetConnected(accessor, blockSection, offset, worldX, worldY, worldZ)) {
         return null;
      } else {
         int x = offset.getX();
         int z = offset.getY();
         int blockX = worldX + x;
         int blockZ = worldZ + z;
         boolean isDifferentSection = !ChunkUtil.isSameChunkSection(worldX, worldY, worldZ, blockX, worldY, blockZ);
         FluidSection otherFluidSection = isDifferentSection ? accessor.getFluidSectionByBlock(blockX, worldY, blockZ) : fluidSection;
         BlockSection otherBlockSection = isDifferentSection ? accessor.getBlockSectionByBlock(blockX, worldY, blockZ) : blockSection;
         if (otherFluidSection != null && otherBlockSection != null) {
            int blockId = otherBlockSection.get(blockX, worldY, blockZ);
            BlockType blockType = BlockType.getAssetMap().getAsset(blockId);
            if (isSolid(blockType)) {
               return null;
            } else {
               int adjacentFluidId = otherFluidSection.getFluidId(blockX, worldY, blockZ);
               byte adjacentFluidLevel = otherFluidSection.getFluidLevel(blockX, worldY, blockZ);
               int newAdjacentFillLevel = 1;
               if (adjacentFluidId == 0 || adjacentFluidId == fluidId && adjacentFluidLevel < fluidLevel - 1) {
                  if (adjacentFluidId == fluidId) {
                     newAdjacentFillLevel = adjacentFluidLevel + 1;
                  }

                  if (otherFluidSection.setFluid(blockX, worldY, blockZ, fluidId, (byte)newAdjacentFillLevel)) {
                     setTickingSurrounding(accessor, otherBlockSection, blockX, worldY, blockZ);
                     return FiniteFluidTicker.SpreadOutcome.SUCCESS;
                  }
               }

               return null;
            }
         } else {
            return FiniteFluidTicker.SpreadOutcome.UNLOADED_CHUNK;
         }
      }
   }

   private boolean drainFromTopBlock(
      @Nonnull FluidTicker.Accessor accessor,
      @Nonnull FluidSection fluidSection,
      BlockSection blockSection,
      int worldX,
      int worldY,
      int worldZ,
      @Nonnull Fluid fluid,
      int fluidId,
      byte drainLevels
   ) {
      int topY = this.getTopY(accessor, fluidSection, worldX, worldY, worldZ, fluid, fluidId);
      boolean isDifferentSection = fluidSection.getY() != ChunkUtil.chunkCoordinate(topY);
      FluidSection topFluidSection = isDifferentSection ? accessor.getFluidSectionByBlock(worldX, topY, worldZ) : fluidSection;
      BlockSection topBlockSection = isDifferentSection ? accessor.getBlockSectionByBlock(worldX, topY, worldZ) : blockSection;
      if (topFluidSection != null && topBlockSection != null) {
         byte topBlockFillLevels = topFluidSection.getFluidLevel(worldX, topY, worldZ);
         if (topBlockFillLevels > drainLevels) {
            setTickingSurrounding(accessor, topBlockSection, worldX, topY, worldZ);
            return topFluidSection.setFluid(worldX, topY, worldZ, fluidId, (byte)(topBlockFillLevels - drainLevels));
         } else if (topBlockFillLevels == drainLevels) {
            setTickingSurrounding(accessor, topBlockSection, worldX, topY, worldZ);
            return topFluidSection.setFluid(worldX, topY, worldZ, 0, (byte)0);
         } else {
            int nextY = topY;
            boolean updated = true;
            FluidSection nextFluidSection = topFluidSection;

            for (BlockSection nextBlockSection = topBlockSection; drainLevels > 0; nextY--) {
               boolean isDifferent = ChunkUtil.chunkCoordinate(nextY) != nextFluidSection.getY();
               if (isDifferent) {
                  nextFluidSection = accessor.getFluidSectionByBlock(worldX, nextY, worldZ);
                  nextBlockSection = accessor.getBlockSectionByBlock(worldX, nextY, worldZ);
                  if (nextFluidSection == null || nextBlockSection == null) {
                     return false;
                  }
               }

               int nextFluidLevel = nextFluidSection.getFluidLevel(worldX, nextY, worldZ);
               int transferLevels = Math.min(nextFluidLevel, drainLevels);
               if (transferLevels == nextFluidLevel) {
                  updated &= nextFluidSection.setFluid(worldX, nextY, worldZ, 0, (byte)0);
               } else {
                  updated &= nextFluidSection.setFluid(worldX, nextY, worldZ, fluidId, (byte)nextFluidLevel);
                  setTickingSurrounding(accessor, nextBlockSection, worldX, nextY, worldZ);
               }

               drainLevels -= (byte)transferLevels;
            }

            return updated;
         }
      } else {
         return false;
      }
   }

   private int getTopY(
      @Nonnull FluidTicker.Accessor accessor, @Nonnull FluidSection fluidSection, int worldX, int worldY, int worldZ, @Nonnull Fluid fluid, int fluidId
   ) {
      int topY = worldY;
      FluidSection aboveFluidSection = fluidSection.getY() != ChunkUtil.chunkCoordinate(worldY + 1)
         ? accessor.getFluidSectionByBlock(worldX, worldY + 1, worldZ)
         : fluidSection;

      while (true) {
         if (fluidSection.getY() != ChunkUtil.chunkCoordinate(topY)) {
            fluidSection = accessor.getFluidSectionByBlock(worldX, topY, worldZ);
         }

         if (aboveFluidSection.getY() != ChunkUtil.chunkCoordinate(topY + 1)) {
            aboveFluidSection = accessor.getFluidSectionByBlock(worldX, topY + 1, worldZ);
         }

         if (fluidSection.getFluidLevel(worldX, topY, worldZ) != fluid.getMaxFluidLevel() || aboveFluidSection.getFluidId(worldX, topY + 1, worldZ) != fluidId) {
            return topY;
         }

         topY++;
      }
   }

   private static boolean isOffsetConnected(
      @Nonnull FluidTicker.Accessor accessor, BlockSection blockSection, @Nonnull Vector2i offset, int worldX, int worldY, int worldZ
   ) {
      int x = offset.getX();
      int z = offset.getY();
      if (x != 0 && z != 0) {
         BlockSection section1 = ChunkUtil.isSameChunkSection(worldX, worldY, worldZ, worldX + x, worldY, worldZ)
            ? blockSection
            : accessor.getBlockSection(worldX + x, worldY, worldZ);
         BlockSection section2 = ChunkUtil.isSameChunkSection(worldX, worldY, worldZ, worldX, worldY, worldZ + z)
            ? blockSection
            : accessor.getBlockSection(worldX, worldY, worldZ + z);
         if (section1 != null && section2 != null) {
            int block1 = section1.get(worldX + x, worldY, worldZ);
            int block2 = section2.get(worldX, worldY, worldZ + z);
            return block1 == 0 || block2 == 0 || !isSolid(BlockType.getAssetMap().getAsset(block1)) || !isSolid(BlockType.getAssetMap().getAsset(block2));
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   static {
      List<Vector2i[]> offsets = List.of(ORTO_OFFSETS, DIAG_OFFSETS);
      Random random = new Random(51966L);

      for (int i = 0; i < 16; i++) {
         ObjectArrayList<Vector2i[]> offsetLists = new ObjectArrayList();

         for (Vector2i[] offset : offsets) {
            List<Vector2i> offsetArray = Arrays.asList(offset);
            Collections.shuffle(offsetArray, random);
            offsetLists.add(offsetArray.toArray(Vector2i[]::new));
         }

         OFFSETS_LISTS.add(offsetLists);
      }
   }

   private static enum SpreadOutcome {
      SUCCESS,
      UNLOADED_CHUNK;
   }
}
