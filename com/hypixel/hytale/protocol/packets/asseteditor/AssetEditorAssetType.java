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

public class AssetEditorAssetType {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 3;
   public static final int VARIABLE_FIELD_COUNT = 4;
   public static final int VARIABLE_BLOCK_START = 19;
   public static final int MAX_SIZE = 65536039;
   @Nullable
   public String id;
   @Nullable
   public String icon;
   public boolean isColoredIcon;
   @Nullable
   public String path;
   @Nullable
   public String fileExtension;
   @Nonnull
   public AssetEditorEditorType editorType = AssetEditorEditorType.None;

   public AssetEditorAssetType() {
   }

   public AssetEditorAssetType(
      @Nullable String id,
      @Nullable String icon,
      boolean isColoredIcon,
      @Nullable String path,
      @Nullable String fileExtension,
      @Nonnull AssetEditorEditorType editorType
   ) {
      this.id = id;
      this.icon = icon;
      this.isColoredIcon = isColoredIcon;
      this.path = path;
      this.fileExtension = fileExtension;
      this.editorType = editorType;
   }

   public AssetEditorAssetType(@Nonnull AssetEditorAssetType other) {
      this.id = other.id;
      this.icon = other.icon;
      this.isColoredIcon = other.isColoredIcon;
      this.path = other.path;
      this.fileExtension = other.fileExtension;
      this.editorType = other.editorType;
   }

