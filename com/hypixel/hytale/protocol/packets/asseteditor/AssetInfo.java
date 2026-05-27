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

public class AssetInfo {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 11;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 23;
   public static final int MAX_SIZE = 81920066;
   @Nullable
   public AssetPath path;
   @Nullable
   public AssetPath oldPath;
   public boolean isDeleted;
   public boolean isNew;
   public long lastModificationDate;
   @Nullable
   public String lastModificationUsername;

   public AssetInfo() {
   }

   public AssetInfo(
      @Nullable AssetPath path,
      @Nullable AssetPath oldPath,
      boolean isDeleted,
      boolean isNew,
      long lastModificationDate,
      @Nullable String lastModificationUsername
   ) {
      this.path = path;
      this.oldPath = oldPath;
      this.isDeleted = isDeleted;
      this.isNew = isNew;
      this.lastModificationDate = lastModificationDate;
      this.lastModificationUsername = lastModificationUsername;
   }

   public AssetInfo(@Nonnull AssetInfo other) {
      this.path = other.path;
      this.oldPath = other.oldPath;
      this.isDeleted = other.isDeleted;
      this.isNew = other.isNew;
      this.lastModificationDate = other.lastModificationDate;
      this.lastModificationUsername = other.lastModificationUsername;
   }

   @Nonnull
   public static AssetInfo deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 23) {
         throw ProtocolException.bufferTooSmall("AssetInfo", 23, buf.readableBytes() - offset);
      } else {
         AssetInfo obj = new AssetInfo();
         byte nullBits = buf.getByte(offset);
         obj.isDeleted = buf.getByte(offset + 1) != 0;
         obj.isNew = buf.getByte(offset + 2) != 0;
         obj.lastModificationDate = buf.getLongLE(offset + 3);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 11);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 23) {
               throw ProtocolException.invalidOffset("Path", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 23 + varPosBase0;
            obj.path = AssetPath.deserialize(buf, varPos0);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 15);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 23) {
               throw ProtocolException.invalidOffset("OldPath", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 23 + varPosBase1;
            obj.oldPath = AssetPath.deserialize(buf, varPos1);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 19);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 23) {
               throw ProtocolException.invalidOffset("LastModificationUsername", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 23 + varPosBase2;
            int lastModificationUsernameLen = VarInt.peek(buf, varPos2);
            if (lastModificationUsernameLen < 0) {
               throw ProtocolException.invalidVarInt("LastModificationUsername");
            }

            int lastModificationUsernameVarIntLen = VarInt.size(lastModificationUsernameLen);
            if (lastModificationUsernameLen > 4096000) {
               throw ProtocolException.stringTooLong("LastModificationUsername", lastModificationUsernameLen, 4096000);
            }

            if (varPos2 + lastModificationUsernameVarIntLen + lastModificationUsernameLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall(
                  "LastModificationUsername", varPos2 + lastModificationUsernameVarIntLen + lastModificationUsernameLen, buf.readableBytes()
               );
            }

            obj.lastModificationUsername = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 23;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 11);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 23) {
            throw ProtocolException.invalidOffset("Path", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 23 + fieldOffset0;
         pos0 += AssetPath.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 15);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 23) {
            throw ProtocolException.invalidOffset("OldPath", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 23 + fieldOffset1;
         pos1 += AssetPath.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 19);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 23) {
            throw ProtocolException.invalidOffset("LastModificationUsername", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 23 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 23L;
   }

   @Nullable
   public static AssetPath getPath(MemorySegment mem) {
      return getPath(mem, 0);
   }

   @Nullable
   public static AssetPath getPath(MemorySegment mem, int offset) {
      return hasPath(mem, offset) ? AssetPath.toObject(mem, offset + getValidatedOffset(mem, offset, 11, 23, "Path")) : null;
   }

   @Nullable
   public static AssetPath getOldPath(MemorySegment mem) {
      return getOldPath(mem, 0);
   }

   @Nullable
   public static AssetPath getOldPath(MemorySegment mem, int offset) {
      return hasOldPath(mem, offset) ? AssetPath.toObject(mem, offset + getValidatedOffset(mem, offset, 15, 23, "OldPath")) : null;
   }

   public static boolean getIsDeleted(MemorySegment mem) {
      return getIsDeleted(mem, 0);
   }

   public static boolean getIsDeleted(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   public static boolean getIsNew(MemorySegment mem) {
      return getIsNew(mem, 0);
   }

   public static boolean getIsNew(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2));
   }

   public static long getLastModificationDate(MemorySegment mem) {
      return getLastModificationDate(mem, 0);
   }

   public static long getLastModificationDate(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_LONG, (long)(offset + 3));
   }

   @Nullable
   public static String getLastModificationUsername(MemorySegment mem) {
      return getLastModificationUsername(mem, 0);
   }

   @Nullable
   public static String getLastModificationUsername(MemorySegment mem, int offset) {
      return hasLastModificationUsername(mem, offset)
         ? PacketIO.readVarString(
            "LastModificationUsername", mem, offset + getValidatedOffset(mem, offset, 19, 23, "LastModificationUsername"), 4096000, PacketIO.UTF8
         )
         : null;
   }

   public static boolean hasPath(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasOldPath(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasLastModificationUsername(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static AssetInfo toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetInfo toObject(MemorySegment mem, int offset) {
      if (offset + 23 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetInfo", offset + 23, (int)mem.byteSize());
      } else {
         return new AssetInfo(
            hasPath(mem, offset) ? AssetPath.toObject(mem, offset + getValidatedOffset(mem, offset, 11, 23, "Path")) : null,
            hasOldPath(mem, offset) ? AssetPath.toObject(mem, offset + getValidatedOffset(mem, offset, 15, 23, "OldPath")) : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2)),
            mem.get(PacketIO.PROTO_LONG, (long)(offset + 3)),
            hasLastModificationUsername(mem, offset)
               ? PacketIO.readVarString(
                  "LastModificationUsername", mem, offset + getValidatedOffset(mem, offset, 19, 23, "LastModificationUsername"), 4096000, PacketIO.UTF8
               )
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

      if (this.oldPath != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.lastModificationUsername != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.isDeleted ? 1 : 0);
      buf.writeByte(this.isNew ? 1 : 0);
      buf.writeLongLE(this.lastModificationDate);
      int pathOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int oldPathOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int lastModificationUsernameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.path != null) {
         buf.setIntLE(pathOffsetSlot, buf.writerIndex() - varBlockStart);
         this.path.serialize(buf);
      } else {
         buf.setIntLE(pathOffsetSlot, -1);
      }

      if (this.oldPath != null) {
         buf.setIntLE(oldPathOffsetSlot, buf.writerIndex() - varBlockStart);
         this.oldPath.serialize(buf);
      } else {
         buf.setIntLE(oldPathOffsetSlot, -1);
      }

      if (this.lastModificationUsername != null) {
         buf.setIntLE(lastModificationUsernameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.lastModificationUsername, 4096000);
      } else {
         buf.setIntLE(lastModificationUsernameOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.path != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.oldPath != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.lastModificationUsername != null) {
         nullBits = (byte)(nullBits | 4);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.isDeleted);
      mem.set(PacketIO.PROTO_BOOL, offset + 2, this.isNew);
      mem.set(PacketIO.PROTO_LONG, (long)(offset + 3), this.lastModificationDate);
      int varOffset = offset + 23;
      if (this.path != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 11), varOffset - offset - 23);
         varOffset += this.path.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 11), -1);
      }

      if (this.oldPath != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 15), varOffset - offset - 23);
         varOffset += this.oldPath.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 15), -1);
      }

      if (this.lastModificationUsername != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 19), varOffset - offset - 23);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.lastModificationUsername, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 19), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 23;
      if (this.path != null) {
         size += this.path.computeSize();
      }

      if (this.oldPath != null) {
         size += this.oldPath.computeSize();
      }

      if (this.lastModificationUsername != null) {
         size += PacketIO.stringSize(this.lastModificationUsername);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 23) {
         return ValidationResult.error("Buffer too small: expected at least 23 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int pathOffset = buffer.getIntLE(offset + 11);
            if (pathOffset < 0 || pathOffset > buffer.writerIndex() - offset - 23) {
               return ValidationResult.error("Invalid offset for Path");
            }

            int pos = offset + 23 + pathOffset;
            ValidationResult pathResult = AssetPath.validateStructure(buffer, pos);
            if (!pathResult.isValid()) {
               return ValidationResult.error("Invalid Path: " + pathResult.error());
            }

            pos += AssetPath.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 2) != 0) {
            int oldPathOffset = buffer.getIntLE(offset + 15);
            if (oldPathOffset < 0 || oldPathOffset > buffer.writerIndex() - offset - 23) {
               return ValidationResult.error("Invalid offset for OldPath");
            }

            int pos = offset + 23 + oldPathOffset;
            ValidationResult oldPathResult = AssetPath.validateStructure(buffer, pos);
            if (!oldPathResult.isValid()) {
               return ValidationResult.error("Invalid OldPath: " + oldPathResult.error());
            }

            pos += AssetPath.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 4) != 0) {
            int lastModificationUsernameOffset = buffer.getIntLE(offset + 19);
            if (lastModificationUsernameOffset < 0 || lastModificationUsernameOffset > buffer.writerIndex() - offset - 23) {
               return ValidationResult.error("Invalid offset for LastModificationUsername");
            }

            int pos = offset + 23 + lastModificationUsernameOffset;
            int lastModificationUsernameLen = VarInt.peek(buffer, pos);
            if (lastModificationUsernameLen < 0) {
               return ValidationResult.error("Invalid string length for LastModificationUsername");
            }

            if (lastModificationUsernameLen > 4096000) {
               return ValidationResult.error("LastModificationUsername exceeds max length 4096000");
            }

            pos += VarInt.size(lastModificationUsernameLen);
            pos += lastModificationUsernameLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading LastModificationUsername");
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetInfo clone() {
      AssetInfo copy = new AssetInfo();
      copy.path = this.path != null ? this.path.clone() : null;
      copy.oldPath = this.oldPath != null ? this.oldPath.clone() : null;
      copy.isDeleted = this.isDeleted;
      copy.isNew = this.isNew;
      copy.lastModificationDate = this.lastModificationDate;
      copy.lastModificationUsername = this.lastModificationUsername;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetInfo other)
            ? false
            : Objects.equals(this.path, other.path)
               && Objects.equals(this.oldPath, other.oldPath)
               && this.isDeleted == other.isDeleted
               && this.isNew == other.isNew
               && this.lastModificationDate == other.lastModificationDate
               && Objects.equals(this.lastModificationUsername, other.lastModificationUsername);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.path, this.oldPath, this.isDeleted, this.isNew, this.lastModificationDate, this.lastModificationUsername);
   }
}
