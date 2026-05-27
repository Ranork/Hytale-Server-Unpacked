package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class Rangeb {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 2;
   public byte min;
   public byte max;

   public Rangeb() {
   }

   public Rangeb(byte min, byte max) {
      this.min = min;
      this.max = max;
   }

   public Rangeb(@Nonnull Rangeb other) {
      this.min = other.min;
      this.max = other.max;
   }

   @Nonnull
   public static Rangeb deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 2) {
         throw ProtocolException.bufferTooSmall("Rangeb", 2, buf.readableBytes() - offset);
      } else {
         Rangeb obj = new Rangeb();
         obj.min = buf.getByte(offset + 0);
         obj.max = buf.getByte(offset + 1);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 2;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 2L;
   }

   public static byte getMin(MemorySegment mem) {
      return getMin(mem, 0);
   }

   public static byte getMin(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
   }

   public static byte getMax(MemorySegment mem) {
      return getMax(mem, 0);
   }

   public static byte getMax(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
   }

   public static Rangeb toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static Rangeb toObject(MemorySegment mem, int offset) {
      if (offset + 2 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("Rangeb", offset + 2, (int)mem.byteSize());
      } else {
         return new Rangeb(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)), mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1)));
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.min);
      buf.writeByte(this.max);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), this.min);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), this.max);
      return 2;
   }

   public int computeSize() {
      return 2;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 2 ? ValidationResult.error("Buffer too small: expected at least 2 bytes") : ValidationResult.OK;
   }

   public Rangeb clone() {
      Rangeb copy = new Rangeb();
      copy.min = this.min;
      copy.max = this.max;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Rangeb other) ? false : this.min == other.min && this.max == other.max;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.min, this.max);
   }
}
