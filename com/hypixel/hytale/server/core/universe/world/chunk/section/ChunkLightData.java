package com.hypixel.hytale.server.core.universe.world.chunk.section;

import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import javax.annotation.Nonnull;

public class ChunkLightData {
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
   ByteBuf light;

   public ChunkLightData(ByteBuf light, short changeId) {
      this.light = light;
      this.changeId = changeId;
   }

   public short getChangeId() {
      return this.changeId;
   }

   public byte getRedBlockLight(int x, int y, int z) {
      return this.getRedBlockLight(ChunkUtil.indexBlock(x, y, z));
   }

   public byte getRedBlockLight(int index) {
      return this.light == null ? 0 : this.getLight(index, 0);
   }

   public byte getGreenBlockLight(int x, int y, int z) {
      return this.getGreenBlockLight(ChunkUtil.indexBlock(x, y, z));
   }

   public byte getGreenBlockLight(int index) {
      return this.light == null ? 0 : this.getLight(index, 1);
   }

   public byte getBlueBlockLight(int x, int y, int z) {
      return this.getBlueBlockLight(ChunkUtil.indexBlock(x, y, z));
   }

   public byte getBlueBlockLight(int index) {
      return this.light == null ? 0 : this.getLight(index, 2);
   }

   public byte getBlockLightIntensity(int x, int y, int z) {
      return this.getBlockLightIntensity(ChunkUtil.indexBlock(x, y, z));
   }

   public byte getBlockLightIntensity(int index) {
      if (this.light == null) {
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
      return this.light == null ? 0 : (short)(this.getLightRaw(index) & -61441);
   }

   public byte getSkyLight(int x, int y, int z) {
      return this.getSkyLight(ChunkUtil.indexBlock(x, y, z));
   }

   public byte getSkyLight(int index) {
      return this.light == null ? 0 : this.getLight(index, 3);
   }

   public byte getLight(int index, int channel) {
      if (channel < 0 || channel >= 4) {
         throw new IllegalArgumentException();
      } else if (this.light == null) {
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
      if (this.light == null) {
         return 0;
      } else if (index >= 0 && index < 32768) {
         return getTraverse(this.light, index, 0, 0);
      } else {
         throw new IllegalArgumentException("Index " + index + " is outside of the bounds!");
      }
   }

   protected static short getTraverse(@Nonnull ByteBuf local, int index, int pointer, int depth) {
      int loc = -1;
      int result = -1;

      try {
         int position = pointer * 17;
         byte mask = local.getByte(position);
         int innerIndex = index >> 12 - depth & 7;
         loc = innerIndex * 2 + position + 1;
         result = local.getUnsignedShort(loc);
         return (mask >> innerIndex & 1) == 1 ? getTraverse(local, index, result, depth + 3) : (short)result;
      } catch (Throwable var9) {
         throw new RuntimeException("Failed with " + index + ", " + pointer + ", " + depth + ". Result: " + result + " from " + loc, var9);
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeShort(this.changeId);
      boolean hasLight = this.light != null;
      buf.writeBoolean(hasLight);
      if (hasLight) {
         buf.ensureWritable(this.light.readableBytes());
         int before = buf.writerIndex();
         buf.writeInt(0);
         this.serializeOctree(buf, 0);
         int after = buf.writerIndex();
         buf.writerIndex(before);
         buf.writeInt(after - before - 4);
         buf.writerIndex(after);
      }
   }

   private void serializeOctree(@Nonnull ByteBuf buf, int position) {
      int mask = this.light.getByte(position * 17);
      buf.writeByte(mask);

      for (int i = 0; i < 8; i++) {
         int val = this.light.getUnsignedShort(position * 17 + i * 2 + 1);
         if ((mask >> i & 1) == 1) {
            this.serializeOctree(buf, val);
         } else {
            buf.writeShort(val);
         }
      }
   }

   public void serializeForPacket(@Nonnull ByteBuf buf) {
      boolean hasLight = this.light != null;
      buf.writeBoolean(hasLight);
      if (hasLight) {
         buf.ensureWritable(this.light.readableBytes());
         this.serializeOctreeForPacket(buf, 0);
      }
   }

   private void serializeOctreeForPacket(@Nonnull ByteBuf buf, int position) {
      int mask = this.light.getByte(position * 17);
      buf.writeByte(mask);

      for (int i = 0; i < 8; i++) {
         int val = this.light.getUnsignedShort(position * 17 + i * 2 + 1);
         if ((mask >> i & 1) == 1) {
            this.serializeOctreeForPacket(buf, val);
         } else {
            buf.writeShortLE(val);
         }
      }
   }

   @Nonnull
   public static ChunkLightData deserialize(@Nonnull ByteBuf buf, int version) {
      short changeId = buf.readShort();
      boolean hasLight = buf.readBoolean();
      ChunkLightData chunkLightData;
      if (hasLight) {
         int length = buf.readInt();
         ByteBuf from = buf.readSlice(length);
         int estSize = length * 23 / 20;
         ByteBuf buffer = Unpooled.buffer(estSize);
         buffer.writerIndex(17);
         deserializeOctree(from, buffer, 0, 0);
         chunkLightData = new ChunkLightData(buffer.copy(), changeId);
      } else {
         chunkLightData = new ChunkLightData(null, changeId);
      }

      return chunkLightData;
   }

   private static int deserializeOctree(@Nonnull ByteBuf from, @Nonnull ByteBuf to, int position, int segmentIndex) {
      int mask = from.readByte();
      to.setByte(position * 17, mask);

      for (int i = 0; i < 8; i++) {
         int val;
         if ((mask >> i & 1) == 1) {
            to.writerIndex((++segmentIndex + 1) * 17);
            val = segmentIndex;
            segmentIndex = deserializeOctree(from, to, segmentIndex, segmentIndex);
         } else {
            val = from.readShort();
         }

         to.setShort(position * 17 + i * 2 + 1, val);
      }

      return segmentIndex;
   }

   @Nonnull
   public String octreeToString() {
      return this.light == null ? "NULL" : ChunkLightDataBuilder.octreeToString(this.light);
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
