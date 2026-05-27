package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.BlockType;
import com.hypixel.hytale.protocol.Model;
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

public class AssetEditorUpdateModelPreview implements Packet, ToClientPacket {
   public static final int PACKET_ID = 355;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 29;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 41;
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
      if (buf.readableBytes() - offset < 41) {
         throw ProtocolException.bufferTooSmall("AssetEditorUpdateModelPreview", 41, buf.readableBytes() - offset);
      } else {
         AssetEditorUpdateModelPreview obj = new AssetEditorUpdateModelPreview();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.camera = AssetEditorPreviewCameraSettings.deserialize(buf, offset + 1);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 29);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 41) {
               throw ProtocolException.invalidOffset("AssetPath", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 41 + varPosBase0;
            obj.assetPath = AssetPath.deserialize(buf, varPos0);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 33);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 41) {
               throw ProtocolException.invalidOffset("Model", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 41 + varPosBase1;
            obj.model = Model.deserialize(buf, varPos1);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 37);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 41) {
               throw ProtocolException.invalidOffset("Block", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 41 + varPosBase2;
            obj.block = BlockType.deserialize(buf, varPos2);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 41;
      if ((nullBits & 2) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 29);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 41) {
            throw ProtocolException.invalidOffset("AssetPath", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 41 + fieldOffset0;
         pos0 += AssetPath.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 33);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 41) {
            throw ProtocolException.invalidOffset("Model", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 41 + fieldOffset1;
         pos1 += Model.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 37);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 41) {
            throw ProtocolException.invalidOffset("Block", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 41 + fieldOffset2;
         pos2 += BlockType.computeBytesConsumed(buf, pos2);
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 41L;
   }

   @Nullable
   public static AssetPath getAssetPath(MemorySegment mem) {
      return getAssetPath(mem, 0);
   }

   @Nullable
   public static AssetPath getAssetPath(MemorySegment mem, int offset) {
      return hasAssetPath(mem, offset) ? AssetPath.toObject(mem, offset + getValidatedOffset(mem, offset, 29, 41, "AssetPath")) : null;
   }

   @Nullable
   public static Model getModel(MemorySegment mem) {
      return getModel(mem, 0);
   }

   @Nullable
   public static Model getModel(MemorySegment mem, int offset) {
      return hasModel(mem, offset) ? Model.toObject(mem, offset + getValidatedOffset(mem, offset, 33, 41, "Model")) : null;
   }

   @Nullable
   public static BlockType getBlock(MemorySegment mem) {
      return getBlock(mem, 0);
   }

   @Nullable
   public static BlockType getBlock(MemorySegment mem, int offset) {
      return hasBlock(mem, offset) ? BlockType.toObject(mem, offset + getValidatedOffset(mem, offset, 37, 41, "Block")) : null;
   }

   @Nullable
   public static AssetEditorPreviewCameraSettings getCamera(MemorySegment mem) {
      return getCamera(mem, 0);
   }

   @Nullable
   public static AssetEditorPreviewCameraSettings getCamera(MemorySegment mem, int offset) {
      return hasCamera(mem, offset) ? AssetEditorPreviewCameraSettings.toObject(mem, offset + 1) : null;
   }

   public static boolean hasCamera(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasAssetPath(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasModel(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasBlock(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static AssetEditorUpdateModelPreview toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorUpdateModelPreview toObject(MemorySegment mem, int offset) {
      if (offset + 41 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorUpdateModelPreview", offset + 41, (int)mem.byteSize());
      } else {
         return new AssetEditorUpdateModelPreview(
            hasAssetPath(mem, offset) ? AssetPath.toObject(mem, offset + getValidatedOffset(mem, offset, 29, 41, "AssetPath")) : null,
            hasModel(mem, offset) ? Model.toObject(mem, offset + getValidatedOffset(mem, offset, 33, 41, "Model")) : null,
            hasBlock(mem, offset) ? BlockType.toObject(mem, offset + getValidatedOffset(mem, offset, 37, 41, "Block")) : null,
            hasCamera(mem, offset) ? AssetEditorPreviewCameraSettings.toObject(mem, offset + 1) : null
         );
      }
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
         buf.writeZero(28);
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
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

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.camera != null) {
         this.camera.serialize(mem, offset + 1);
      } else {
         mem.asSlice(offset + 1, 28L).fill((byte)0);
      }

      int varOffset = offset + 41;
      if (this.assetPath != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 29), varOffset - offset - 41);
         varOffset += this.assetPath.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 29), -1);
      }

      if (this.model != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 33), varOffset - offset - 41);
         varOffset += this.model.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 33), -1);
      }

      if (this.block != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 37), varOffset - offset - 41);
         varOffset += this.block.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 37), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 41;
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
      if (buffer.readableBytes() - offset < 41) {
         return ValidationResult.error("Buffer too small: expected at least 41 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 2) != 0) {
            int assetPathOffset = buffer.getIntLE(offset + 29);
            if (assetPathOffset < 0 || assetPathOffset > buffer.writerIndex() - offset - 41) {
               return ValidationResult.error("Invalid offset for AssetPath");
            }

            int pos = offset + 41 + assetPathOffset;
            ValidationResult assetPathResult = AssetPath.validateStructure(buffer, pos);
            if (!assetPathResult.isValid()) {
               return ValidationResult.error("Invalid AssetPath: " + assetPathResult.error());
            }

            pos += AssetPath.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 4) != 0) {
            int modelOffset = buffer.getIntLE(offset + 33);
            if (modelOffset < 0 || modelOffset > buffer.writerIndex() - offset - 41) {
               return ValidationResult.error("Invalid offset for Model");
            }

            int pos = offset + 41 + modelOffset;
            ValidationResult modelResult = Model.validateStructure(buffer, pos);
            if (!modelResult.isValid()) {
               return ValidationResult.error("Invalid Model: " + modelResult.error());
            }

            pos += Model.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 8) != 0) {
            int blockOffset = buffer.getIntLE(offset + 37);
            if (blockOffset < 0 || blockOffset > buffer.writerIndex() - offset - 41) {
               return ValidationResult.error("Invalid offset for Block");
            }

            int pos = offset + 41 + blockOffset;
            ValidationResult blockResult = BlockType.validateStructure(buffer, pos);
            if (!blockResult.isValid()) {
               return ValidationResult.error("Invalid Block: " + blockResult.error());
            }

            pos += BlockType.computeBytesConsumed(buffer, pos);
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
