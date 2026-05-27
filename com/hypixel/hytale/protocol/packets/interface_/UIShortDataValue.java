package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class UIShortDataValue extends UIDataValue {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 2;
   public short value;

   public UIShortDataValue() {
   }

   public UIShortDataValue(short value) {
      this.value = value;
   }

   public UIShortDataValue(@Nonnull UIShortDataValue other) {
      this.value = other.value;
   }

   @Nonnull
   public static UIShortDataValue deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 2) {
         throw ProtocolException.bufferTooSmall("UIShortDataValue", 2, buf.readableBytes() - offset);
      } else {
         UIShortDataValue obj = new UIShortDataValue();
         obj.value = buf.getShortLE(offset + 0);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 2;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 2L;
   }

   public static short getValue(MemorySegment mem) {
      return getValue(mem, 0);
   }

   public static short getValue(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_SHORT, (long)(offset + 0));
   }

   public static UIShortDataValue toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UIShortDataValue toObject(MemorySegment mem, int offset) {
      if (offset + 2 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UIShortDataValue", offset + 2, (int)mem.byteSize());
      } else {
         return new UIShortDataValue(mem.get(PacketIO.PROTO_SHORT, (long)(offset + 0)));
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      buf.writeShortLE(this.value);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_SHORT, (long)(offset + 0), this.value);
      return 2;
   }

   @Override
   public int computeSize() {
      return 2;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 2 ? ValidationResult.error("Buffer too small: expected at least 2 bytes") : ValidationResult.OK;
   }

   public UIShortDataValue clone() {
      UIShortDataValue copy = new UIShortDataValue();
      copy.value = this.value;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof UIShortDataValue other ? this.value == other.value : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.value);
   }
}
