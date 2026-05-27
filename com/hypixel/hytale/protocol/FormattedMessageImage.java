package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 8) {
         throw ProtocolException.bufferTooSmall("FormattedMessageImage", 8, buf.readableBytes() - offset);
      } else {
         FormattedMessageImage obj = new FormattedMessageImage();
         obj.width = buf.getIntLE(offset + 0);
         obj.height = buf.getIntLE(offset + 4);
         int pos = offset + 8;
         int filePathLen = VarInt.peek(buf, pos);
         if (filePathLen < 0) {
            throw ProtocolException.invalidVarInt("FilePath");
         } else {
            int filePathVarLen = VarInt.size(filePathLen);
            if (filePathLen > 4096000) {
               throw ProtocolException.stringTooLong("FilePath", filePathLen, 4096000);
            } else if (pos + filePathVarLen + filePathLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("FilePath", pos + filePathVarLen + filePathLen, buf.readableBytes());
            } else {
               obj.filePath = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
               pos += filePathVarLen + filePathLen;
               return obj;
            }
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 8;
      int sl = VarInt.peek(buf, pos);
      pos += VarInt.size(sl) + sl;
      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 8L;
   }

   public static String getFilePath(MemorySegment mem) {
      return getFilePath(mem, 0);
   }

   public static String getFilePath(MemorySegment mem, int offset) {
      return PacketIO.readVarString("FilePath", mem, offset + 8, 4096000, PacketIO.UTF8);
   }

   public static int getWidth(MemorySegment mem) {
      return getWidth(mem, 0);
   }

   public static int getWidth(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 0));
   }

   public static int getHeight(MemorySegment mem) {
      return getHeight(mem, 0);
   }

   public static int getHeight(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 4));
   }

   public static FormattedMessageImage toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static FormattedMessageImage toObject(MemorySegment mem, int offset) {
      if (offset + 8 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("FormattedMessageImage", offset + 8, (int)mem.byteSize());
      } else {
         return new FormattedMessageImage(
            PacketIO.readVarString("FilePath", mem, offset + 8, 4096000, PacketIO.UTF8),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 4))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.width);
      buf.writeIntLE(this.height);
      PacketIO.writeVarString(buf, this.filePath, 4096000);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_INT, (long)(offset + 0), this.width);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 4), this.height);
      int varOffset = offset + 8;
      varOffset += PacketIO.writeVarString(mem, varOffset, this.filePath, 4096000);
      return varOffset - offset;
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
            pos += VarInt.size(filePathLen);
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
