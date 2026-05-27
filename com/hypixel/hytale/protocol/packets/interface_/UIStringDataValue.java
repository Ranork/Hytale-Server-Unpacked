package com.hypixel.hytale.protocol.packets.interface_;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class UIStringDataValue extends UIDataValue {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 0;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 0;
   public static final int MAX_SIZE = 16384005;
   @Nonnull
   public String value = "";

   public UIStringDataValue() {
   }

   public UIStringDataValue(@Nonnull String value) {
      this.value = value;
   }

   public UIStringDataValue(@Nonnull UIStringDataValue other) {
      this.value = other.value;
   }

   @Nonnull
   public static UIStringDataValue deserialize(@Nonnull ByteBuf buf, int offset) {
      UIStringDataValue obj = new UIStringDataValue();
      int pos = offset + 0;
      int valueLen = VarInt.peek(buf, pos);
      if (valueLen < 0) {
         throw ProtocolException.invalidVarInt("Value");
      } else {
         int valueVarLen = VarInt.size(valueLen);
         if (valueLen > 4096000) {
            throw ProtocolException.stringTooLong("Value", valueLen, 4096000);
         } else if (pos + valueVarLen + valueLen > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Value", pos + valueVarLen + valueLen, buf.readableBytes());
         } else {
            obj.value = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += valueVarLen + valueLen;
            return obj;
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 0;
      int sl = VarInt.peek(buf, pos);
      pos += VarInt.size(sl) + sl;
      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 0L;
   }

   public static String getValue(MemorySegment mem) {
      return getValue(mem, 0);
   }

   public static String getValue(MemorySegment mem, int offset) {
      return PacketIO.readVarString("Value", mem, offset + 0, 4096000, PacketIO.UTF8);
   }

   public static UIStringDataValue toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static UIStringDataValue toObject(MemorySegment mem, int offset) {
      if (offset + 0 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("UIStringDataValue", offset + 0, (int)mem.byteSize());
      } else {
         return new UIStringDataValue(PacketIO.readVarString("Value", mem, offset + 0, 4096000, PacketIO.UTF8));
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      PacketIO.writeVarString(buf, this.value, 4096000);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      int varOffset = offset + 0;
      varOffset += PacketIO.writeVarString(mem, varOffset, this.value, 4096000);
      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 0;
      return size + PacketIO.stringSize(this.value);
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 0) {
         return ValidationResult.error("Buffer too small: expected at least 0 bytes");
      } else {
         int pos = offset + 0;
         int valueLen = VarInt.peek(buffer, pos);
         if (valueLen < 0) {
            return ValidationResult.error("Invalid string length for Value");
         } else if (valueLen > 4096000) {
            return ValidationResult.error("Value exceeds max length 4096000");
         } else {
            pos += VarInt.size(valueLen);
            pos += valueLen;
            return pos > buffer.writerIndex() ? ValidationResult.error("Buffer overflow reading Value") : ValidationResult.OK;
         }
      }
   }

   public UIStringDataValue clone() {
      UIStringDataValue copy = new UIStringDataValue();
      copy.value = this.value;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof UIStringDataValue other ? Objects.equals(this.value, other.value) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.value);
   }
}
