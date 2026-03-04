package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StringParamValue extends ParamValue {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 16384006;
   @Nullable
   public String value;

   public StringParamValue() {
   }

   public StringParamValue(@Nullable String value) {
      this.value = value;
   }

   public StringParamValue(@Nonnull StringParamValue other) {
      this.value = other.value;
   }

   @Nonnull
   public static StringParamValue deserialize(@Nonnull ByteBuf buf, int offset) {
      StringParamValue obj = new StringParamValue();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int valueLen = VarInt.peek(buf, pos);
         if (valueLen < 0) {
            throw ProtocolException.negativeLength("Value", valueLen);
         }

         if (valueLen > 4096000) {
            throw ProtocolException.stringTooLong("Value", valueLen, 4096000);
         }

         int valueVarLen = VarInt.length(buf, pos);
         obj.value = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += valueVarLen + valueLen;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + sl;
      }

      return pos - offset;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.value != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.value != null) {
         PacketIO.writeVarString(buf, this.value, 4096000);
      }

      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.value != null) {
         size += PacketIO.stringSize(this.value);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 1) {
         return ValidationResult.error("Buffer too small: expected at least 1 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int valueLen = VarInt.peek(buffer, pos);
            if (valueLen < 0) {
               return ValidationResult.error("Invalid string length for Value");
            }

            if (valueLen > 4096000) {
               return ValidationResult.error("Value exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += valueLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Value");
            }
         }

         return ValidationResult.OK;
      }
   }

   public StringParamValue clone() {
      StringParamValue copy = new StringParamValue();
      copy.value = this.value;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof StringParamValue other ? Objects.equals(this.value, other.value) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.value);
   }
}
