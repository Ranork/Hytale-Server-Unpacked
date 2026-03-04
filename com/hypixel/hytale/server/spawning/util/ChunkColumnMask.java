package com.hypixel.hytale.server.spawning.util;

import com.hypixel.hytale.common.util.BitSetUtil;
import com.hypixel.hytale.math.util.ChunkUtil;
import java.util.BitSet;
import javax.annotation.Nonnull;

public class ChunkColumnMask {
   public static final int COLUMNS = 1024;
   private final BitSet columns = new BitSet(1024);

   public void copyFrom(@Nonnull ChunkColumnMask src) {
      BitSetUtil.copyValues(src.columns, this.columns);
   }

   public boolean isEmpty() {
      return this.columns.isEmpty();
   }

   public void clear() {
      this.columns.clear();
   }

   public void set() {
      this.columns.set(0, 1024);
   }

   public boolean get(int x, int z) {
      return this.columns.get(ChunkUtil.indexColumn(x, z));
   }

   public void set(int x, int z) {
      this.columns.set(ChunkUtil.indexColumn(x, z));
   }

   public void clear(int x, int z) {
      this.columns.clear(ChunkUtil.indexColumn(x, z));
   }

   public void clear(int index) {
      this.columns.clear(index & 1023);
   }

   public void set(int x, int z, boolean value) {
      this.columns.set(ChunkUtil.indexColumn(x, z), value);
   }

   public boolean get(int index) {
      return this.columns.get(index & 1023);
   }

   public void set(int bitIndex) {
      this.columns.set(bitIndex & 1023);
   }

   public int nextSetBit(int fromIndex) {
      return this.columns.nextSetBit(fromIndex & 1023);
   }

   public int nextClearBit(int fromIndex) {
      return this.columns.nextClearBit(fromIndex & 1023);
   }

   public int previousSetBit(int fromIndex) {
      return this.columns.previousSetBit(fromIndex & 1023);
   }

   public int previousClearBit(int fromIndex) {
      return this.columns.previousClearBit(fromIndex & 1023);
   }

   public int cardinality() {
      return this.columns.cardinality();
   }
}
