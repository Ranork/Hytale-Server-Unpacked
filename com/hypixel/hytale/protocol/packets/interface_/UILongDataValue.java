package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class UILongDataValue extends UIDataValue {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 8;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 8;
   public long value;

   public UILongDataValue() {
   }

   public UILongDataValue(long value) {
      this.value = value;
   }

   public UILongDataValue(@Nonnull UILongDataValue other) {
      this.value = other.value;
   }

   @Nonnull
   public static UILongDataValue deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 8) {
         throw ProtocolException.bufferTooSmall("UILongDataValue", 8, buf.readableBytes() - offset);
      } else {
         UILongDataValue obj = new UILongDataValue();
         obj.value = buf.getLongLE(offset + 0);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 8;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 8L;
   }

   public static long getValue(MemorySegment mem) {
      return getValue(mem, 0);
   }

   public static long getValue(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_LONG, (long)(offset + 0));
   }

   public static UILongDataValue toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UILongDataValue toObject(MemorySegment mem, int offset) {
      if (offset + 8 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UILongDataValue", offset + 8, (int)mem.byteSize());
      } else {
         return new UILongDataValue(mem.get(PacketIO.PROTO_LONG, (long)(offset + 0)));
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      buf.writeLongLE(this.value);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_LONG, (long)(offset + 0), this.value);
      return 8;
   }

   @Override
   public int computeSize() {
      return 8;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 8 ? ValidationResult.error("Buffer too small: expected at least 8 bytes") : ValidationResult.OK;
   }

   public UILongDataValue clone() {
      UILongDataValue copy = new UILongDataValue();
      copy.value = this.value;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof UILongDataValue other ? this.value == other.value : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.value);
   }
}
