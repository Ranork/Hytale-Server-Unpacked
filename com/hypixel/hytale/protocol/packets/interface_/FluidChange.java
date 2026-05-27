package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class FluidChange {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 17;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 17;
   public static final int MAX_SIZE = 17;
   public int x;
   public int y;
   public int z;
   public int fluidId;
   public byte fluidLevel;

   public FluidChange() {
   }

   public FluidChange(int x, int y, int z, int fluidId, byte fluidLevel) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.fluidId = fluidId;
      this.fluidLevel = fluidLevel;
   }

   public FluidChange(@Nonnull FluidChange other) {
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
      this.fluidId = other.fluidId;
      this.fluidLevel = other.fluidLevel;
   }

   @Nonnull
   public static FluidChange deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 17) {
         throw ProtocolException.bufferTooSmall("FluidChange", 17, buf.readableBytes() - offset);
      } else {
         FluidChange obj = new FluidChange();
         obj.x = buf.getIntLE(offset + 0);
         obj.y = buf.getIntLE(offset + 4);
         obj.z = buf.getIntLE(offset + 8);
         obj.fluidId = buf.getIntLE(offset + 12);
         obj.fluidLevel = buf.getByte(offset + 16);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 17;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 17L;
   }

   public static int getX(MemorySegment mem) {
      return getX(mem, 0);
   }

   public static int getX(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static int getY(MemorySegment mem) {
      return getY(mem, 0);
   }

   public static int getY(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 4));
   }

   public static int getZ(MemorySegment mem) {
      return getZ(mem, 0);
   }

   public static int getZ(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 8));
   }

   public static int getFluidId(MemorySegment mem) {
      return getFluidId(mem, 0);
   }

   public static int getFluidId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 12));
   }

   public static byte getFluidLevel(MemorySegment mem) {
      return getFluidLevel(mem, 0);
   }

   public static byte getFluidLevel(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BYTE, (long)(offset + 16));
   }

   public static FluidChange toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static FluidChange toObject(MemorySegment mem, int offset) {
      if (offset + 17 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("FluidChange", offset + 17, (int)mem.byteSize());
      } else {
         return new FluidChange(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 8)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 12)),
            mem.get(PacketIO.PROTO_BYTE, (long)(offset + 16))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.x);
      buf.writeIntLE(this.y);
      buf.writeIntLE(this.z);
      buf.writeIntLE(this.fluidId);
      buf.writeByte(this.fluidLevel);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.x);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), this.y);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 8), this.z);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 12), this.fluidId);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 16), this.fluidLevel);
      return 17;
   }

   public int computeSize() {
      return 17;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 17 ? ValidationResult.error("Buffer too small: expected at least 17 bytes") : ValidationResult.OK;
   }

   public FluidChange clone() {
      FluidChange copy = new FluidChange();
      copy.x = this.x;
      copy.y = this.y;
      copy.z = this.z;
      copy.fluidId = this.fluidId;
      copy.fluidLevel = this.fluidLevel;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof FluidChange other)
            ? false
            : this.x == other.x && this.y == other.y && this.z == other.z && this.fluidId == other.fluidId && this.fluidLevel == other.fluidLevel;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y, this.z, this.fluidId, this.fluidLevel);
   }
}
