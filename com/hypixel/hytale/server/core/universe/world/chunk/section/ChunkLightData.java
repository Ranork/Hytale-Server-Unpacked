package com.hypixel.hytale.server.core.universe.world.chunk.section;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.server.core.util.io.MemorySegmentUtil;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.foreign.ValueLayout.OfByte;
import java.lang.foreign.ValueLayout.OfShort;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChunkLightData {
   protected static final OfByte VL_BYTE = ValueLayout.JAVA_BYTE;
   protected static final OfShort VL_SHORT = ValueLayout.JAVA_SHORT_UNALIGNED;
   public static final ChunkLightData EMPTY = new ChunkLightData(null, (short)0);
   public static final int TREE_SIZE = 8;
   public static final int TREE_MASK = 7;
   public static final int DEPTH_MAGIC = 12;
   public static final int SIZE_MAGIC = 17;
   public static final int INITIAL_CAPACITY = 128;
   public static final byte MAX_VALUE = 15;
   public static final int CHANNEL_COUNT = 4;
   public static final int BITS_PER_CHANNEL = 4;
   public static final int CHANNEL_MASK = 15;
   public static final int RED_CHANNEL = 0;
   public static final int GREEN_CHANNEL = 1;
   public static final int BLUE_CHANNEL = 2;
   public static final int SKY_CHANNEL = 3;
   public static final int RED_CHANNEL_BIT = 0;
   public static final int GREEN_CHANNEL_BIT = 4;
   public static final int BLUE_CHANNEL_BIT = 8;
   public static final int SKY_CHANNEL_BIT = 12;
   public static final int RGB_MASK = -61441;
   protected final short changeId;
   @Nonnull
   final Arena arena;
   @Nullable
   MemorySegment lightData = null;

   public ChunkLightData(@Nullable MemorySegment lightData, short changeId) {
      this.arena = Arena.ofAuto();
      if (lightData != null) {
         this.lightData = this.arena.allocate(lightData.byteSize());
         this.lightData.copyFrom(lightData);
      }

      this.changeId = changeId;
   }

   protected ChunkLightData(@Nonnull Arena arena, @Nullable MemorySegment lightData, short changeId) {
      assert lightData == null || lightData.scope() == arena.scope();

      this.arena = arena;
      this.lightData = lightData;
      this.changeId = changeId;
   }

   public short getChangeId() {
      return this.changeId;
   }

   public byte getRedBlockLight(int x, int y, int z) {
      return this.getRedBlockLight(ChunkUtil.indexBlock(x, y, z));
   }

   public byte getRedBlockLight(int index) {
      return this.lightData == null ? 0 : this.getLight(index, 0);
   }

   public byte getGreenBlockLight(int x, int y, int z) {
      return this.getGreenBlockLight(ChunkUtil.indexBlock(x, y, z));
   }

   public byte getGreenBlockLight(int index) {
      return this.lightData == null ? 0 : this.getLight(index, 1);
   }

   public byte getBlueBlockLight(int x, int y, int z) {
      return this.getBlueBlockLight(ChunkUtil.indexBlock(x, y, z));
   }

   public byte getBlueBlockLight(int index) {
      return this.lightData == null ? 0 : this.getLight(index, 2);
   }

   public byte getBlockLightIntensity(int x, int y, int z) {
      return this.getBlockLightIntensity(ChunkUtil.indexBlock(x, y, z));
   }

   public byte getBlockLightIntensity(int index) {
      if (this.lightData == null) {
         return 0;
      } else {
         byte r = this.getLight(index, 0);
         byte g = this.getLight(index, 1);
         byte b = this.getLight(index, 2);
         return (byte)(MathUtil.maxValue(b, g, r) & 15);
      }
   }

   public short getBlockLight(int x, int y, int z) {
      return this.getBlockLight(ChunkUtil.indexBlock(x, y, z));
   }

   public short getBlockLight(int index) {
      return this.lightData == null ? 0 : (short)(this.getLightRaw(index) & -61441);
   }

   public byte getSkyLight(int x, int y, int z) {
      return this.getSkyLight(ChunkUtil.indexBlock(x, y, z));
   }

   public byte getSkyLight(int index) {
      return this.lightData == null ? 0 : this.getLight(index, 3);
   }

   public byte getLight(int index, int channel) {
      if (channel < 0 || channel >= 4) {
         throw new IllegalArgumentException();
      } else if (this.lightData == null) {
         return 0;
      } else {
         short value = this.getLightRaw(index);
         return (byte)(value >> channel * 4 & 15);
      }
   }

   public short getLightRaw(int x, int y, int z) {
      return this.getLightRaw(ChunkUtil.indexBlock(x, y, z));
   }

   public short getLightRaw(int index) {
      if (this.lightData == null) {
         return 0;
      } else if (index >= 0 && index < 32768) {
         return getTraverse(this.lightData, index, 0, 0);
      } else {
         throw new IllegalArgumentException("Index " + index + " is outside of the bounds!");
      }
   }

   protected static short getTraverse(@Nonnull MemorySegment local, int index, int pointer, int depth) {
      int loc = -1;
      int result = -1;

      try {
         int position = pointer * 17;
         byte mask = local.get(VL_BYTE, (long)position);
         int innerIndex = index >> 12 - depth & 7;
         loc = innerIndex * 2 + position + 1;
         result = Short.toUnsignedInt(local.get(VL_SHORT, (long)loc));
         return (mask >> innerIndex & 1) == 1 ? getTraverse(local, index, result, depth + 3) : (short)result;
      } catch (Throwable var9) {
         throw new RuntimeException("Failed with " + index + ", " + pointer + ", " + depth + ". Result: " + result + " from " + loc, var9);
      }
   }

   public int serialize(@Nonnull MemorySegment data, int offset) {
      data.set(MemorySegmentUtil.SHORT_BE, (long)offset, this.changeId);
      boolean hasLight = this.lightData != null;
      data.set(ValueLayout.JAVA_BOOLEAN, offset + 2, hasLight);
      if (hasLight) {
         int headerEnd = offset + 2 + 1 + 4;
         int size = this.serializeOctree(data, headerEnd, 0);
         data.set(MemorySegmentUtil.INT_BE, (long)(offset + 2 + 1), size);
         return 7 + size;
      } else {
         return 3;
      }
   }

   private int serializeOctree(@Nonnull MemorySegment data, int baseOffset, int position) {
      int mask = this.lightData.get(VL_BYTE, position * 17L);
      data.set(ValueLayout.JAVA_BYTE, (long)baseOffset, (byte)mask);
      int offset = baseOffset + 1;

      for (int i = 0; i < 8; i++) {
         int val = Short.toUnsignedInt(this.lightData.get(VL_SHORT, (long)(position * 17) + i * 2 + 1L));
         if ((mask >> i & 1) == 1) {
            offset += this.serializeOctree(data, offset, val);
         } else {
            data.set(MemorySegmentUtil.SHORT_BE, (long)offset, (short)val);
            offset += 2;
         }
      }

      return offset - baseOffset;
   }

   private int octreeSerializedSize() {
      return 15 * (int)(this.lightData.byteSize() / 17L) + 2;
   }

   public int serializedByteSize() {
      return this.lightData == null ? 3 : 7 + this.octreeSerializedSize();
   }

   public int serializedForPacketByteSize() {
      return this.lightData == null ? 1 : 1 + this.octreeSerializedSize();
   }

   public int serializeForPacket(@Nonnull MemorySegment data, int offset) {
      boolean hasLight = this.lightData != null;
      data.set(ValueLayout.JAVA_BOOLEAN, offset, hasLight);
      return hasLight ? 1 + this.serializeOctreeForPacket(data, offset + 1, 0) : 1;
   }

   private int serializeOctreeForPacket(@Nonnull MemorySegment data, int baseOffset, int position) {
      int mask = this.lightData.get(VL_BYTE, position * 17L);
      data.set(ValueLayout.JAVA_BYTE, (long)baseOffset, (byte)mask);
      int offset = baseOffset + 1;

      for (int i = 0; i < 8; i++) {
         int val = Short.toUnsignedInt(this.lightData.get(VL_SHORT, (long)(position * 17) + i * 2 + 1L));
         if ((mask >> i & 1) == 1) {
            offset += this.serializeOctreeForPacket(data, offset, val);
         } else {
            data.set(MemorySegmentUtil.SHORT_LE, (long)offset, (short)val);
            offset += 2;
         }
      }

      return offset - baseOffset;
   }

   @Nonnull
   public static ChunkLightData deserialize(@Nonnull MemorySegment data, int offset) {
      short changeId = data.get(MemorySegmentUtil.SHORT_BE, (long)offset);
      boolean hasLight = data.get(ValueLayout.JAVA_BOOLEAN, (long)(offset + 2));
      ChunkLightData chunkLightData;
      if (hasLight) {
         int length = data.get(MemorySegmentUtil.INT_BE, (long)(offset + 2 + 1));
         int from = offset + 2 + 1 + 4;
         int segments = (length - 2) / 15;
         Arena arena = Arena.ofAuto();
         MemorySegment lightData = arena.allocate(segments * 17L);
         deserializeOctree(data, from, lightData, 0, 0);
         chunkLightData = new ChunkLightData(arena, lightData, changeId);
      } else {
         chunkLightData = new ChunkLightData(null, changeId);
      }

      return chunkLightData;
   }

   private static long deserializeOctree(@Nonnull MemorySegment from, int baseOffset, @Nonnull MemorySegment to, int position, int segmentIndex) {
      int mask = from.get(ValueLayout.JAVA_BYTE, (long)baseOffset);
      int offset = baseOffset + 1;
      to.set(VL_BYTE, position * 17L, (byte)mask);

      for (int i = 0; i < 8; i++) {
         int val;
         if ((mask >> i & 1) == 1) {
            val = ++segmentIndex;
            long result = deserializeOctree(from, offset, to, segmentIndex, segmentIndex);
            offset += (int)(result >>> 32);
            segmentIndex = (int)result;
         } else {
            val = from.get(MemorySegmentUtil.SHORT_BE, (long)offset);
            offset += 2;
         }

         to.set(VL_SHORT, position * 17L + i * 2 + 1L, (short)val);
      }

      return (long)(offset - baseOffset) << 32 | segmentIndex & 4294967295L;
   }

   @Nonnull
   public String octreeToString() {
      return this.lightData == null ? "NULL" : ChunkLightDataBuilder.octreeToString(this.lightData);
   }

   public static short combineLightValues(byte red, byte green, byte blue, byte sky) {
      return (short)(sky << 12 | blue << 8 | green << 4 | red << 0);
   }

   public static short combineLightValues(byte red, byte green, byte blue) {
      return (short)(blue << 8 | green << 4 | red << 0);
   }

   public static byte getLightValue(short value, int channel) {
      return (byte)(value >> channel * 4 & 15);
   }
}
