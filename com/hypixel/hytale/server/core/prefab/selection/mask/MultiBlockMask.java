package com.hypixel.hytale.server.core.prefab.selection.mask;

import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.accessor.ChunkAccessor;
import javax.annotation.Nonnull;

public class MultiBlockMask extends BlockMask {
   private static final String BLOCK_MASK_SEPARATOR = ";";
   private final BlockMask[] masks;

   public MultiBlockMask(BlockMask[] masks) {
      super(BlockFilter.EMPTY_ARRAY);
      this.masks = masks;
   }

   @Override
   public boolean isExcluded(@Nonnull ChunkAccessor accessor, int x, int y, int z, Vector3i min, Vector3i max, int blockId) {
      return this.isExcluded(accessor, x, y, z, min, max, blockId, -1);
   }

   @Override
   public boolean isExcluded(@Nonnull ChunkAccessor accessor, int x, int y, int z, Vector3i min, Vector3i max, int blockId, int fluidId) {
      boolean excluded = false;

      for (BlockMask mask : this.masks) {
         if (mask.isExcluded(accessor, x, y, z, min, max, blockId, fluidId)) {
            excluded = true;
            break;
         }
      }

      return this.isInverted() != excluded;
   }

   @Nonnull
   @Override
   public String toString() {
      if (this.masks.length == 0) {
         return "-";
      } else {
         String base = joinElements(";", this.masks);
         return this.isInverted() ? "!" + base : base;
      }
   }

   @Nonnull
   @Override
   public String informativeToString() {
      if (this.masks.length == 0) {
         return "-";
      } else {
         StringBuilder builder = new StringBuilder();
         if (this.isInverted()) {
            builder.append("NOT(");
         }

         for (int i = 0; i < this.masks.length; i++) {
            BlockMask mask = this.masks[i];
            builder.append(mask.informativeToString());
            if (i != this.masks.length - 1) {
               builder.append(" AND ");
            }
         }

         if (this.isInverted()) {
            builder.append(")");
         }

         return builder.toString();
      }
   }
}