   @Nonnull
   public static AssetEditorAssetType deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 19) {
         throw ProtocolException.bufferTooSmall("AssetEditorAssetType", 19, buf.readableBytes() - offset);
      } else {
         AssetEditorAssetType obj = new AssetEditorAssetType();
         byte nullBits = buf.getByte(offset);
         obj.isColoredIcon = buf.getByte(offset + 1) != 0;
         obj.editorType = AssetEditorEditorType.fromValue(buf.getByte(offset + 2));
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 3);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 19) {
               throw ProtocolException.invalidOffset("Id", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 19 + varPosBase0;
            int idLen = VarInt.peek(buf, varPos0);
            if (idLen < 0) {
               throw ProtocolException.invalidVarInt("Id");
            }

            int idVarIntLen = VarInt.size(idLen);
            if (idLen > 4096000) {
               throw ProtocolException.stringTooLong("Id", idLen, 4096000);
            }

            if (varPos0 + idVarIntLen + idLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Id", varPos0 + idVarIntLen + idLen, buf.readableBytes());
            }

            obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 7);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 19) {
               throw ProtocolException.invalidOffset("Icon", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 19 + varPosBase1;
            int iconLen = VarInt.peek(buf, varPos1);
            if (iconLen < 0) {
               throw ProtocolException.invalidVarInt("Icon");
            }

            int iconVarIntLen = VarInt.size(iconLen);
            if (iconLen > 4096000) {
               throw ProtocolException.stringTooLong("Icon", iconLen, 4096000);
            }

            if (varPos1 + iconVarIntLen + iconLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Icon", varPos1 + iconVarIntLen + iconLen, buf.readableBytes());
            }

            obj.icon = PacketIO.readVarString(buf, varPos1, PacketIO.UTF8);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 11);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 19) {
               throw ProtocolException.invalidOffset("Path", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 19 + varPosBase2;
            int pathLen = VarInt.peek(buf, varPos2);
            if (pathLen < 0) {
               throw ProtocolException.invalidVarInt("Path");
            }

            int pathVarIntLen = VarInt.size(pathLen);
            if (pathLen > 4096000) {
               throw ProtocolException.stringTooLong("Path", pathLen, 4096000);
            }

            if (varPos2 + pathVarIntLen + pathLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Path", varPos2 + pathVarIntLen + pathLen, buf.readableBytes());
            }

            obj.path = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
         }

         if ((nullBits & 8) != 0) {
            int varPosBase3 = buf.getIntLE(offset + 15);
            if (varPosBase3 < 0 || varPosBase3 > buf.writerIndex() - offset - 19) {
               throw ProtocolException.invalidOffset("FileExtension", varPosBase3, buf.readableBytes());
            }

            int varPos3 = offset + 19 + varPosBase3;
            int fileExtensionLen = VarInt.peek(buf, varPos3);
            if (fileExtensionLen < 0) {
               throw ProtocolException.invalidVarInt("FileExtension");
            }

            int fileExtensionVarIntLen = VarInt.size(fileExtensionLen);
            if (fileExtensionLen > 4096000) {
               throw ProtocolException.stringTooLong("FileExtension", fileExtensionLen, 4096000);
            }

            if (varPos3 + fileExtensionVarIntLen + fileExtensionLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("FileExtension", varPos3 + fileExtensionVarIntLen + fileExtensionLen, buf.readableBytes());
            }

            obj.fileExtension = PacketIO.readVarString(buf, varPos3, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 19;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 3);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 19) {
            throw ProtocolException.invalidOffset("Id", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 19 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 7);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 19) {
            throw ProtocolException.invalidOffset("Icon", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 19 + fieldOffset1;
         int sl = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(sl) + sl;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 11);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 19) {
            throw ProtocolException.invalidOffset("Path", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 19 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset3 = buf.getIntLE(offset + 15);
         if (fieldOffset3 < 0 || fieldOffset3 > buf.writerIndex() - offset - 19) {
            throw ProtocolException.invalidOffset("FileExtension", fieldOffset3, maxEnd);
         }

         int pos3 = offset + 19 + fieldOffset3;
         int sl = VarInt.peek(buf, pos3);
         pos3 += VarInt.size(sl) + sl;
         if (pos3 - offset > maxEnd) {
            maxEnd = pos3 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 19L;
   }

   @Nullable
   public static String getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   @Nullable
   public static String getId(MemorySegment mem, int offset) {
      return hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 3, 19, "Id"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static String getIcon(MemorySegment mem) {
      return getIcon(mem, 0);
   }

   @Nullable
   public static String getIcon(MemorySegment mem, int offset) {
      return hasIcon(mem, offset) ? PacketIO.readVarString("Icon", mem, offset + getValidatedOffset(mem, offset, 7, 19, "Icon"), 4096000, PacketIO.UTF8) : null;
   }

   public static boolean getIsColoredIcon(MemorySegment mem) {
      return getIsColoredIcon(mem, 0);
   }

   public static boolean getIsColoredIcon(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   @Nullable
   public static String getPath(MemorySegment mem) {
      return getPath(mem, 0);
   }

   @Nullable
   public static String getPath(MemorySegment mem, int offset) {
      return hasPath(mem, offset)
         ? PacketIO.readVarString("Path", mem, offset + getValidatedOffset(mem, offset, 11, 19, "Path"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String getFileExtension(MemorySegment mem) {
      return getFileExtension(mem, 0);
   }

   @Nullable
   public static String getFileExtension(MemorySegment mem, int offset) {
      return hasFileExtension(mem, offset)
         ? PacketIO.readVarString("FileExtension", mem, offset + getValidatedOffset(mem, offset, 15, 19, "FileExtension"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static AssetEditorEditorType getEditorType(MemorySegment mem) {
      return getEditorType(mem, 0);
   }

   public static AssetEditorEditorType getEditorType(MemorySegment mem, int offset) {
      return AssetEditorEditorType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2)));
   }

   public static boolean hasId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasIcon(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasPath(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasFileExtension(MemorySegment mem, int offset) {
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

   public static AssetEditorAssetType toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorAssetType toObject(MemorySegment mem, int offset) {
      if (offset + 19 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorAssetType", offset + 19, (int)mem.byteSize());
      } else {
         return new AssetEditorAssetType(
            hasId(mem, offset) ? PacketIO.readVarString("Id", mem, offset + getValidatedOffset(mem, offset, 3, 19, "Id"), 4096000, PacketIO.UTF8) : null,
            hasIcon(mem, offset) ? PacketIO.readVarString("Icon", mem, offset + getValidatedOffset(mem, offset, 7, 19, "Icon"), 4096000, PacketIO.UTF8) : null,
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1)),
            hasPath(mem, offset) ? PacketIO.readVarString("Path", mem, offset + getValidatedOffset(mem, offset, 11, 19, "Path"), 4096000, PacketIO.UTF8) : null,
            hasFileExtension(mem, offset)
               ? PacketIO.readVarString("FileExtension", mem, offset + getValidatedOffset(mem, offset, 15, 19, "FileExtension"), 4096000, PacketIO.UTF8)
               : null,
            AssetEditorEditorType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 2)))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.icon != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.path != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.fileExtension != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.isColoredIcon ? 1 : 0);
      buf.writeByte(this.editorType.getValue());
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int iconOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int pathOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int fileExtensionOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.icon != null) {
         buf.setIntLE(iconOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.icon, 4096000);
      } else {
         buf.setIntLE(iconOffsetSlot, -1);
      }

      if (this.path != null) {
         buf.setIntLE(pathOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.path, 4096000);
      } else {
         buf.setIntLE(pathOffsetSlot, -1);
      }

      if (this.fileExtension != null) {
         buf.setIntLE(fileExtensionOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.fileExtension, 4096000);
      } else {
         buf.setIntLE(fileExtensionOffsetSlot, -1);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.icon != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.path != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.fileExtension != null) {
         nullBits = (byte)(nullBits | 8);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.isColoredIcon);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 2), (byte)this.editorType.getValue());
      int varOffset = offset + 19;
      if (this.id != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 3), varOffset - offset - 19);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.id, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 3), -1);
      }

      if (this.icon != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 7), varOffset - offset - 19);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.icon, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 7), -1);
      }

      if (this.path != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 11), varOffset - offset - 19);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.path, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 11), -1);
      }

      if (this.fileExtension != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 15), varOffset - offset - 19);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.fileExtension, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 15), -1);
      }

      return varOffset - offset;
   }

   public int computeSize() {
      int size = 19;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.icon != null) {
         size += PacketIO.stringSize(this.icon);
      }

      if (this.path != null) {
         size += PacketIO.stringSize(this.path);
      }

      if (this.fileExtension != null) {
         size += PacketIO.stringSize(this.fileExtension);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 19) {
         return ValidationResult.error("Buffer too small: expected at least 19 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 2) & 255;
         if (v >= 7) {
            return ValidationResult.error("Invalid AssetEditorEditorType value for EditorType");
         } else {
            if ((nullBits & 1) != 0) {
               v = buffer.getIntLE(offset + 3);
               if (v < 0 || v > buffer.writerIndex() - offset - 19) {
                  return ValidationResult.error("Invalid offset for Id");
               }

               int pos = offset + 19 + v;
               int idLen = VarInt.peek(buffer, pos);
               if (idLen < 0) {
                  return ValidationResult.error("Invalid string length for Id");
               }

               if (idLen > 4096000) {
                  return ValidationResult.error("Id exceeds max length 4096000");
               }

               pos += VarInt.size(idLen);
               pos += idLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Id");
               }
            }

            if ((nullBits & 2) != 0) {
               v = buffer.getIntLE(offset + 7);
               if (v < 0 || v > buffer.writerIndex() - offset - 19) {
                  return ValidationResult.error("Invalid offset for Icon");
               }

               int posx = offset + 19 + v;
               int iconLen = VarInt.peek(buffer, posx);
               if (iconLen < 0) {
                  return ValidationResult.error("Invalid string length for Icon");
               }

               if (iconLen > 4096000) {
                  return ValidationResult.error("Icon exceeds max length 4096000");
               }

               posx += VarInt.size(iconLen);
               posx += iconLen;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Icon");
               }
            }

            if ((nullBits & 4) != 0) {
               v = buffer.getIntLE(offset + 11);
               if (v < 0 || v > buffer.writerIndex() - offset - 19) {
                  return ValidationResult.error("Invalid offset for Path");
               }

               int posxx = offset + 19 + v;
               int pathLen = VarInt.peek(buffer, posxx);
               if (pathLen < 0) {
                  return ValidationResult.error("Invalid string length for Path");
               }

               if (pathLen > 4096000) {
                  return ValidationResult.error("Path exceeds max length 4096000");
               }

               posxx += VarInt.size(pathLen);
               posxx += pathLen;
               if (posxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading Path");
               }
            }

            if ((nullBits & 8) != 0) {
               v = buffer.getIntLE(offset + 15);
               if (v < 0 || v > buffer.writerIndex() - offset - 19) {
                  return ValidationResult.error("Invalid offset for FileExtension");
               }

               int posxxx = offset + 19 + v;
               int fileExtensionLen = VarInt.peek(buffer, posxxx);
               if (fileExtensionLen < 0) {
                  return ValidationResult.error("Invalid string length for FileExtension");
               }

               if (fileExtensionLen > 4096000) {
                  return ValidationResult.error("FileExtension exceeds max length 4096000");
               }

               posxxx += VarInt.size(fileExtensionLen);
               posxxx += fileExtensionLen;
               if (posxxx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading FileExtension");
               }
            }

            return ValidationResult.OK;
         }
      }
   }

   public AssetEditorAssetType clone() {
      AssetEditorAssetType copy = new AssetEditorAssetType();
      copy.id = this.id;
      copy.icon = this.icon;
      copy.isColoredIcon = this.isColoredIcon;
      copy.path = this.path;
      copy.fileExtension = this.fileExtension;
      copy.editorType = this.editorType;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorAssetType other)
            ? false
            : Objects.equals(this.id, other.id)
               && Objects.equals(this.icon, other.icon)
               && this.isColoredIcon == other.isColoredIcon
               && Objects.equals(this.path, other.path)
               && Objects.equals(this.fileExtension, other.fileExtension)
               && Objects.equals(this.editorType, other.editorType);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.icon, this.isColoredIcon, this.path, this.fileExtension, this.editorType);
   }
}
