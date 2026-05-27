package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SetFluidCmd {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 7;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 7;
   public static final int MAX_SIZE = 7;
   public short index;
   public int fluidId;
   public byte fluidLevel;

   public SetFluidCmd() {
   }

   public SetFluidCmd(short index, int fluidId, byte fluidLevel) {
      this.index = index;
      this.fluidId = fluidId;
      this.fluidLevel = fluidLevel;
   }

   public SetFluidCmd(@Nonnull SetFluidCmd other) {
      this.index = other.index;
      this.fluidId = other.fluidId;
      this.fluidLevel = other.fluidLevel;
   }

   @Nonnull
   public static SetFluidCmd deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 7) {
         throw ProtocolException.bufferTooSmall("SetFluidCmd", 7, buf.readableBytes() - offset);
      } else {
         SetFluidCmd obj = new SetFluidCmd();
         obj.index = buf.getShortLE(offset + 0);
         obj.fluidId = buf.getIntLE(offset + 2);
         obj.fluidLevel = buf.getByte(offset + 6);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 7;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 7L;
   }

   public static short getIndex(MemorySegment mem) {
      return getIndex(mem, 0);
   }

   public static short getIndex(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_SHORT, (long)(offset + 0));
   }

   public static int getFluidId(MemorySegment mem) {
      return getFluidId(mem, 0);
   }

   public static int getFluidId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 2));
   }

   public static byte getFluidLevel(MemorySegment mem) {
      return getFluidLevel(mem, 0);
   }

   public static byte getFluidLevel(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BYTE, (long)(offset + 6));
   }

   public static SetFluidCmd toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SetFluidCmd toObject(MemorySegment mem, int offset) {
      if (offset + 7 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SetFluidCmd", offset + 7, (int)mem.byteSize());
      } else {
         return new SetFluidCmd(
            mem.get(PacketIO.PROTO_SHORT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 2)),
            mem.get(PacketIO.PROTO_BYTE, (long)(offset + 6))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeShortLE(this.index);
      buf.writeIntLE(this.fluidId);
      buf.writeByte(this.fluidLevel);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_SHORT, (long)(offset + 0), this.index);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.fluidId);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 6), this.fluidLevel);
      return 7;
   }

   public int computeSize() {
      return 7;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 7 ? ValidationResult.error("Buffer too small: expected at least 7 bytes") : ValidationResult.OK;
   }

   public SetFluidCmd clone() {
      SetFluidCmd copy = new SetFluidCmd();
      copy.index = this.index;
      copy.fluidId = this.fluidId;
      copy.fluidLevel = this.fluidLevel;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SetFluidCmd other) ? false : this.index == other.index && this.fluidId == other.fluidId && this.fluidLevel == other.fluidLevel;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.index, this.fluidId, this.fluidLevel);
   }
}
