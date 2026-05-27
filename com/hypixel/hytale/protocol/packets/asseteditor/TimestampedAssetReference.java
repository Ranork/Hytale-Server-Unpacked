package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("TimestampedAssetReference", 9, buf.readableBytes() - offset);
      } else {
         TimestampedAssetReference obj = new TimestampedAssetReference();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("Path", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 9 + varPosBase0;
            obj.path = AssetPath.deserialize(buf, varPos0);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("Timestamp", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 9 + varPosBase1;
            int timestampLen = VarInt.peek(buf, varPos1);
            if (timestampLen < 0) {
               throw ProtocolException.invalidVarInt("Timestamp");
            }

            int timestampVarIntLen = VarInt.size(timestampLen);
            if (timestampLen > 4096000) {
               throw ProtocolException.stringTooLong("Timestamp", timestampLen, 4096000);
            }

            if (varPos1 + timestampVarIntLen + timestampLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Timestamp", varPos1 + timestampVarIntLen + timestampLen, buf.readableBytes());
            }

            obj.timestamp = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("Path", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 9 + fieldOffset0;
         pos0 += AssetPath.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("Timestamp", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 9 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   @Nullable
   public static AssetPath getPath(MemorySegment mem) {
      return getPath(mem, 0);
   }

   @Nullable
   public static AssetPath getPath(MemorySegment mem, int offset) {
      return hasPath(mem, offset) ? AssetPath.toObject(mem, offset + getValidatedOffset(mem, offset, 1, 9, "Path")) : null;
   }

   @Nullable
   public static String getTimestamp(MemorySegment mem) {
      return getTimestamp(mem, 0);
   }

   @Nullable
   public static String getTimestamp(MemorySegment mem, int offset) {
      return hasTimestamp(mem, offset)
         ? PacketIO.readVarString("Timestamp", mem, offset + getValidatedOffset(mem, offset, 5, 9, "Timestamp"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean hasPath(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasTimestamp(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static TimestampedAssetReference toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static TimestampedAssetReference toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("TimestampedAssetReference", offset + 9, (int)mem.byteSize());
      } else {
         return new TimestampedAssetReference(
            hasPath(mem, offset) ? AssetPath.toObject(mem, offset + getValidatedOffset(mem, offset, 1, 9, "Path")) : null,
            hasTimestamp(mem, offset)
               ? PacketIO.readVarString("Timestamp", mem, offset + getValidatedOffset(mem, offset, 5, 9, "Timestamp"), 4096000, PacketIO.UTF8)
               : null
         );
      }
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.path != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.timestamp != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 9;
      if (this.path != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 9);
         varOffset += this.path.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.timestamp != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 9);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.timestamp, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      return varOffset - offset;
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
            if (pathOffset < 0 || pathOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for Path");
            }

            int pos = offset + 9 + pathOffset;
            ValidationResult pathResult = AssetPath.validateStructure(buffer, pos);
            if (!pathResult.isValid()) {
               return ValidationResult.error("Invalid Path: " + pathResult.error());
            }

            pos += AssetPath.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 2) != 0) {
            int timestampOffset = buffer.getIntLE(offset + 5);
            if (timestampOffset < 0 || timestampOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for Timestamp");
            }

            int pos = offset + 9 + timestampOffset;
            int timestampLen = VarInt.peek(buffer, pos);
            if (timestampLen < 0) {
               return ValidationResult.error("Invalid string length for Timestamp");
            }

            if (timestampLen > 4096000) {
               return ValidationResult.error("Timestamp exceeds max length 4096000");
            }

            pos += VarInt.size(timestampLen);
            pos += timestampLen;
            if (pos > buffer.writerIndex()) {
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
