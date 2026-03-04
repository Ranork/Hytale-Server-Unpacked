package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorAsset {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 49152033;
   @Nullable
   public String hash;
   @Nullable
   public AssetPath path;

   public AssetEditorAsset() {
   }

   public AssetEditorAsset(@Nullable String hash, @Nullable AssetPath path) {
      this.hash = hash;
      this.path = path;
   }

   public AssetEditorAsset(@Nonnull AssetEditorAsset other) {
      this.hash = other.hash;
      this.path = other.path;
   }

   @Nonnull
   public static AssetEditorAsset deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetEditorAsset obj = new AssetEditorAsset();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 9 + buf.getIntLE(offset + 1);
         int hashLen = VarInt.peek(buf, varPos0);
         if (hashLen < 0) {
            throw ProtocolException.negativeLength("Hash", hashLen);
         }

         if (hashLen > 4096000) {
            throw ProtocolException.stringTooLong("Hash", hashLen, 4096000);
         }

         obj.hash = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 9 + buf.getIntLE(offset + 5);
         obj.path = AssetPath.deserialize(buf, varPos1);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 9 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 9 + fieldOffset1;
         pos1 += AssetPath.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.hash != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.path != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int hashOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int pathOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.hash != null) {
         buf.setIntLE(hashOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.hash, 4096000);
      } else {
         buf.setIntLE(hashOffsetSlot, -1);
      }

      if (this.path != null) {
         buf.setIntLE(pathOffsetSlot, buf.writerIndex() - varBlockStart);
         this.path.serialize(buf);
      } else {
         buf.setIntLE(pathOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 9;
      if (this.hash != null) {
         size += PacketIO.stringSize(this.hash);
      }

      if (this.path != null) {
         size += this.path.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int hashOffset = buffer.getIntLE(offset + 1);
            if (hashOffset < 0) {
               return ValidationResult.error("Invalid offset for Hash");
            }

            int pos = offset + 9 + hashOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Hash");
            }

            int hashLen = VarInt.peek(buffer, pos);
            if (hashLen < 0) {
               return ValidationResult.error("Invalid string length for Hash");
            }

            if (hashLen > 4096000) {
               return ValidationResult.error("Hash exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += hashLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Hash");
            }
         }

         if ((nullBits & 2) != 0) {
            int pathOffset = buffer.getIntLE(offset + 5);
            if (pathOffset < 0) {
               return ValidationResult.error("Invalid offset for Path");
            }

            int posx = offset + 9 + pathOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Path");
            }

            ValidationResult pathResult = AssetPath.validateStructure(buffer, posx);
            if (!pathResult.isValid()) {
               return ValidationResult.error("Invalid Path: " + pathResult.error());
            }

            posx += AssetPath.computeBytesConsumed(buffer, posx);
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorAsset clone() {
      AssetEditorAsset copy = new AssetEditorAsset();
      copy.hash = this.hash;
      copy.path = this.path != null ? this.path.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorAsset other) ? false : Objects.equals(this.hash, other.hash) && Objects.equals(this.path, other.path);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.hash, this.path);
   }
}
