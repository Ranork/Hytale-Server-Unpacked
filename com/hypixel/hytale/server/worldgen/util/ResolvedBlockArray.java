package com.hypixel.hytale.server.worldgen.util;

import com.hypixel.hytale.math.util.MathUtil;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Arrays;
import javax.annotation.Nonnull;

public final class ResolvedBlockArray {
   public static final ResolvedBlockArray EMPTY = new ResolvedBlockArray(BlockFluidEntry.EMPTY_ARRAY);
   public static final Long2ObjectMap<ResolvedBlockArray> RESOLVED_BLOCKS = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap());
   public static final Long2ObjectMap<ResolvedBlockArray> RESOLVED_BLOCKS_WITH_VARIANTS = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap());
   @Nonnull
   private final LongSet entrySet;
   @Nonnull
   private final BlockFluidEntry[] entries;

   public ResolvedBlockArray(@Nonnull BlockFluidEntry[] entries) {
      this.entries = entries;
      this.entrySet = new LongOpenHashSet();

      for (BlockFluidEntry entry : entries) {
         this.entrySet.add(MathUtil.packLong(entry.blockId(), entry.fluidId()));
      }
   }

   @Nonnull
   public BlockFluidEntry[] getEntries() {
      return this.entries;
   }

   @Nonnull
   public LongSet getEntrySet() {
      return this.entrySet;
   }

   public int size() {
      return this.entries.length;
   }

   public boolean contains(int block, int fluidId) {
      return this.entrySet.contains(MathUtil.packLong(block, fluidId));
   }

   @Override
   public boolean equals(Object o) {
      if (o != null && this.getClass() == o.getClass()) {
         ResolvedBlockArray that = (ResolvedBlockArray)o;
         return this.entrySet.equals(that.entrySet) && Arrays.deepEquals(this.entries, that.entries);
      } else {
         return false;
      }
   }

   @Override
   public int hashCode() {
      int result = this.entrySet.hashCode();
      return 31 * result + Arrays.hashCode((Object[])this.entries);
   }

   @Nonnull
   @Override
   public String toString() {
      return "ResolvedBlockArray{entrySet=" + this.entrySet + ", entries=" + Arrays.toString((Object[])this.entries) + "}";
   }
}
