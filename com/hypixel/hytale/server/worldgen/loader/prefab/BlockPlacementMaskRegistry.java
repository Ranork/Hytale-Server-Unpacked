package com.hypixel.hytale.server.worldgen.loader.prefab;

import com.hypixel.hytale.server.worldgen.loader.util.FileMaskCache;
import com.hypixel.hytale.server.worldgen.util.ResolvedBlockArray;
import com.hypixel.hytale.server.worldgen.util.condition.BlockMaskCondition;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

public class BlockPlacementMaskRegistry extends FileMaskCache<BlockMaskCondition> {
   @Nonnull
   private final Map<BlockMaskCondition, BlockMaskCondition> masks = new HashMap<>();
   @Nonnull
   private final Map<BlockMaskCondition.MaskEntry, BlockMaskCondition.MaskEntry> entries = new HashMap<>();
   private BlockMaskCondition tempMask = new BlockMaskCondition();
   private BlockMaskCondition.MaskEntry tempEntry = new BlockMaskCondition.MaskEntry();

   @Nonnull
   public BlockMaskCondition retainOrAllocateMask(@Nonnull BlockMaskCondition.Mask defaultMask, @Nonnull Long2ObjectMap<BlockMaskCondition.Mask> specificMasks) {
      BlockMaskCondition mask = this.tempMask;
      mask.set(defaultMask, specificMasks);
      BlockMaskCondition old = this.masks.putIfAbsent(mask, mask);
      if (old != null) {
         return old;
      } else {
         this.tempMask = new BlockMaskCondition();
         return mask;
      }
   }

   @Nonnull
   public BlockMaskCondition.MaskEntry retainOrAllocateEntry(@Nonnull ResolvedBlockArray blocks, boolean replace) {
      BlockMaskCondition.MaskEntry entry = this.tempEntry;
      entry.set(blocks, replace);
      BlockMaskCondition.MaskEntry old = this.entries.putIfAbsent(entry, entry);
      if (old != null) {
         return old;
      } else {
         this.tempEntry = new BlockMaskCondition.MaskEntry();
         return entry;
      }
   }
}
