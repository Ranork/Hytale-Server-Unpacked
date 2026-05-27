package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderToolMaskArg {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 16384006;
   @Nullable
   public String defaultValue;

   public BuilderToolMaskArg() {
   }

   public BuilderToolMaskArg(@Nullable String defaultValue) {
      this.defaultValue = defaultValue;
   }

   public BuilderToolMaskArg(@Nonnull BuilderToolMaskArg other) {
      this.defaultValue = other.defaultValue;
   }

   @Nonnull
   public static BuilderToolMaskArg deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("BuilderToolMaskArg", 1, buf.readableBytes() - offset);
      } else {
         BuilderToolMaskArg obj = new BuilderToolMaskArg();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int defaultValueLen = VarInt.peek(buf, pos);
            if (defaultValueLen < 0) {
               throw ProtocolException.invalidVarInt("Default");
            }

            int defaultValueVarLen = VarInt.size(defaultValueLen);
            if (defaultValueLen > 4096000) {
               throw ProtocolException.stringTooLong("Default", defaultValueLen, 4096000);
            }

            if (pos + defaultValueVarLen + defaultValueLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Default", pos + defaultValueVarLen + defaultValueLen, buf.readableBytes());
            }

            obj.defaultValue = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += defaultValueVarLen + defaultValueLen;
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
   public static String getDefault(MemorySegment mem) {
      return getDefault(mem, 0);
   }

   @Nullable
   public static String getDefault(MemorySegment mem, int offset) {
      return hasDefault(mem, offset) ? PacketIO.readVarString("Default", mem, offset + 1, 4096000, PacketIO.UTF8) : null;
   }

   public static boolean hasDefault(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static BuilderToolMaskArg toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BuilderToolMaskArg toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BuilderToolMaskArg", offset + 1, (int)mem.byteSize());
      } else {
         return new BuilderToolMaskArg(hasDefault(mem, offset) ? PacketIO.readVarString("Default", mem, offset + 1, 4096000, PacketIO.UTF8) : null);
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.defaultValue != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.defaultValue != null) {
         PacketIO.writeVarString(buf, this.defaultValue, 4096000);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.defaultValue != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.defaultValue != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.defaultValue, 4096000);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 1;
      if (this.defaultValue != null) {
         size += PacketIO.stringSize(this.defaultValue);
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
            int defaultLen = VarInt.peek(buffer, pos);
            if (defaultLen < 0) {
               return ValidationResult.error("Invalid string length for Default");
            }

            if (defaultLen > 4096000) {
               return ValidationResult.error("Default exceeds max length 4096000");
            }

            pos += VarInt.size(defaultLen);
            pos += defaultLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Default");
            }
         }

         return ValidationResult.OK;
      }
   }

   public BuilderToolMaskArg clone() {
      BuilderToolMaskArg copy = new BuilderToolMaskArg();
      copy.defaultValue = this.defaultValue;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof BuilderToolMaskArg other ? Objects.equals(this.defaultValue, other.defaultValue) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.defaultValue);
   }
}
