package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TimestampedAssetReference {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 49152033;
   @Nullable
   public AssetPath path;
   @Nullable
   public String timestamp;

   public TimestampedAssetReference() {
   }

   public TimestampedAssetReference(@Nullable AssetPath path, @Nullable String timestamp) {
      this.path = path;
      this.timestamp = timestamp;
   }

   public TimestampedAssetReference(@Nonnull TimestampedAssetReference other) {
      this.path = other.path;
      this.timestamp = other.timestamp;
   }

   @Nonnull
   public static TimestampedAssetReference deserialize(@Nonnull ByteBuf buf, int offset) {
      TimestampedAssetReference obj = new TimestampedAssetReference();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 9 + buf.getIntLE(offset + 1);
         obj.path = AssetPath.deserialize(buf, varPos0);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 9 + buf.getIntLE(offset + 5);
         int timestampLen = VarInt.peek(buf, varPos1);
         if (timestampLen < 0) {
            throw ProtocolException.negativeLength("Timestamp", timestampLen);
         }

         if (timestampLen > 4096000) {
            throw ProtocolException.stringTooLong("Timestamp", timestampLen, 4096000);
         }

         obj.timestamp = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 9 + fieldOffset0;
         pos0 += AssetPath.computeBytesConsumed(buf, pos0);
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
      if (this.path != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.timestamp != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int pathOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int timestampOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.path != null) {
         buf.setIntLE(pathOffsetSlot, buf.writerIndex() - varBlockStart);
         this.path.serialize(buf);
      } else {
         buf.setIntLE(pathOffsetSlot, -1);
      }

      if (this.timestamp != null) {
         buf.setIntLE(timestampOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.timestamp, 4096000);
      } else {
         buf.setIntLE(timestampOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 9;
      if (this.path != null) {
         size += this.path.computeSize();
      }

      if (this.timestamp != null) {
         size += PacketIO.stringSize(this.timestamp);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int pathOffset = buffer.getIntLE(offset + 1);
            if (pathOffset < 0) {
               return ValidationResult.error("Invalid offset for Path");
            }

            int pos = offset + 9 + pathOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Path");
            }

            ValidationResult pathResult = AssetPath.validateStructure(buffer, pos);
            if (!pathResult.isValid()) {
               return ValidationResult.error("Invalid Path: " + pathResult.error());
            }

            pos += AssetPath.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 2) != 0) {
            int timestampOffset = buffer.getIntLE(offset + 5);
            if (timestampOffset < 0) {
               return ValidationResult.error("Invalid offset for Timestamp");
            }

            int posx = offset + 9 + timestampOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Timestamp");
            }

            int timestampLen = VarInt.peek(buffer, posx);
            if (timestampLen < 0) {
               return ValidationResult.error("Invalid string length for Timestamp");
            }

            if (timestampLen > 4096000) {
               return ValidationResult.error("Timestamp exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += timestampLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Timestamp");
            }
         }

         return ValidationResult.OK;
      }
   }

   public TimestampedAssetReference clone() {
      TimestampedAssetReference copy = new TimestampedAssetReference();
      copy.path = this.path != null ? this.path.clone() : null;
      copy.timestamp = this.timestamp;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof TimestampedAssetReference other)
            ? false
            : Objects.equals(this.path, other.path) && Objects.equals(this.timestamp, other.timestamp);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.path, this.timestamp);
   }
}
