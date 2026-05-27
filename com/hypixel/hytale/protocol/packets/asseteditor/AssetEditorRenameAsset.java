package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorRenameAsset implements Packet, ToServerPacket {
   public static final int PACKET_ID = 328;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 65536051;
   public int token;
   @Nullable
   public AssetPath path;
   @Nullable
   public AssetPath newPath;

   @Override
   public int getId() {
      return 328;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorRenameAsset() {
   }

   public AssetEditorRenameAsset(int token, @Nullable AssetPath path, @Nullable AssetPath newPath) {
      this.token = token;
      this.path = path;
      this.newPath = newPath;
   }

   public AssetEditorRenameAsset(@Nonnull AssetEditorRenameAsset other) {
      this.token = other.token;
      this.path = other.path;
      this.newPath = other.newPath;
   }

   @Nonnull
   public static AssetEditorRenameAsset deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 13) {
         throw ProtocolException.bufferTooSmall("AssetEditorRenameAsset", 13, buf.readableBytes() - offset);
      } else {
         AssetEditorRenameAsset obj = new AssetEditorRenameAsset();
         byte nullBits = buf.getByte(offset);
         obj.token = buf.getIntLE(offset + 1);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 5);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("Path", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 13 + varPosBase0;
            obj.path = AssetPath.deserialize(buf, varPos0);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 9);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("NewPath", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 13 + varPosBase1;
            obj.newPath = AssetPath.deserialize(buf, varPos1);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 5);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("Path", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 13 + fieldOffset0;
         pos0 += AssetPath.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 9);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("NewPath", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 13 + fieldOffset1;
         pos1 += AssetPath.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 13L;
   }

   public static int getToken(MemorySegment mem) {
      return getToken(mem, 0);
   }

   public static int getToken(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static AssetPath getPath(MemorySegment mem) {
      return getPath(mem, 0);
   }

   @Nullable
   public static AssetPath getPath(MemorySegment mem, int offset) {
      return hasPath(mem, offset) ? AssetPath.toObject(mem, offset + getValidatedOffset(mem, offset, 5, 13, "Path")) : null;
   }

   @Nullable
   public static AssetPath getNewPath(MemorySegment mem) {
      return getNewPath(mem, 0);
   }

   @Nullable
   public static AssetPath getNewPath(MemorySegment mem, int offset) {
      return hasNewPath(mem, offset) ? AssetPath.toObject(mem, offset + getValidatedOffset(mem, offset, 9, 13, "NewPath")) : null;
   }

   public static boolean hasPath(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasNewPath(MemorySegment mem, int offset) {
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

   public static AssetEditorRenameAsset toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorRenameAsset toObject(MemorySegment mem, int offset) {
      if (offset + 13 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorRenameAsset", offset + 13, (int)mem.byteSize());
      } else {
         return new AssetEditorRenameAsset(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            hasPath(mem, offset) ? AssetPath.toObject(mem, offset + getValidatedOffset(mem, offset, 5, 13, "Path")) : null,
            hasNewPath(mem, offset) ? AssetPath.toObject(mem, offset + getValidatedOffset(mem, offset, 9, 13, "NewPath")) : null
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.path != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.newPath != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.token);
      int pathOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int newPathOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.path != null) {
         buf.setIntLE(pathOffsetSlot, buf.writerIndex() - varBlockStart);
         this.path.serialize(buf);
      } else {
         buf.setIntLE(pathOffsetSlot, -1);
      }

      if (this.newPath != null) {
         buf.setIntLE(newPathOffsetSlot, buf.writerIndex() - varBlockStart);
         this.newPath.serialize(buf);
      } else {
         buf.setIntLE(newPathOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.path != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.newPath != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.token);
      int varOffset = offset + 13;
      if (this.path != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 13);
         varOffset += this.path.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      if (this.newPath != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 13);
         varOffset += this.newPath.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 13;
      if (this.path != null) {
         size += this.path.computeSize();
      }

      if (this.newPath != null) {
         size += this.newPath.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int pathOffset = buffer.getIntLE(offset + 5);
            if (pathOffset < 0 || pathOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for Path");
            }

            int pos = offset + 13 + pathOffset;
            ValidationResult pathResult = AssetPath.validateStructure(buffer, pos);
            if (!pathResult.isValid()) {
               return ValidationResult.error("Invalid Path: " + pathResult.error());
            }

            pos += AssetPath.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 2) != 0) {
            int newPathOffset = buffer.getIntLE(offset + 9);
            if (newPathOffset < 0 || newPathOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for NewPath");
            }

            int pos = offset + 13 + newPathOffset;
            ValidationResult newPathResult = AssetPath.validateStructure(buffer, pos);
            if (!newPathResult.isValid()) {
               return ValidationResult.error("Invalid NewPath: " + newPathResult.error());
            }

            pos += AssetPath.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorRenameAsset clone() {
      AssetEditorRenameAsset copy = new AssetEditorRenameAsset();
      copy.token = this.token;
      copy.path = this.path != null ? this.path.clone() : null;
      copy.newPath = this.newPath != null ? this.newPath.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorRenameAsset other)
            ? false
            : this.token == other.token && Objects.equals(this.path, other.path) && Objects.equals(this.newPath, other.newPath);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.token, this.path, this.newPath);
   }
}
