package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderToolBlockArg {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 16384007;
   @Nullable
   public String defaultValue;
   public boolean allowPattern;

   public BuilderToolBlockArg() {
   }

   public BuilderToolBlockArg(@Nullable String defaultValue, boolean allowPattern) {
      this.defaultValue = defaultValue;
      this.allowPattern = allowPattern;
   }

   public BuilderToolBlockArg(@Nonnull BuilderToolBlockArg other) {
      this.defaultValue = other.defaultValue;
      this.allowPattern = other.allowPattern;
   }

   @Nonnull
   public static BuilderToolBlockArg deserialize(@Nonnull ByteBuf buf, int offset) {
      BuilderToolBlockArg obj = new BuilderToolBlockArg();
      byte nullBits = buf.getByte(offset);
      obj.allowPattern = buf.getByte(offset + 1) != 0;
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int defaultValueLen = VarInt.peek(buf, pos);
         if (defaultValueLen < 0) {
            throw ProtocolException.negativeLength("Default", defaultValueLen);
         }

         if (defaultValueLen > 4096000) {
            throw ProtocolException.stringTooLong("Default", defaultValueLen, 4096000);
         }

         int defaultValueVarLen = VarInt.length(buf, pos);
         obj.defaultValue = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += defaultValueVarLen + defaultValueLen;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + sl;
      }

      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.defaultValue != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.allowPattern ? 1 : 0);
      if (this.defaultValue != null) {
         PacketIO.writeVarString(buf, this.defaultValue, 4096000);
      }
   }

   public int computeSize() {
      int size = 2;
      if (this.defaultValue != null) {
         size += PacketIO.stringSize(this.defaultValue);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 2) {
         return ValidationResult.error("Buffer too small: expected at least 2 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 2;
         if ((nullBits & 1) != 0) {
            int defaultLen = VarInt.peek(buffer, pos);
            if (defaultLen < 0) {
               return ValidationResult.error("Invalid string length for Default");
            }

            if (defaultLen > 4096000) {
               return ValidationResult.error("Default exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += defaultLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Default");
            }
         }

         return ValidationResult.OK;
      }
   }

   public BuilderToolBlockArg clone() {
      BuilderToolBlockArg copy = new BuilderToolBlockArg();
      copy.defaultValue = this.defaultValue;
      copy.allowPattern = this.allowPattern;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolBlockArg other)
            ? false
            : Objects.equals(this.defaultValue, other.defaultValue) && this.allowPattern == other.allowPattern;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.defaultValue, this.allowPattern);
   }
}
