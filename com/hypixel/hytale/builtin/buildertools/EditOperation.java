package com.hypixel.hytale.builtin.buildertools;

import com.hypixel.hytale.builtin.buildertools.utils.Material;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.prefab.selection.mask.BlockMask;
import com.hypixel.hytale.server.core.prefab.selection.standard.BlockSelection;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.accessor.BlockAccessor;
import com.hypixel.hytale.server.core.universe.world.accessor.LocalCachedChunkAccessor;
import com.hypixel.hytale.server.core.universe.world.accessor.OverridableChunkAccessor;
import javax.annotation.Nonnull;

public class EditOperation {
   private final BlockMask blockMask;
   @Nonnull
   private final OverridableChunkAccessor accessor;
   @Nonnull
   private final BlockSelection before;
   @Nonnull
   private final BlockSelection after;
   private final Vector3i min;
   private final Vector3i max;

   public EditOperation(@Nonnull World world, int x, int y, int z, int editRange, Vector3i min, Vector3i max, BlockMask blockMask) {
      this.blockMask = blockMask;
      this.accessor = LocalCachedChunkAccessor.atWorldCoords(world, x, z, editRange);
      this.min = min;
      this.max = max;
      this.before = new BlockSelection();
      this.before.setPosition(x, y, z);
      if (min != null && max != null) {
         this.before.setSelectionArea(min, max);
      }

      this.after = new BlockSelection(this.before);
   }

   public BlockMask getBlockMask() {
      return this.blockMask;
   }

   @Nonnull
   public BlockSelection getBefore() {
      return this.before;
   }

   @Nonnull
   public BlockSelection getAfter() {
      return this.after;
   }

   @Nonnull
   public OverridableChunkAccessor getAccessor() {
      return this.accessor;
   }

   public int getBlock(int x, int y, int z) {
      return this.accessor.getBlock(x, y, z);
   }

   public boolean setBlock(int x, int y, int z, int blockId) {
      return this.setBlock(x, y, z, blockId, 0);
   }

   public boolean setBlock(int x, int y, int z, int blockId, int rotation) {
      int currentBlock = this.getBlock(x, y, z);
      int currentFluid = this.getFluid(x, y, z);
      if (this.blockMask != null && this.blockMask.isExcluded(this.accessor, x, y, z, this.min, this.max, currentBlock, currentFluid)) {
         return false;
      } else {
         BlockAccessor blocks = this.accessor.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(x, z));
         if (blocks == null) {
            return false;
         } else {
            if (!this.before.hasBlockAtWorldPos(x, y, z)) {
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

            this.after.addBlockAtWorldPos(x, y, z, blockId, rotation, 0, 0);
            if (blockId == 0) {
               this.setFluid(x, y, z, 0, (byte)0);
            }

            return true;
         }
      }
   }

   private boolean setFluid(int x, int y, int z, int fluidId, byte fluidLevel) {
      BlockAccessor chunk = this.accessor.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(x, z));
      if (chunk == null) {
         return false;
      } else {
         int currentBlock = this.getBlock(x, y, z);
         int currentFluid = this.getFluid(x, y, z);
         if (this.blockMask != null && this.blockMask.isExcluded(this.accessor, x, y, z, this.min, this.max, currentBlock, currentFluid)) {
            return false;
         } else {
            int beforeFluid = this.before.getFluidAtWorldPos(x, y, z);
            if (beforeFluid < 0) {
               int originalFluidId = chunk.getFluidId(x, y, z);
               byte originalFluidLevel = chunk.getFluidLevel(x, y, z);
               this.before.addFluidAtWorldPos(x, y, z, originalFluidId, originalFluidLevel);
            }

            this.after.addFluidAtWorldPos(x, y, z, fluidId, fluidLevel);
            return true;
         }
      }
   }

   public int getFluid(int x, int y, int z) {
      BlockAccessor chunk = this.accessor.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(x, z));
      return chunk != null ? chunk.getFluidId(x, y, z) : 0;
   }

   public boolean setMaterial(int x, int y, int z, @Nonnull Material material) {
      return material.isFluid()
         ? this.setFluid(x, y, z, material.getFluidId(), material.getFluidLevel())
         : this.setBlock(x, y, z, material.getBlockId(), material.getRotation());
   }
}
