package com.hypixel.hytale.builtin.buildertools.scriptedbrushes;

import com.hypixel.hytale.assetstore.map.BlockTypeAssetMap;
import com.hypixel.hytale.builtin.buildertools.BuilderToolsPlugin;
import com.hypixel.hytale.builtin.buildertools.utils.Material;
import com.hypixel.hytale.math.block.BlockUtil;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockMask;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap.Entry;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import javax.annotation.Nonnull;

public class BrushConfigEditStore {
   @Nonnull
   private final BrushConfig brushConfig;
   @Nonnull
   private final BrushConfigChunkAccessor accessor;
   @Nonnull
   private final BlockSelection before;
   @Nonnull
   private final BlockSelection previous;
   private BlockSelection current;
   private final LongOpenHashSet packedPlacedBlockPositions;

   public BrushConfigEditStore(LongOpenHashSet packedPlacedBlockPositions, @Nonnull BrushConfig brushConfig, World world) {
      this.brushConfig = brushConfig;
      this.packedPlacedBlockPositions = packedPlacedBlockPositions;
      Vector3i origin = brushConfig.getOrigin();
      int shapeWidth = brushConfig.getShapeWidth();
      int shapeHeight = brushConfig.getShapeHeight();
      int halfWidth = shapeWidth / 2;
      int halfHeight = shapeHeight / 2;
      this.accessor = BrushConfigChunkAccessor.atWorldCoords(this, world, origin.x, origin.z, shapeWidth * 2);
      this.before = new BlockSelection();
      this.before.setPosition(origin.x, origin.y, origin.z);
      this.before
         .setSelectionArea(
            new Vector3i(origin.x - halfWidth, origin.y - halfHeight, origin.z - halfWidth),
            new Vector3i(origin.x + halfWidth, origin.y + halfHeight, origin.z + halfWidth)
         );
      this.previous = new BlockSelection(this.before);
      this.current = new BlockSelection(this.before);
   }

   @Nonnull
   public BrushConfigChunkAccessor getAccessor() {
      return this.accessor;
   }

   public int getOriginalBlock(int x, int y, int z) {
      return this.accessor.getBlockIgnoringHistory(x, y, z);
   }

   public int getBlock(int x, int y, int z) {
      return this.previous.hasBlockAtWorldPos(x, y, z) ? this.previous.getBlockAtWorldPos(x, y, z) : this.getOriginalBlock(x, y, z);
   }

   public int getBlockIncludingCurrent(int x, int y, int z) {
      return this.current.hasBlockAtWorldPos(x, y, z) ? this.current.getBlockAtWorldPos(x, y, z) : this.getBlock(x, y, z);
   }

