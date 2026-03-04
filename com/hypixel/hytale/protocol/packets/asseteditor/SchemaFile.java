package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SchemaFile {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 67108866;
   @Nullable
   public String content;

   public SchemaFile() {
   }

   public SchemaFile(@Nullable String content) {
      this.content = content;
   }

   public SchemaFile(@Nonnull SchemaFile other) {
      this.content = other.content;
   }

   @Nonnull
   public static SchemaFile deserialize(@Nonnull ByteBuf buf, int offset) {
      SchemaFile obj = new SchemaFile();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int contentLen = VarInt.peek(buf, pos);
         if (contentLen < 0) {
            throw ProtocolException.negativeLength("Content", contentLen);
         }

         if (contentLen > 16777215) {
            throw ProtocolException.stringTooLong("Content", contentLen, 16777215);
         }

         int contentVarLen = VarInt.length(buf, pos);
         obj.content = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += contentVarLen + contentLen;
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

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.content != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.content != null) {
         PacketIO.writeVarString(buf, this.content, 16777215);
      }
   }

   public int computeSize() {
      int size = 1;
      if (this.content != null) {
         size += PacketIO.stringSize(this.content);
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
            int contentLen = VarInt.peek(buffer, pos);
            if (contentLen < 0) {
               return ValidationResult.error("Invalid string length for Content");
            }

            if (contentLen > 16777215) {
               return ValidationResult.error("Content exceeds max length 16777215");
            }

            pos += VarInt.length(buffer, pos);
            pos += contentLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Content");
            }
         }

         return ValidationResult.OK;
      }
   }

   public SchemaFile clone() {
      SchemaFile copy = new SchemaFile();
      copy.content = this.content;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof SchemaFile other ? Objects.equals(this.content, other.content) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.content);
   }
}
