package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetPath {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 32768019;
   @Nullable
   public String pack;
   @Nullable
   public String path;

   public AssetPath() {
   }

   public AssetPath(@Nullable String pack, @Nullable String path) {
      this.pack = pack;
      this.path = path;
   }

   public AssetPath(@Nonnull AssetPath other) {
      this.pack = other.pack;
      this.path = other.path;
   }

   @Nonnull
   public static AssetPath deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetPath obj = new AssetPath();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 9 + buf.getIntLE(offset + 1);
         int packLen = VarInt.peek(buf, varPos0);
         if (packLen < 0) {
            throw ProtocolException.negativeLength("Pack", packLen);
         }

         if (packLen > 4096000) {
            throw ProtocolException.stringTooLong("Pack", packLen, 4096000);
         }

         obj.pack = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 9 + buf.getIntLE(offset + 5);
         int pathLen = VarInt.peek(buf, varPos1);
         if (pathLen < 0) {
            throw ProtocolException.negativeLength("Path", pathLen);
         }

         if (pathLen > 4096000) {
            throw ProtocolException.stringTooLong("Path", pathLen, 4096000);
         }

         obj.path = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
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
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.pack != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.path != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int packOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int pathOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.pack != null) {
         buf.setIntLE(packOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.pack, 4096000);
      } else {
         buf.setIntLE(packOffsetSlot, -1);
      }

      if (this.path != null) {
         buf.setIntLE(pathOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.path, 4096000);
      } else {
         buf.setIntLE(pathOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 9;
      if (this.pack != null) {
         size += PacketIO.stringSize(this.pack);
      }

      if (this.path != null) {
         size += PacketIO.stringSize(this.path);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int packOffset = buffer.getIntLE(offset + 1);
            if (packOffset < 0) {
               return ValidationResult.error("Invalid offset for Pack");
            }

            int pos = offset + 9 + packOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Pack");
            }

            int packLen = VarInt.peek(buffer, pos);
            if (packLen < 0) {
               return ValidationResult.error("Invalid string length for Pack");
            }

            if (packLen > 4096000) {
               return ValidationResult.error("Pack exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += packLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Pack");
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

            int pathLen = VarInt.peek(buffer, posx);
            if (pathLen < 0) {
               return ValidationResult.error("Invalid string length for Path");
            }

            if (pathLen > 4096000) {
               return ValidationResult.error("Path exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += pathLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Path");
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetPath clone() {
      AssetPath copy = new AssetPath();
      copy.pack = this.pack;
      copy.path = this.path;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetPath other) ? false : Objects.equals(this.pack, other.pack) && Objects.equals(this.path, other.path);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.pack, this.path);
   }
}
