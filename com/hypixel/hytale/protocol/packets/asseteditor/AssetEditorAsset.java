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
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("AssetEditorAsset", 9, buf.readableBytes() - offset);
      } else {
         AssetEditorAsset obj = new AssetEditorAsset();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("Hash", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 9 + varPosBase0;
            int hashLen = VarInt.peek(buf, varPos0);
            if (hashLen < 0) {
               throw ProtocolException.invalidVarInt("Hash");
            }

            int hashVarIntLen = VarInt.size(hashLen);
            if (hashLen > 4096000) {
               throw ProtocolException.stringTooLong("Hash", hashLen, 4096000);
            }

            if (varPos0 + hashVarIntLen + hashLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Hash", varPos0 + hashVarIntLen + hashLen, buf.readableBytes());
            }

            obj.hash = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("Path", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 9 + varPosBase1;
            obj.path = AssetPath.deserialize(buf, varPos1);
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
            throw ProtocolException.invalidOffset("Hash", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 9 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("Path", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 9 + fieldOffset1;
         pos1 += AssetPath.computeBytesConsumed(buf, pos1);
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
   public static String getHash(MemorySegment mem) {
      return getHash(mem, 0);
   }

   @Nullable
   public static String getHash(MemorySegment mem, int offset) {
      return hasHash(mem, offset) ? PacketIO.readVarString("Hash", mem, offset + getValidatedOffset(mem, offset, 1, 9, "Hash"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static AssetPath getPath(MemorySegment mem) {
      return getPath(mem, 0);
   }

   @Nullable
   public static AssetPath getPath(MemorySegment mem, int offset) {
      return hasPath(mem, offset) ? AssetPath.toObject(mem, offset + getValidatedOffset(mem, offset, 5, 9, "Path")) : null;
   }

   public static boolean hasHash(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasPath(MemorySegment mem, int offset) {
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

   public static AssetEditorAsset toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorAsset toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorAsset", offset + 9, (int)mem.byteSize());
      } else {
         return new AssetEditorAsset(
            hasHash(mem, offset) ? PacketIO.readVarString("Hash", mem, offset + getValidatedOffset(mem, offset, 1, 9, "Hash"), 4096000, PacketIO.UTF8) : null,
            hasPath(mem, offset) ? AssetPath.toObject(mem, offset + getValidatedOffset(mem, offset, 5, 9, "Path")) : null
         );
      }
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.hash != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.path != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 9;
      if (this.hash != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 9);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.hash, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.path != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 9);
         varOffset += this.path.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      return varOffset - offset;
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
            if (hashOffset < 0 || hashOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for Hash");
            }

            int pos = offset + 9 + hashOffset;
            int hashLen = VarInt.peek(buffer, pos);
            if (hashLen < 0) {
               return ValidationResult.error("Invalid string length for Hash");
            }

            if (hashLen > 4096000) {
               return ValidationResult.error("Hash exceeds max length 4096000");
            }

            pos += VarInt.size(hashLen);
            pos += hashLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Hash");
            }
         }

         if ((nullBits & 2) != 0) {
            int pathOffset = buffer.getIntLE(offset + 5);
            if (pathOffset < 0 || pathOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for Path");
            }

            int posx = offset + 9 + pathOffset;
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
