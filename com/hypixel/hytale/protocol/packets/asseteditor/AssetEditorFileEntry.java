package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorFileEntry {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 2;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 2;
   public static final int MAX_SIZE = 16384007;
   @Nullable
   public String path;
   public boolean isDirectory;

   public AssetEditorFileEntry() {
   }

   public AssetEditorFileEntry(@Nullable String path, boolean isDirectory) {
      this.path = path;
      this.isDirectory = isDirectory;
   }

   public AssetEditorFileEntry(@Nonnull AssetEditorFileEntry other) {
      this.path = other.path;
      this.isDirectory = other.isDirectory;
   }

   @Nonnull
   public static AssetEditorFileEntry deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetEditorFileEntry obj = new AssetEditorFileEntry();
      byte nullBits = buf.getByte(offset);
      obj.isDirectory = buf.getByte(offset + 1) != 0;
      int pos = offset + 2;
      if ((nullBits & 1) != 0) {
         int pathLen = VarInt.peek(buf, pos);
         if (pathLen < 0) {
            throw ProtocolException.negativeLength("Path", pathLen);
         }

         if (pathLen > 4096000) {
            throw ProtocolException.stringTooLong("Path", pathLen, 4096000);
         }

         int pathVarLen = VarInt.length(buf, pos);
         obj.path = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
         pos += pathVarLen + pathLen;
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
      if (this.path != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.isDirectory ? 1 : 0);
      if (this.path != null) {
         PacketIO.writeVarString(buf, this.path, 4096000);
      }
   }

   public int computeSize() {
      int size = 2;
      if (this.path != null) {
         size += PacketIO.stringSize(this.path);
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
            int pathLen = VarInt.peek(buffer, pos);
            if (pathLen < 0) {
               return ValidationResult.error("Invalid string length for Path");
            }

            if (pathLen > 4096000) {
               return ValidationResult.error("Path exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += pathLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Path");
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorFileEntry clone() {
      AssetEditorFileEntry copy = new AssetEditorFileEntry();
      copy.path = this.path;
      copy.isDirectory = this.isDirectory;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorFileEntry other) ? false : Objects.equals(this.path, other.path) && this.isDirectory == other.isDirectory;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.path, this.isDirectory);
   }
}
