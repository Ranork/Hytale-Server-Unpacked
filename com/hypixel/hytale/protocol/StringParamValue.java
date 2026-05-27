package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("StringParamValue", 1, buf.readableBytes() - offset);
      } else {
         StringParamValue obj = new StringParamValue();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int valueLen = VarInt.peek(buf, pos);
            if (valueLen < 0) {
               throw ProtocolException.invalidVarInt("Value");
            }

            int valueVarLen = VarInt.size(valueLen);
            if (valueLen > 4096000) {
               throw ProtocolException.stringTooLong("Value", valueLen, 4096000);
            }

            if (pos + valueVarLen + valueLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Value", pos + valueVarLen + valueLen, buf.readableBytes());
            }

            obj.value = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += valueVarLen + valueLen;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static String getValue(MemorySegment mem) {
      return getValue(mem, 0);
   }

   @Nullable
   public static String getValue(MemorySegment mem, int offset) {
      return hasValue(mem, offset) ? PacketIO.readVarString("Value", mem, offset + 1, 4096000, PacketIO.UTF8) : null;
   }

   public static boolean hasValue(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static StringParamValue toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static StringParamValue toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("StringParamValue", offset + 1, (int)mem.byteSize());
      } else {
         return new StringParamValue(hasValue(mem, offset) ? PacketIO.readVarString("Value", mem, offset + 1, 4096000, PacketIO.UTF8) : null);
      }
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.value != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.value != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.value, 4096000);
      }

      return varOffset - offset;
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

            pos += VarInt.size(valueLen);
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
