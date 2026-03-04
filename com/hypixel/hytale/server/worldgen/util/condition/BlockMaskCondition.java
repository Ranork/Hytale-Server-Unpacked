package com.hypixel.hytale.server.worldgen.util.condition;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.worldgen.util.ResolvedBlockArray;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BlockMaskCondition {
   public static final BlockMaskCondition.Mask DEFAULT_MASK = new BlockMaskCondition.Mask(true, new BlockMaskCondition.MaskEntry[0]);
   public static final BlockMaskCondition DEFAULT_TRUE = new BlockMaskCondition();
   public static final BlockMaskCondition DEFAULT_FALSE = new BlockMaskCondition();
   @Nonnull
   private BlockMaskCondition.Mask defaultMask = DEFAULT_MASK;
   @Nonnull
   private Long2ObjectMap<BlockMaskCondition.Mask> specificMasks = Long2ObjectMaps.emptyMap();

   public void set(@Nonnull BlockMaskCondition.Mask defaultMask, @Nonnull Long2ObjectMap<BlockMaskCondition.Mask> specificMasks) {
      this.defaultMask = defaultMask;
      this.specificMasks = specificMasks;
   }

   public boolean eval(int currentBlock, int currentFluid, int nextBlockId, int nextFluidId) {
      BlockMaskCondition.Mask mask = (BlockMaskCondition.Mask)this.specificMasks.getOrDefault(MathUtil.packLong(nextBlockId, nextFluidId), this.defaultMask);
      return mask.shouldReplace(currentBlock, currentFluid);
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         BlockMaskCondition that = (BlockMaskCondition)o;
         return !this.defaultMask.equals(that.defaultMask) ? false : Objects.equals(this.specificMasks, that.specificMasks);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.defaultMask.hashCode();
      return 31 * result + (this.specificMasks != null ? this.specificMasks.hashCode() : 0);
   }

   @Nonnull
   @Override
   public String toString() {
      return "BlockMaskCondition{defaultMask=" + this.defaultMask + ", specificMasks=" + this.specificMasks + "}";
   }

   public static class Mask {
      private final boolean matchEmpty;
      private final BlockMaskCondition.MaskEntry[] entries;

      public Mask(@Nonnull BlockMaskCondition.MaskEntry[] entries) {
         this(false, entries);
      }

      private Mask(boolean matchEmpty, @Nonnull BlockMaskCondition.MaskEntry[] entries) {
         this.entries = entries;
         this.matchEmpty = matchEmpty;
      }

      public boolean shouldReplace(int current, int fluid) {
         for (BlockMaskCondition.MaskEntry entry : this.entries) {
            if (entry.shouldHandle(current, fluid)) {
               return entry.shouldReplace();
            }
         }

         return this.matchEmpty && (current == 0 || fluid == 0);
      }

      @Override
      public boolean equals(@Nullable Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            BlockMaskCondition.Mask mask = (BlockMaskCondition.Mask)o;
            return Arrays.equals((Object[])this.entries, (Object[])mask.entries);
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         return Arrays.hashCode((Object[])this.entries);
      }

      @Nonnull
      @Override
      public String toString() {
         return "Mask{entries=" + Arrays.toString((Object[])this.entries) + "}";
      }
   }

   public static class MaskEntry {
      public static final BlockMaskCondition.MaskEntry WILDCARD_TRUE = new BlockMaskCondition.MaskEntry(true, true);
      public static final BlockMaskCondition.MaskEntry WILDCARD_FALSE = new BlockMaskCondition.MaskEntry(true, false);
      private ResolvedBlockArray blocks = ResolvedBlockArray.EMPTY;
      private final boolean any;
      private boolean replace;

      public MaskEntry() {
         this(false, false);
      }

      private MaskEntry(boolean any, boolean replace) {
         this.any = any;
         this.replace = replace;
      }

      public void set(ResolvedBlockArray blocks, boolean replace) {
         this.blocks = blocks;
         this.replace = replace;
      }

      public boolean shouldHandle(int current, int fluid) {
         return this.any || this.blocks.contains(current, fluid);
      }

      public boolean shouldReplace() {
         return this.replace;
      }

      @Override
      public boolean equals(@Nullable Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            BlockMaskCondition.MaskEntry that = (BlockMaskCondition.MaskEntry)o;
            if (this.any != that.any) {
               return false;
            } else {
               return this.replace != that.replace ? false : this.blocks.equals(that.blocks);
            }
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         int result = this.blocks.hashCode();
         result = 31 * result + (this.any ? 1 : 0);
         return 31 * result + (this.replace ? 1 : 0);
      }

      @Nonnull
      @Override
      public String toString() {
         return "MaskEntry{blocks=" + this.blocks + ", replace=" + this.replace + "}";
      }
   }
}
