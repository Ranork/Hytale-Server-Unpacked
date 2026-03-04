package com.hypixel.hytale.server.worldgen.util;

import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.procedurallib.property.NoiseProperty;
import com.hypixel.hytale.procedurallib.supplier.IDoubleCoordinateSupplier2d;
import com.hypixel.hytale.procedurallib.supplier.IDoubleRange;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NoiseBlockArray {
   public static final NoiseBlockArray EMPTY = new NoiseBlockArray(new NoiseBlockArray.Entry[0]);
   protected final NoiseBlockArray.Entry[] entries;

   public NoiseBlockArray(NoiseBlockArray.Entry[] entries) {
      this.entries = entries;
   }

   public NoiseBlockArray.Entry[] getEntries() {
      return this.entries;
   }

   public BlockFluidEntry getTopBlockAt(int seed, double x, double z) {
      for (int i = 0; i < this.entries.length; i++) {
         NoiseBlockArray.Entry entry = this.entries[i];
         int repetitions = entry.getRepetitions(seed, x, z);
         if (repetitions > 0) {
            return entry.blockEntry;
         }
      }

      return BlockFluidEntry.EMPTY;
   }

   public BlockFluidEntry getBottomBlockAt(int seed, double x, double z) {
      for (int i = this.entries.length - 1; i >= 0; i--) {
         NoiseBlockArray.Entry entry = this.entries[i];
         int repetitions = entry.getRepetitions(seed, x, z);
         if (repetitions > 0) {
            return entry.blockEntry;
         }
      }

      return BlockFluidEntry.EMPTY;
   }

   @Override
   public boolean equals(@Nullable Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         NoiseBlockArray that = (NoiseBlockArray)o;
         return Arrays.equals((Object[])this.entries, (Object[])that.entries);
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
      return "NoiseBlockArray{entries=" + Arrays.toString((Object[])this.entries) + "}";
   }

   public static class Entry {
      protected final String blockName;
      protected final BlockFluidEntry blockEntry;
      protected final IDoubleRange repetitions;
      @Nonnull
      protected final NoiseProperty noise;
      @Nonnull
      protected final IDoubleCoordinateSupplier2d noiseSupplier;

      public Entry(String blockName, BlockFluidEntry blockEntry, IDoubleRange repetitions, @Nonnull NoiseProperty noise) {
         this.blockName = blockName;
         this.blockEntry = blockEntry;
         this.repetitions = repetitions;
         this.noise = noise;
         this.noiseSupplier = noise::get;
      }

      public String getBlockName() {
         return this.blockName;
      }

      public BlockFluidEntry getBlockEntry() {
         return this.blockEntry;
      }

      public int getRepetitions(int seed, double x, double z) {
         return MathUtil.floor(this.repetitions.getValue(seed, x, z, this.noiseSupplier));
      }

      @Override
      public boolean equals(@Nullable Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            NoiseBlockArray.Entry entry = (NoiseBlockArray.Entry)o;
            if (this.blockEntry != entry.blockEntry) {
               return false;
            } else if (!this.blockName.equals(entry.blockName)) {
               return false;
            } else if (!this.repetitions.equals(entry.repetitions)) {
               return false;
            } else {
               return !this.noise.equals(entry.noise) ? false : this.noiseSupplier.equals(entry.noiseSupplier);
            }
         } else {
            return false;
         }
      }

      @Override
      public int hashCode() {
         int result = this.blockName.hashCode();
         result = 31 * result + this.blockEntry.hashCode();
         result = 31 * result + this.repetitions.hashCode();
         result = 31 * result + this.noise.hashCode();
         return 31 * result + this.noiseSupplier.hashCode();
      }

      @Nonnull
      @Override
      public String toString() {
         return "Entry{blockName='" + this.blockName + "', blockEntry=" + this.blockEntry + ", repetitions=" + this.repetitions + ", noise=" + this.noise + "}";
      }
   }
}
