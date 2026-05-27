package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorExportAssetInitialize implements Packet, ToClientPacket {
   public static final int PACKET_ID = 343;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 14;
   public static final int MAX_SIZE = 81920066;
   @Nullable
   public AssetEditorAsset asset;
   @Nullable
   public AssetPath oldPath;
   public int size;
   public boolean failed;

   @Override
   public int getId() {
      return 343;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorExportAssetInitialize() {
   }

   public AssetEditorExportAssetInitialize(@Nullable AssetEditorAsset asset, @Nullable AssetPath oldPath, int size, boolean failed) {
      this.asset = asset;
      this.oldPath = oldPath;
      this.size = size;
      this.failed = failed;
   }

   public AssetEditorExportAssetInitialize(@Nonnull AssetEditorExportAssetInitialize other) {
      this.asset = other.asset;
      this.oldPath = other.oldPath;
      this.size = other.size;
      this.failed = other.failed;
   }

   @Nonnull
   public static AssetEditorExportAssetInitialize deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 14) {
         throw ProtocolException.bufferTooSmall("AssetEditorExportAssetInitialize", 14, buf.readableBytes() - offset);
      } else {
         AssetEditorExportAssetInitialize obj = new AssetEditorExportAssetInitialize();
         byte nullBits = buf.getByte(offset);
         obj.size = buf.getIntLE(offset + 1);
         obj.failed = buf.getByte(offset + 5) != 0;
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 6);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 14) {
               throw ProtocolException.invalidOffset("Asset", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 14 + varPosBase0;
            obj.asset = AssetEditorAsset.deserialize(buf, varPos0);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 10);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 14) {
               throw ProtocolException.invalidOffset("OldPath", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 14 + varPosBase1;
            obj.oldPath = AssetPath.deserialize(buf, varPos1);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 14;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 6);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 14) {
            throw ProtocolException.invalidOffset("Asset", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 14 + fieldOffset0;
         pos0 += AssetEditorAsset.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 10);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 14) {
            throw ProtocolException.invalidOffset("OldPath", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 14 + fieldOffset1;
         pos1 += AssetPath.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 14L;
   }

   @Nullable
   public static AssetEditorAsset getAsset(MemorySegment mem) {
      return getAsset(mem, 0);
   }

   @Nullable
   public static AssetEditorAsset getAsset(MemorySegment mem, int offset) {
      return hasAsset(mem, offset) ? AssetEditorAsset.toObject(mem, offset + getValidatedOffset(mem, offset, 6, 14, "Asset")) : null;
   }

   @Nullable
   public static AssetPath getOldPath(MemorySegment mem) {
      return getOldPath(mem, 0);
   }

   @Nullable
   public static AssetPath getOldPath(MemorySegment mem, int offset) {
      return hasOldPath(mem, offset) ? AssetPath.toObject(mem, offset + getValidatedOffset(mem, offset, 10, 14, "OldPath")) : null;
   }

   public static int getSize(MemorySegment mem) {
      return getSize(mem, 0);
   }

   public static int getSize(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   public static boolean getFailed(MemorySegment mem) {
      return getFailed(mem, 0);
   }

   public static boolean getFailed(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 5));
   }

   public static boolean hasAsset(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasOldPath(MemorySegment mem, int offset) {
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

   public static AssetEditorExportAssetInitialize toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorExportAssetInitialize toObject(MemorySegment mem, int offset) {
      if (offset + 14 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorExportAssetInitialize", offset + 14, (int)mem.byteSize());
      } else {
         return new AssetEditorExportAssetInitialize(
            hasAsset(mem, offset) ? AssetEditorAsset.toObject(mem, offset + getValidatedOffset(mem, offset, 6, 14, "Asset")) : null,
            hasOldPath(mem, offset) ? AssetPath.toObject(mem, offset + getValidatedOffset(mem, offset, 10, 14, "OldPath")) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 5))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.asset != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.oldPath != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.size);
      buf.writeByte(this.failed ? 1 : 0);
      int assetOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int oldPathOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.asset != null) {
         buf.setIntLE(assetOffsetSlot, buf.writerIndex() - varBlockStart);
         this.asset.serialize(buf);
      } else {
         buf.setIntLE(assetOffsetSlot, -1);
      }

      if (this.oldPath != null) {
         buf.setIntLE(oldPathOffsetSlot, buf.writerIndex() - varBlockStart);
         this.oldPath.serialize(buf);
      } else {
         buf.setIntLE(oldPathOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.asset != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.oldPath != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.size);
      mem.set(PacketIO.PROTO_BOOL, offset + 5, this.failed);
      int varOffset = offset + 14;
      if (this.asset != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), varOffset - offset - 14);
         varOffset += this.asset.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 6), -1);
      }

      if (this.oldPath != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), varOffset - offset - 14);
         varOffset += this.oldPath.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 14;
      if (this.asset != null) {
         size += this.asset.computeSize();
      }

      if (this.oldPath != null) {
         size += this.oldPath.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 14) {
         return ValidationResult.error("Buffer too small: expected at least 14 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int assetOffset = buffer.getIntLE(offset + 6);
            if (assetOffset < 0 || assetOffset > buffer.writerIndex() - offset - 14) {
               return ValidationResult.error("Invalid offset for Asset");
            }

            int pos = offset + 14 + assetOffset;
            ValidationResult assetResult = AssetEditorAsset.validateStructure(buffer, pos);
            if (!assetResult.isValid()) {
               return ValidationResult.error("Invalid Asset: " + assetResult.error());
            }

            pos += AssetEditorAsset.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 2) != 0) {
            int oldPathOffset = buffer.getIntLE(offset + 10);
            if (oldPathOffset < 0 || oldPathOffset > buffer.writerIndex() - offset - 14) {
               return ValidationResult.error("Invalid offset for OldPath");
            }

            int pos = offset + 14 + oldPathOffset;
            ValidationResult oldPathResult = AssetPath.validateStructure(buffer, pos);
            if (!oldPathResult.isValid()) {
               return ValidationResult.error("Invalid OldPath: " + oldPathResult.error());
            }

            pos += AssetPath.computeBytesConsumed(buffer, pos);
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorExportAssetInitialize clone() {
      AssetEditorExportAssetInitialize copy = new AssetEditorExportAssetInitialize();
      copy.asset = this.asset != null ? this.asset.clone() : null;
      copy.oldPath = this.oldPath != null ? this.oldPath.clone() : null;
      copy.size = this.size;
      copy.failed = this.failed;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorExportAssetInitialize other)
            ? false
            : Objects.equals(this.asset, other.asset) && Objects.equals(this.oldPath, other.oldPath) && this.size == other.size && this.failed == other.failed;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.asset, this.oldPath, this.size, this.failed);
   }
}
