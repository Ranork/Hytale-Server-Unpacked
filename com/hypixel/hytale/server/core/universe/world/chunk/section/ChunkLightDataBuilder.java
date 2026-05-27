package com.hypixel.hytale.server.core.universe.world.chunk.section;

import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.math.util.ChunkUtil;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.BitSet;
import java.util.HexFormat;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChunkLightDataBuilder extends ChunkLightData {
   static boolean DEBUG = false;
   protected BitSet currentSegments;

   public ChunkLightDataBuilder(short changeId) {
      super(Arena.ofAuto(), null, changeId);
   }

   public ChunkLightDataBuilder(@Nonnull ChunkLightData lightData, short changeId) {
      super(lightData.lightData, changeId);
      if (lightData instanceof ChunkLightDataBuilder) {
         throw new IllegalArgumentException("ChunkLightDataBuilder light data isn't compacted so we can't read this cleanly atm");
      } else {
         if (this.lightData != null) {
            int segmentCount = (int)(this.lightData.byteSize() / 17L);
            this.currentSegments = new BitSet(segmentCount);
            this.currentSegments.set(0, segmentCount);
         }
      }
   }

   public void setBlockLight(int x, int y, int z, byte red, byte green, byte blue) {
      this.setBlockLight(ChunkUtil.indexBlock(x, y, z), red, green, blue);
   }

   public void setBlockLight(int index, byte red, byte green, byte blue) {
      byte sky = this.getLight(index, 3);
      this.setLightRaw(index, combineLightValues(red, green, blue, sky));
   }

   public void setSkyLight(int x, int y, int z, byte light) {
      this.setSkyLight(ChunkUtil.indexBlock(x, y, z), light);
   }

   public void setSkyLight(int index, byte light) {
      this.setLight(index, 3, light);
   }

   public void setLight(int index, int channel, byte value) {
      if (channel >= 0 && channel < 4) {
         int current = this.getLightRaw(index);
         current &= ~(15 << channel * 4);
         current |= (value & 15) << channel * 4;
         this.setLightRaw(index, (short)current);
      } else {
         throw new IllegalArgumentException();
      }
   }

   public void setLightRaw(int index, short value) {
      if (index >= 0 && index < 32768) {
         if (this.lightData == null) {
            this.lightData = this.arena.allocate(2176L);
         }

         if (this.currentSegments == null) {
            this.currentSegments = new BitSet();
            this.currentSegments.set(0);
         }

         this.setTraverse(this.currentSegments, index, 0, 0, value);
      } else {
         throw new IllegalArgumentException("Index " + index + " is outside of the bounds!");
      }
   }

   @Nonnull
   public ChunkLightData build() {
      if (this.lightData == null) {
         return new ChunkLightData(null, this.changeId);
      } else {
         Arena arena = Arena.ofAuto();
         int cardinality = this.currentSegments.cardinality();
         MemorySegment buffer = arena.allocate(cardinality * 17L);
         if (cardinality == this.currentSegments.length()) {
            MemorySegment.copy(this.lightData, 0L, buffer, 0L, buffer.byteSize());
            return new ChunkLightData(arena, buffer, this.changeId);
         } else {
            this.serializeOctree(buffer, 0, 0);
            return new ChunkLightData(arena, buffer, this.changeId);
         }
      }
   }

   private int serializeOctree(@Nonnull MemorySegment to, int position, int segmentIndex) {
      int toPosition = segmentIndex;
      int mask = this.lightData.get(VL_BYTE, position * 17L);
      to.set(VL_BYTE, segmentIndex * 17L, (byte)mask);

      for (int i = 0; i < 8; i++) {
         int val = Short.toUnsignedInt(this.lightData.get(VL_SHORT, position * 17L + i * 2 + 1L));
         if ((mask >> i & 1) == 1) {
            segmentIndex++;
            int from = val;
            val = segmentIndex;
            segmentIndex = this.serializeOctree(to, from, segmentIndex);
         }

         to.set(VL_SHORT, toPosition * 17L + i * 2 + 1L, (short)val);
      }

      return segmentIndex;
   }

   @Nullable
   private ChunkLightDataBuilder.Res setTraverse(@Nonnull BitSet currentSegments, int index, int pointer, int depth, short value) {
      int headerLocation = pointer * 17;
      byte i = this.lightData.get(VL_BYTE, (long)headerLocation);
      int innerIndex = index >> 12 - depth & 7;
      int position = innerIndex * 2 + headerLocation + 1;
      short currentValue = this.lightData.get(VL_SHORT, (long)position);

      try {
         if ((i >> innerIndex & 1) == 1) {
            return this.setTraverseBranch(currentSegments, index, pointer, depth, value, headerLocation, i, innerIndex, position, currentValue);
         } else {
            return value != currentValue
               ? this.setTraverseLeaf(currentSegments, index, pointer, depth, value, headerLocation, i, innerIndex, position, currentValue)
               : null;
         }
      } catch (Throwable var12) {
         throw new RuntimeException(
            "Failed to setTraverse("
               + index
               + ", "
               + pointer
               + ", "
               + depth
               + ", "
               + value
               + ") with i "
               + i
               + ", innerIndex "
               + innerIndex
               + ", position "
               + position
               + ", currentValue "
               + currentValue,
            var12
         );
      }
   }

   @Nullable
   private ChunkLightDataBuilder.Res setTraverseBranch(
      @Nonnull BitSet currentSegments,
      int index,
      int pointer,
      int depth,
      short value,
      int headerLocation,
      byte i,
      int innerIndex,
      int position,
      short currentValue
   ) {
      int currentValueMasked = currentValue & '\uffff';
      if (depth == 12) {
         throw new RuntimeException(
            "Discovered branch at deepest point in octree! Mask "
               + i
               + " innerIndex "
               + innerIndex
               + " depth "
               + depth
               + " setValue "
               + value
               + " currentValue "
               + currentValue
               + " at index "
               + index
               + " pointer "
               + pointer
         );
      } else {
         if (this.setTraverse(currentSegments, index, currentValueMasked, depth + 3, value) != null) {
            currentSegments.clear(currentValueMasked);
            this.lightData.set(VL_SHORT, (long)position, value);
            int mask = ~(1 << innerIndex);
            i = (byte)(i & mask);
            this.lightData.set(VL_BYTE, (long)headerLocation, i);
            if (i == 0 && this.allLeavesEqual(headerLocation, value)) {
               return ChunkLightDataBuilder.Res.INSTANCE;
            }
         }

         return null;
      }
   }

   @Nullable
   private ChunkLightDataBuilder.Res setTraverseLeaf(
      @Nonnull BitSet currentSegments,
      int index,
      int pointer,
      int depth,
      short value,
      int headerLocation,
      byte i,
      int innerIndex,
      int position,
      short currentValue
   ) {
      if (depth > 12) {
         throw new IllegalStateException(
            "Somehow have invalid octree state: "
               + octreeToString(this.lightData)
               + " when setTraverse("
               + index
               + ", "
               + pointer
               + ", "
               + depth
               + ", "
               + value
               + ");"
         );
      } else if (depth == 12) {
         byte[] bytes = null;
         if (DEBUG) {
            bytes = this.lightData.asSlice(headerLocation, 17L).toArray(ValueLayout.JAVA_BYTE);
         }

         this.lightData.set(VL_SHORT, (long)position, value);
         if (!this.allLeavesEqual(headerLocation, value)) {
            return null;
         } else {
            return DEBUG ? new ChunkLightDataBuilder.Res(HexFormat.of().formatHex(bytes)) : ChunkLightDataBuilder.Res.INSTANCE;
         }
      } else {
         i = (byte)(i | 1 << innerIndex);
         this.lightData.set(VL_BYTE, (long)headerLocation, i);
         int newSegmentIndex = this.growSegment(currentSegments, currentValue);
         this.lightData.set(VL_SHORT, (long)position, (short)newSegmentIndex);
         ChunkLightDataBuilder.Res out = this.setTraverse(currentSegments, index, newSegmentIndex, depth + 3, value);
         if (out != null) {
            throw new RuntimeException(
               "Created new segment that instantly collapsed with ("
                  + index
                  + ", "
                  + pointer
                  + ", "
                  + depth
                  + ", "
                  + value
                  + "): with currentValue mismatch "
                  + currentValue
                  + " res "
                  + out
            );
         } else {
            return null;
         }
      }
   }

   private boolean allLeavesEqual(int headerLocation, short value) {
      for (int j = 0; j < 8; j++) {
         short s = this.lightData.get(VL_SHORT, (long)(j * 2) + headerLocation + 1L);
         if (s != value) {
            return false;
         }
      }

      return true;
   }

   protected int growSegment(@Nonnull BitSet currentSegments, short val) {
      int newSegmentIndex = currentSegments.nextClearBit(0);
      currentSegments.set(newSegmentIndex);
      long currentCapacity = this.lightData.byteSize();
      long newSize = (newSegmentIndex + 1) * 17L;
      if (currentCapacity < newSize) {
         int newCap = Math.max(ArrayUtil.grow((int)currentCapacity), (int)newSize);
         MemorySegment old = this.lightData;
         this.lightData = this.arena.allocate(newCap);
         this.lightData.copyFrom(old);
      }

      this.lightData.set(VL_BYTE, newSegmentIndex * 17L, (byte)0);

      for (int j = 0; j < 8; j++) {
         this.lightData.set(VL_SHORT, newSegmentIndex * 17L + j * 2 + 1L, val);
      }

      return newSegmentIndex;
   }

   @Nonnull
   public String toStringOctree() {
      return this.lightData == null ? "NULL" : octreeToString(this.lightData);
   }

   @Nonnull
   public static String octreeToString(@Nonnull MemorySegment buffer) {
      StringBuffer out = new StringBuffer();

      try {
         octreeToString(buffer, 0, out, 0);
      } catch (Throwable var3) {
         throw new RuntimeException("Failed at " + out, var3);
      }

      return out.toString();
   }

   public static void octreeToString(@Nonnull MemorySegment buffer, int pointer, @Nonnull StringBuffer out, int recursion) {
      int i = buffer.get(VL_BYTE, pointer * 17L);

      for (int j = 0; j < 8; j++) {
         int loc = pointer * 17 + j * 2 + 1;
         int s = Short.toUnsignedInt(buffer.get(VL_SHORT, (long)loc));
         out.append("\t".repeat(Math.max(0, recursion)));
         if ((i & 1 << j) != 0) {
            out.append("SUBTREE AT ").append(j).append('\n');
            octreeToString(buffer, s, out, recursion + 1);
         } else {
            out.append("INDEX ").append(j).append(" VALUE: ").append(s);
         }

         if (j != 7) {
            out.append('\n');
         }
      }
   }

   private static class Res {
      public static final ChunkLightDataBuilder.Res INSTANCE = new ChunkLightDataBuilder.Res(null);
      private final String segment;

      private Res(String segment) {
         this.segment = segment;
      }

      @Nonnull
      @Override
      public String toString() {
         return "Res{segment='" + this.segment + "'}";
      }
   }
}