   public boolean setBlock(int x, int y, int z, int blockId) {
      boolean hasHistory = this.previous.hasBlockAtWorldPos(x, y, z) || this.previous.getFluidAtWorldPos(x, y, z) >= 0;
      switch (this.brushConfig.getHistoryMask()) {
         case Only:
            if (!hasHistory) {
               return false;
            }
            break;
         case Not:
            if (hasHistory) {
               return false;
            }
      }

      if (this.brushConfig.getRandom().nextInt(100) >= this.brushConfig.getDensity()) {
         return false;
      } else {
         if (this.getOriginalBlock(x, y, z) == 0) {
            this.packedPlacedBlockPositions.add(BlockUtil.pack(x, y, z));
         }

         int currentBlock = this.getBlock(x, y, z);
         int currentFluid = this.getFluid(x, y, z);
         BlockMask blockMask = this.brushConfig.getBlockMask();
         if (blockMask != null && blockMask.isExcluded(this.accessor, x, y, z, null, null, currentBlock, currentFluid)) {
            return false;
         } else {
            if (!this.before.hasBlockAtWorldPos(x, y, z)) {
               WorldChunk blocks = this.accessor.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(x, z));
               if (blocks != null) {
                  this.before
                     .addBlockAtWorldPos(
                        x,
                        y,
                        z,
                        currentBlock,
                        blocks.getRotationIndex(x, y, z),
                        blocks.getFiller(x, y, z),
                        blocks.getSupportValue(x, y, z),
                        blocks.getBlockComponentHolder(x, y, z)
                     );
               }
            }

            this.current.addBlockAtWorldPos(x, y, z, blockId, 0, 0, 0);
            return true;
         }
      }
   }

   private boolean setFluid(int x, int y, int z, int fluidId, byte fluidLevel) {
      boolean hasHistory = this.previous.hasBlockAtWorldPos(x, y, z) || this.previous.getFluidAtWorldPos(x, y, z) >= 0;
      switch (this.brushConfig.getHistoryMask()) {
         case Only:
            if (!hasHistory) {
               return false;
            }
            break;
         case Not:
            if (hasHistory) {
               return false;
            }
      }

      if (this.brushConfig.getRandom().nextInt(100) >= this.brushConfig.getDensity()) {
         return false;
      } else {
         int currentBlock = this.getBlock(x, y, z);
         int currentFluid = this.getFluid(x, y, z);
         BlockMask blockMask = this.brushConfig.getBlockMask();
         if (blockMask != null && blockMask.isExcluded(this.accessor, x, y, z, null, null, currentBlock, currentFluid)) {
            return false;
         } else {
            int beforeFluid = this.before.getFluidAtWorldPos(x, y, z);
            if (beforeFluid < 0) {
               WorldChunk chunk = this.accessor.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(x, z));
               if (chunk != null) {
                  int originalFluidId = chunk.getFluidId(x, y, z);
                  byte originalFluidLevel = chunk.getFluidLevel(x, y, z);
                  this.before.addFluidAtWorldPos(x, y, z, originalFluidId, originalFluidLevel);
               }
            }

            this.current.addFluidAtWorldPos(x, y, z, fluidId, fluidLevel);
            return true;
         }
      }
   }

   private int getOriginalFluid(int x, int y, int z) {
      WorldChunk chunk = this.accessor.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(x, z));
      return chunk != null ? chunk.getFluidId(x, y, z) : 0;
   }

   public int getFluid(int x, int y, int z) {
      int previousFluid = this.previous.getFluidAtWorldPos(x, y, z);
      return previousFluid >= 0 ? previousFluid : this.getOriginalFluid(x, y, z);
   }

   public boolean setMaterial(int x, int y, int z, @Nonnull Material material) {
      if (material.isFluid()) {
         return this.setFluid(x, y, z, material.getFluidId(), material.getFluidLevel());
      } else {
         boolean result = this.setBlock(x, y, z, material.getBlockId());
         if (result && material.isEmpty()) {
            this.setFluid(x, y, z, 0, (byte)0);
         }

         return result;
      }
   }

   @Nonnull
   public BuilderToolsPlugin.BuilderState.BlocksSampleData getBlockSampledataIncludingPreviousStages(int x, int y, int z, int radius) {
      BuilderToolsPlugin.BuilderState.BlocksSampleData data = new BuilderToolsPlugin.BuilderState.BlocksSampleData();
      Int2IntMap blockCounts = new Int2IntOpenHashMap();

      for (int ix = x - radius; ix <= x + radius; ix++) {
         for (int iz = z - radius; iz <= z + radius; iz++) {
            for (int iy = y - radius; iy <= y + radius; iy++) {
               int currentBlock = this.getBlock(ix, iy, iz);
               blockCounts.put(currentBlock, blockCounts.getOrDefault(currentBlock, 0) + 1);
            }
         }
      }

      BlockTypeAssetMap<String, BlockType> assetMap = BlockType.getAssetMap();
      ObjectIterator var14 = Int2IntMaps.fastIterable(blockCounts).iterator();

      while (var14.hasNext()) {
         Entry pair = (Entry)var14.next();
         int block = pair.getIntKey();
         int count = pair.getIntValue();
         if (count > data.mainBlockCount) {
            data.mainBlock = block;
            data.mainBlockCount = count;
         }

         BlockType blockType = assetMap.getAsset(block);
         if (count > data.mainBlockNotAirCount && block != 0) {
            data.mainBlockNotAir = block;
            data.mainBlockNotAirCount = count;
         }
      }

      return data;
   }

   public void flushCurrentEditsToPrevious() {
      this.previous.add(this.current);
      this.current = new BlockSelection();
      this.current.setPosition(this.brushConfig.getOrigin().x, this.brushConfig.getOrigin().y, this.brushConfig.getOrigin().z);
   }

   @Nonnull
   public BlockSelection getAfter() {
      return this.previous;
   }

   @Nonnull
   public BlockSelection getBefore() {
      return this.before;
   }
}
