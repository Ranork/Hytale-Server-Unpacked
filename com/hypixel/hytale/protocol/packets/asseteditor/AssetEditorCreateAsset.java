package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorCreateAsset implements Packet, ToServerPacket {
   public static final int PACKET_ID = 327;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 10;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 22;
   public static final int MAX_SIZE = 53248051;
   public int token;
   @Nullable
   public AssetPath path;
   @Nullable
   public byte[] data;
   @Nullable
   public AssetEditorRebuildCaches rebuildCaches;
   @Nullable
   public String buttonId;

   @Override
   public int getId() {
      return 327;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorCreateAsset() {
   }

   public AssetEditorCreateAsset(
      int token, @Nullable AssetPath path, @Nullable byte[] data, @Nullable AssetEditorRebuildCaches rebuildCaches, @Nullable String buttonId
   ) {
      this.token = token;
      this.path = path;
      this.data = data;
      this.rebuildCaches = rebuildCaches;
      this.buttonId = buttonId;
   }

   public AssetEditorCreateAsset(@Nonnull AssetEditorCreateAsset other) {
      this.token = other.token;
      this.path = other.path;
      this.data = other.data;
      this.rebuildCaches = other.rebuildCaches;
      this.buttonId = other.buttonId;
   }

   @Nonnull
   public static AssetEditorCreateAsset deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 22) {
         throw ProtocolException.bufferTooSmall("AssetEditorCreateAsset", 22, buf.readableBytes() - offset);
      } else {
         AssetEditorCreateAsset obj = new AssetEditorCreateAsset();
         byte nullBits = buf.getByte(offset);
         obj.token = buf.getIntLE(offset + 1);
         if ((nullBits & 1) != 0) {
            obj.rebuildCaches = AssetEditorRebuildCaches.deserialize(buf, offset + 5);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 10);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 22) {
               throw ProtocolException.invalidOffset("Path", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 22 + varPosBase0;
            obj.path = AssetPath.deserialize(buf, varPos0);
         }

         if ((nullBits & 4) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 14);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 22) {
               throw ProtocolException.invalidOffset("Data", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 22 + varPosBase1;
            int dataCount = VarInt.peek(buf, varPos1);
            if (dataCount < 0) {
               throw ProtocolException.invalidVarInt("Data");
            }

            int varIntLen = VarInt.size(dataCount);
            if (dataCount > 4096000) {
               throw ProtocolException.arrayTooLong("Data", dataCount, 4096000);
            }

            if (varPos1 + varIntLen + dataCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Data", varPos1 + varIntLen + dataCount * 1, buf.readableBytes());
            }

            obj.data = new byte[dataCount];

            for (int i = 0; i < dataCount; i++) {
               obj.data[i] = buf.getByte(varPos1 + varIntLen + i * 1);
            }
         }

         if ((nullBits & 8) != 0) {
            int varPosBase2 = buf.getIntLE(offset + 18);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 22) {
               throw ProtocolException.invalidOffset("ButtonId", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 22 + varPosBase2;
            int buttonIdLen = VarInt.peek(buf, varPos2);
            if (buttonIdLen < 0) {
               throw ProtocolException.invalidVarInt("ButtonId");
            }

            int buttonIdVarIntLen = VarInt.size(buttonIdLen);
            if (buttonIdLen > 4096000) {
               throw ProtocolException.stringTooLong("ButtonId", buttonIdLen, 4096000);
            }

            if (varPos2 + buttonIdVarIntLen + buttonIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("ButtonId", varPos2 + buttonIdVarIntLen + buttonIdLen, buf.readableBytes());
            }

            obj.buttonId = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 22;
      if ((nullBits & 2) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 10);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 22) {
            throw ProtocolException.invalidOffset("Path", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 22 + fieldOffset0;
         pos0 += AssetPath.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 14);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 22) {
            throw ProtocolException.invalidOffset("Data", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 22 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen) + arrLen * 1;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 18);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 22) {
            throw ProtocolException.invalidOffset("ButtonId", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 22 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(sl) + sl;
         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 22L;
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
      return hasPath(mem, offset) ? AssetPath.toObject(mem, offset + getValidatedOffset(mem, offset, 10, 22, "Path")) : null;
   }

   @Nullable
   public static byte[] getData(MemorySegment mem) {
      return getData(mem, 0);
   }

   @Nullable
   public static byte[] getData(MemorySegment mem, int offset) {
      if (!hasData(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 14, 22, "Data");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Data", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Data", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Data", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               byte[] data = new byte[len];
               MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, data, 0, len);
               return data;
            }
         }
      }
   }

   @Nullable
   public static AssetEditorRebuildCaches getRebuildCaches(MemorySegment mem) {
      return getRebuildCaches(mem, 0);
   }

   @Nullable
   public static AssetEditorRebuildCaches getRebuildCaches(MemorySegment mem, int offset) {
      return hasRebuildCaches(mem, offset) ? AssetEditorRebuildCaches.toObject(mem, offset + 5) : null;
   }

   @Nullable
   public static String getButtonId(MemorySegment mem) {
      return getButtonId(mem, 0);
   }

   @Nullable
   public static String getButtonId(MemorySegment mem, int offset) {
      return hasButtonId(mem, offset)
         ? PacketIO.readVarString("ButtonId", mem, offset + getValidatedOffset(mem, offset, 18, 22, "ButtonId"), 4096000, PacketIO.UTF8)
         : null;
   }

   public static boolean hasRebuildCaches(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasPath(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasData(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasButtonId(MemorySegment mem, int offset) {
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

   public static AssetEditorCreateAsset toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorCreateAsset toObject(MemorySegment mem, int offset) {
      if (offset + 22 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorCreateAsset", offset + 22, (int)mem.byteSize());
      } else {
         byte[] data = null;
         if (hasData(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 14, 22, "Data");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Data", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Data", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Data", off + lenOffset + len * 1, (int)mem.byteSize());
            }

            off += lenOffset;
            data = new byte[len];
            MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, data, 0, len);
         }

         return new AssetEditorCreateAsset(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            hasPath(mem, offset) ? AssetPath.toObject(mem, offset + getValidatedOffset(mem, offset, 10, 22, "Path")) : null,
            data,
            hasRebuildCaches(mem, offset) ? AssetEditorRebuildCaches.toObject(mem, offset + 5) : null,
            hasButtonId(mem, offset)
               ? PacketIO.readVarString("ButtonId", mem, offset + getValidatedOffset(mem, offset, 18, 22, "ButtonId"), 4096000, PacketIO.UTF8)
               : null
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.rebuildCaches != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.path != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.data != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.buttonId != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.token);
      if (this.rebuildCaches != null) {
         this.rebuildCaches.serialize(buf);
      } else {
         buf.writeZero(5);
      }

      int pathOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int dataOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int buttonIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.path != null) {
         buf.setIntLE(pathOffsetSlot, buf.writerIndex() - varBlockStart);
         this.path.serialize(buf);
      } else {
         buf.setIntLE(pathOffsetSlot, -1);
      }

      if (this.data != null) {
         buf.setIntLE(dataOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.data.length > 4096000) {
            throw ProtocolException.arrayTooLong("Data", this.data.length, 4096000);
         }

         VarInt.write(buf, this.data.length);

         for (byte item : this.data) {
            buf.writeByte(item);
         }
      } else {
         buf.setIntLE(dataOffsetSlot, -1);
      }

      if (this.buttonId != null) {
         buf.setIntLE(buttonIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.buttonId, 4096000);
      } else {
         buf.setIntLE(buttonIdOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.rebuildCaches != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.path != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.data != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.buttonId != null) {
         nullBits = (byte)(nullBits | 8);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.token);
      if (this.rebuildCaches != null) {
         this.rebuildCaches.serialize(mem, offset + 5);
      } else {
         mem.asSlice(offset + 5, 5L).fill((byte)0);
      }

      int varOffset = offset + 22;
      if (this.path != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), varOffset - offset - 22);
         varOffset += this.path.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 10), -1);
      }

      if (this.data != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 14), varOffset - offset - 22);
         if (this.data.length > 4096000) {
            throw ProtocolException.arrayTooLong("Data", this.data.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.data.length);
         MemorySegment.copy(this.data, 0, mem, PacketIO.PROTO_BYTE, varOffset, this.data.length);
         varOffset += this.data.length * 1;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 14), -1);
      }

      if (this.buttonId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 18), varOffset - offset - 22);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.buttonId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 18), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 22;
      if (this.path != null) {
         size += this.path.computeSize();
      }

      if (this.data != null) {
         size += VarInt.size(this.data.length) + this.data.length * 1;
      }

      if (this.buttonId != null) {
         size += PacketIO.stringSize(this.buttonId);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 22) {
         return ValidationResult.error("Buffer too small: expected at least 22 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 2) != 0) {
            int pathOffset = buffer.getIntLE(offset + 10);
            if (pathOffset < 0 || pathOffset > buffer.writerIndex() - offset - 22) {
               return ValidationResult.error("Invalid offset for Path");
            }

            int pos = offset + 22 + pathOffset;
            ValidationResult pathResult = AssetPath.validateStructure(buffer, pos);
            if (!pathResult.isValid()) {
               return ValidationResult.error("Invalid Path: " + pathResult.error());
            }

            pos += AssetPath.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 4) != 0) {
            int dataOffset = buffer.getIntLE(offset + 14);
            if (dataOffset < 0 || dataOffset > buffer.writerIndex() - offset - 22) {
               return ValidationResult.error("Invalid offset for Data");
            }

            int pos = offset + 22 + dataOffset;
            int dataCount = VarInt.peek(buffer, pos);
            if (dataCount < 0) {
               return ValidationResult.error("Invalid array count for Data");
            }

            if (dataCount > 4096000) {
               return ValidationResult.error("Data exceeds max length 4096000");
            }

            pos += VarInt.size(dataCount);
            pos += dataCount * 1;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Data");
            }
         }

         if ((nullBits & 8) != 0) {
            int buttonIdOffset = buffer.getIntLE(offset + 18);
            if (buttonIdOffset < 0 || buttonIdOffset > buffer.writerIndex() - offset - 22) {
               return ValidationResult.error("Invalid offset for ButtonId");
            }

            int posx = offset + 22 + buttonIdOffset;
            int buttonIdLen = VarInt.peek(buffer, posx);
            if (buttonIdLen < 0) {
               return ValidationResult.error("Invalid string length for ButtonId");
            }

            if (buttonIdLen > 4096000) {
               return ValidationResult.error("ButtonId exceeds max length 4096000");
            }

            posx += VarInt.size(buttonIdLen);
            posx += buttonIdLen;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading ButtonId");
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorCreateAsset clone() {
      AssetEditorCreateAsset copy = new AssetEditorCreateAsset();
      copy.token = this.token;
      copy.path = this.path != null ? this.path.clone() : null;
      copy.data = this.data != null ? Arrays.copyOf(this.data, this.data.length) : null;
      copy.rebuildCaches = this.rebuildCaches != null ? this.rebuildCaches.clone() : null;
      copy.buttonId = this.buttonId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorCreateAsset other)
            ? false
            : this.token == other.token
               && Objects.equals(this.path, other.path)
               && Arrays.equals(this.data, other.data)
               && Objects.equals(this.rebuildCaches, other.rebuildCaches)
               && Objects.equals(this.buttonId, other.buttonId);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Integer.hashCode(this.token);
      result = 31 * result + Objects.hashCode(this.path);
      result = 31 * result + Arrays.hashCode(this.data);
      result = 31 * result + Objects.hashCode(this.rebuildCaches);
      return 31 * result + Objects.hashCode(this.buttonId);
   }
}
