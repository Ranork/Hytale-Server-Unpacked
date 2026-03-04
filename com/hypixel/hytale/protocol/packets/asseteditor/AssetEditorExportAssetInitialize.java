package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
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
      AssetEditorExportAssetInitialize obj = new AssetEditorExportAssetInitialize();
      byte nullBits = buf.getByte(offset);
      obj.size = buf.getIntLE(offset + 1);
      obj.failed = buf.getByte(offset + 5) != 0;
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 14 + buf.getIntLE(offset + 6);
         obj.asset = AssetEditorAsset.deserialize(buf, varPos0);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 14 + buf.getIntLE(offset + 10);
         obj.oldPath = AssetPath.deserialize(buf, varPos1);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 14;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 6);
         int pos0 = offset + 14 + fieldOffset0;
         pos0 += AssetEditorAsset.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 10);
         int pos1 = offset + 14 + fieldOffset1;
         pos1 += AssetPath.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
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
            if (assetOffset < 0) {
               return ValidationResult.error("Invalid offset for Asset");
            }

            int pos = offset + 14 + assetOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Asset");
            }

            ValidationResult assetResult = AssetEditorAsset.validateStructure(buffer, pos);
            if (!assetResult.isValid()) {
               return ValidationResult.error("Invalid Asset: " + assetResult.error());
            }

            pos += AssetEditorAsset.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 2) != 0) {
            int oldPathOffset = buffer.getIntLE(offset + 10);
            if (oldPathOffset < 0) {
               return ValidationResult.error("Invalid offset for OldPath");
            }

            int posx = offset + 14 + oldPathOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for OldPath");
            }

            ValidationResult oldPathResult = AssetPath.validateStructure(buffer, posx);
            if (!oldPathResult.isValid()) {
               return ValidationResult.error("Invalid OldPath: " + oldPathResult.error());
            }

            posx += AssetPath.computeBytesConsumed(buffer, posx);
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
