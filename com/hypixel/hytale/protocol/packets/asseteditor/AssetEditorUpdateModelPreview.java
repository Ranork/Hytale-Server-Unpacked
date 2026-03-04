package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.BlockType;
import com.hypixel.hytale.protocol.Model;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorUpdateModelPreview implements Packet, ToClientPacket {
   public static final int PACKET_ID = 355;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 30;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 42;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public AssetPath assetPath;
   @Nullable
   public Model model;
   @Nullable
   public BlockType block;
   @Nullable
   public AssetEditorPreviewCameraSettings camera;

   @Override
   public int getId() {
      return 355;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorUpdateModelPreview() {
   }

   public AssetEditorUpdateModelPreview(
      @Nullable AssetPath assetPath, @Nullable Model model, @Nullable BlockType block, @Nullable AssetEditorPreviewCameraSettings camera
   ) {
      this.assetPath = assetPath;
      this.model = model;
      this.block = block;
      this.camera = camera;
   }

   public AssetEditorUpdateModelPreview(@Nonnull AssetEditorUpdateModelPreview other) {
      this.assetPath = other.assetPath;
      this.model = other.model;
      this.block = other.block;
      this.camera = other.camera;
   }

   @Nonnull
   public static AssetEditorUpdateModelPreview deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetEditorUpdateModelPreview obj = new AssetEditorUpdateModelPreview();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.camera = AssetEditorPreviewCameraSettings.deserialize(buf, offset + 1);
      }

      if ((nullBits & 2) != 0) {
         int varPos0 = offset + 42 + buf.getIntLE(offset + 30);
         obj.assetPath = AssetPath.deserialize(buf, varPos0);
      }

      if ((nullBits & 4) != 0) {
         int varPos1 = offset + 42 + buf.getIntLE(offset + 34);
         obj.model = Model.deserialize(buf, varPos1);
      }

      if ((nullBits & 8) != 0) {
         int varPos2 = offset + 42 + buf.getIntLE(offset + 38);
         obj.block = BlockType.deserialize(buf, varPos2);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 42;
      if ((nullBits & 2) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 30);
         int pos0 = offset + 42 + fieldOffset0;
         pos0 += AssetPath.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 34);
         int pos1 = offset + 42 + fieldOffset1;
         pos1 += Model.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 38);
         int pos2 = offset + 42 + fieldOffset2;
         pos2 += BlockType.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.camera != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.assetPath != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.model != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.block != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      if (this.camera != null) {
         this.camera.serialize(buf);
      } else {
         buf.writeZero(29);
      }

      int assetPathOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int modelOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int blockOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.assetPath != null) {
         buf.setIntLE(assetPathOffsetSlot, buf.writerIndex() - varBlockStart);
         this.assetPath.serialize(buf);
      } else {
         buf.setIntLE(assetPathOffsetSlot, -1);
      }

      if (this.model != null) {
         buf.setIntLE(modelOffsetSlot, buf.writerIndex() - varBlockStart);
         this.model.serialize(buf);
      } else {
         buf.setIntLE(modelOffsetSlot, -1);
      }

      if (this.block != null) {
         buf.setIntLE(blockOffsetSlot, buf.writerIndex() - varBlockStart);
         this.block.serialize(buf);
      } else {
         buf.setIntLE(blockOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 42;
      if (this.assetPath != null) {
         size += this.assetPath.computeSize();
      }

      if (this.model != null) {
         size += this.model.computeSize();
      }

      if (this.block != null) {
         size += this.block.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 42) {
         return ValidationResult.error("Buffer too small: expected at least 42 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 2) != 0) {
            int assetPathOffset = buffer.getIntLE(offset + 30);
            if (assetPathOffset < 0) {
               return ValidationResult.error("Invalid offset for AssetPath");
            }

            int pos = offset + 42 + assetPathOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for AssetPath");
            }

            ValidationResult assetPathResult = AssetPath.validateStructure(buffer, pos);
            if (!assetPathResult.isValid()) {
               return ValidationResult.error("Invalid AssetPath: " + assetPathResult.error());
            }

            pos += AssetPath.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 4) != 0) {
            int modelOffset = buffer.getIntLE(offset + 34);
            if (modelOffset < 0) {
               return ValidationResult.error("Invalid offset for Model");
            }

            int posx = offset + 42 + modelOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Model");
            }

            ValidationResult modelResult = Model.validateStructure(buffer, posx);
            if (!modelResult.isValid()) {
               return ValidationResult.error("Invalid Model: " + modelResult.error());
            }

            posx += Model.computeBytesConsumed(buffer, posx);
         }

         if ((nullBits & 8) != 0) {
            int blockOffset = buffer.getIntLE(offset + 38);
            if (blockOffset < 0) {
               return ValidationResult.error("Invalid offset for Block");
            }

            int posxx = offset + 42 + blockOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Block");
            }

            ValidationResult blockResult = BlockType.validateStructure(buffer, posxx);
            if (!blockResult.isValid()) {
               return ValidationResult.error("Invalid Block: " + blockResult.error());
            }

            posxx += BlockType.computeBytesConsumed(buffer, posxx);
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorUpdateModelPreview clone() {
      AssetEditorUpdateModelPreview copy = new AssetEditorUpdateModelPreview();
      copy.assetPath = this.assetPath != null ? this.assetPath.clone() : null;
      copy.model = this.model != null ? this.model.clone() : null;
      copy.block = this.block != null ? this.block.clone() : null;
      copy.camera = this.camera != null ? this.camera.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorUpdateModelPreview other)
            ? false
            : Objects.equals(this.assetPath, other.assetPath)
               && Objects.equals(this.model, other.model)
               && Objects.equals(this.block, other.block)
               && Objects.equals(this.camera, other.camera);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.assetPath, this.model, this.block, this.camera);
   }
}
