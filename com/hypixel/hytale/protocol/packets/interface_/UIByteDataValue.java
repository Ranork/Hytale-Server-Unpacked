package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class UIByteDataValue extends UIDataValue {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 1;
   public byte value;

   public UIByteDataValue() {
   }

   public UIByteDataValue(byte value) {
      this.value = value;
   }

   public UIByteDataValue(@Nonnull UIByteDataValue other) {
      this.value = other.value;
   }

   @Nonnull
   public static UIByteDataValue deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("UIByteDataValue", 1, buf.readableBytes() - offset);
      } else {
         UIByteDataValue obj = new UIByteDataValue();
         obj.value = buf.getByte(offset + 0);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 1;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   public static byte getValue(MemorySegment mem) {
      return getValue(mem, 0);
   }

   public static byte getValue(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
   }

   public static UIByteDataValue toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UIByteDataValue toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UIByteDataValue", offset + 1, (int)mem.byteSize());
      } else {
         return new UIByteDataValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)));
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      buf.writeByte(this.value);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), this.value);
      return 1;
   }

   @Override
   public int computeSize() {
      return 1;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 1 ? ValidationResult.error("Buffer too small: expected at least 1 bytes") : ValidationResult.OK;
   }

   public UIByteDataValue clone() {
      UIByteDataValue copy = new UIByteDataValue();
      copy.value = this.value;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof UIByteDataValue other ? this.value == other.value : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.value);
   }
}
