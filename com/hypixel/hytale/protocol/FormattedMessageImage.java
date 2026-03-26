package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class FormattedMessageImage {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 8;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 16384013;
   @Nonnull
   public String filePath = "";
   public int width;
   public int height;

   public FormattedMessageImage() {
   }

   public FormattedMessageImage(@Nonnull String filePath, int width, int height) {
      this.filePath = filePath;
      this.width = width;
      this.height = height;
   }

   public FormattedMessageImage(@Nonnull FormattedMessageImage other) {
      this.filePath = other.filePath;
      this.width = other.width;
      this.height = other.height;
   }

   @Nonnull
   public static FormattedMessageImage deserialize(@Nonnull ByteBuf buf, int offset) {
      FormattedMessageImage obj = new FormattedMessageImage();
      obj.width = buf.getIntLE(offset + 0);
      obj.height = buf.getIntLE(offset + 4);
      int pos = offset + 8;
      int filePathLen = VarInt.peek(buf, pos);
      if (filePathLen < 0) {
         throw ProtocolException.negativeLength("FilePath", filePathLen);
      } else if (filePathLen > 4096000) {
         throw ProtocolException.stringTooLong("FilePath", filePathLen, 4096000);
      } else {
         int filePathVarLen = VarInt.length(buf, pos);
         obj.filePath = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += filePathVarLen + filePathLen;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 8;
      int sl = VarInt.peek(buf, pos);
      pos += VarInt.length(buf, pos) + sl;
      return pos - offset;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.width);
      buf.writeIntLE(this.height);
      PacketIO.writeVarString(buf, this.filePath, 4096000);
   }

   public int computeSize() {
      int size = 8;
      return size + PacketIO.stringSize(this.filePath);
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 8) {
         return ValidationResult.error("Buffer too small: expected at least 8 bytes");
      } else {
         int pos = offset + 8;
         int filePathLen = VarInt.peek(buffer, pos);
         if (filePathLen < 0) {
            return ValidationResult.error("Invalid string length for FilePath");
         } else if (filePathLen > 4096000) {
            return ValidationResult.error("FilePath exceeds max length 4096000");
         } else {
            pos += VarInt.length(buffer, pos);
            pos += filePathLen;
            return pos > buffer.writerIndex() ? ValidationResult.error("Buffer overflow reading FilePath") : ValidationResult.OK;
         }
      }
   }

   public FormattedMessageImage clone() {
      FormattedMessageImage copy = new FormattedMessageImage();
      copy.filePath = this.filePath;
      copy.width = this.width;
      copy.height = this.height;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof FormattedMessageImage other)
            ? false
            : Objects.equals(this.filePath, other.filePath) && this.width == other.width && this.height == other.height;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.filePath, this.width, this.height);
   }
}
