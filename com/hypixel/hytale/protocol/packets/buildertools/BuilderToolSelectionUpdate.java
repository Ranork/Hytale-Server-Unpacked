package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BuilderToolSelectionUpdate implements Packet, ToServerPacket {
   public static final int PACKET_ID = 409;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 24;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 24;
   public static final int MAX_SIZE = 24;
   public int xMin;
   public int yMin;
   public int zMin;
   public int xMax;
   public int yMax;
   public int zMax;

   @Override
   public int getId() {
      return 409;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolSelectionUpdate() {
   }

   public BuilderToolSelectionUpdate(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
      this.xMin = xMin;
      this.yMin = yMin;
      this.zMin = zMin;
      this.xMax = xMax;
      this.yMax = yMax;
      this.zMax = zMax;
   }

   public BuilderToolSelectionUpdate(@Nonnull BuilderToolSelectionUpdate other) {
      this.xMin = other.xMin;
      this.yMin = other.yMin;
      this.zMin = other.zMin;
      this.xMax = other.xMax;
      this.yMax = other.yMax;
      this.zMax = other.zMax;
   }

   @Nonnull
   public static BuilderToolSelectionUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 24) {
         throw ProtocolException.bufferTooSmall("BuilderToolSelectionUpdate", 24, buf.readableBytes() - offset);
      } else {
         BuilderToolSelectionUpdate obj = new BuilderToolSelectionUpdate();
         obj.xMin = buf.getIntLE(offset + 0);
         obj.yMin = buf.getIntLE(offset + 4);
         obj.zMin = buf.getIntLE(offset + 8);
         obj.xMax = buf.getIntLE(offset + 12);
         obj.yMax = buf.getIntLE(offset + 16);
         obj.zMax = buf.getIntLE(offset + 20);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 24;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 24L;
   }

   public static int getXMin(MemorySegment mem) {
      return getXMin(mem, 0);
   }

   public static int getXMin(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static int getYMin(MemorySegment mem) {
      return getYMin(mem, 0);
   }

   public static int getYMin(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 4));
   }

   public static int getZMin(MemorySegment mem) {
      return getZMin(mem, 0);
   }

   public static int getZMin(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 8));
   }

   public static int getXMax(MemorySegment mem) {
      return getXMax(mem, 0);
   }

   public static int getXMax(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 12));
   }

   public static int getYMax(MemorySegment mem) {
      return getYMax(mem, 0);
   }

   public static int getYMax(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 16));
   }

   public static int getZMax(MemorySegment mem) {
      return getZMax(mem, 0);
   }

   public static int getZMax(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 20));
   }

   public static BuilderToolSelectionUpdate toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BuilderToolSelectionUpdate toObject(MemorySegment mem, int offset) {
      if (offset + 24 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BuilderToolSelectionUpdate", offset + 24, (int)mem.byteSize());
      } else {
         return new BuilderToolSelectionUpdate(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 8)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 12)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 16)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 20))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.xMin);
      buf.writeIntLE(this.yMin);
      buf.writeIntLE(this.zMin);
      buf.writeIntLE(this.xMax);
      buf.writeIntLE(this.yMax);
      buf.writeIntLE(this.zMax);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.xMin);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), this.yMin);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 8), this.zMin);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 12), this.xMax);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 16), this.yMax);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 20), this.zMax);
      return 24;
   }

   @Override
   public int computeSize() {
      return 24;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 24 ? ValidationResult.error("Buffer too small: expected at least 24 bytes") : ValidationResult.OK;
   }

   public BuilderToolSelectionUpdate clone() {
      BuilderToolSelectionUpdate copy = new BuilderToolSelectionUpdate();
      copy.xMin = this.xMin;
      copy.yMin = this.yMin;
      copy.zMin = this.zMin;
      copy.xMax = this.xMax;
      copy.yMax = this.yMax;
      copy.zMax = this.zMax;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolSelectionUpdate other)
            ? false
            : this.xMin == other.xMin
               && this.yMin == other.yMin
               && this.zMin == other.zMin
               && this.xMax == other.xMax
               && this.yMax == other.yMax
               && this.zMax == other.zMax;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.xMin, this.yMin, this.zMin, this.xMax, this.yMax, this.zMax);
   }
}
