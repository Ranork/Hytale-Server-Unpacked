package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
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
      AssetEditorCreateAsset obj = new AssetEditorCreateAsset();
      byte nullBits = buf.getByte(offset);
      obj.token = buf.getIntLE(offset + 1);
      if ((nullBits & 1) != 0) {
         obj.rebuildCaches = AssetEditorRebuildCaches.deserialize(buf, offset + 5);
      }

      if ((nullBits & 2) != 0) {
         int varPos0 = offset + 22 + buf.getIntLE(offset + 10);
         obj.path = AssetPath.deserialize(buf, varPos0);
      }

      if ((nullBits & 4) != 0) {
         int varPos1 = offset + 22 + buf.getIntLE(offset + 14);
         int dataCount = VarInt.peek(buf, varPos1);
         if (dataCount < 0) {
            throw ProtocolException.negativeLength("Data", dataCount);
         }

         if (dataCount > 4096000) {
            throw ProtocolException.arrayTooLong("Data", dataCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + dataCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Data", varPos1 + varIntLen + dataCount * 1, buf.readableBytes());
         }

         obj.data = new byte[dataCount];

         for (int i = 0; i < dataCount; i++) {
            obj.data[i] = buf.getByte(varPos1 + varIntLen + i * 1);
         }
      }

      if ((nullBits & 8) != 0) {
         int varPos2 = offset + 22 + buf.getIntLE(offset + 18);
         int buttonIdLen = VarInt.peek(buf, varPos2);
         if (buttonIdLen < 0) {
            throw ProtocolException.negativeLength("ButtonId", buttonIdLen);
         }

         if (buttonIdLen > 4096000) {
            throw ProtocolException.stringTooLong("ButtonId", buttonIdLen, 4096000);
         }

         obj.buttonId = PacketIO.readVarString(buf, varPos2, PacketIO.UTF8);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 22;
      if ((nullBits & 2) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 10);
         int pos0 = offset + 22 + fieldOffset0;
         pos0 += AssetPath.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 14);
         int pos1 = offset + 22 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + arrLen * 1;
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 8) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 18);
         int pos2 = offset + 22 + fieldOffset2;
         int sl = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2) + sl;
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
            if (pathOffset < 0) {
               return ValidationResult.error("Invalid offset for Path");
            }

            int pos = offset + 22 + pathOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Path");
            }

            ValidationResult pathResult = AssetPath.validateStructure(buffer, pos);
            if (!pathResult.isValid()) {
               return ValidationResult.error("Invalid Path: " + pathResult.error());
            }

            pos += AssetPath.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 4) != 0) {
            int dataOffset = buffer.getIntLE(offset + 14);
            if (dataOffset < 0) {
               return ValidationResult.error("Invalid offset for Data");
            }

            int posx = offset + 22 + dataOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Data");
            }

            int dataCount = VarInt.peek(buffer, posx);
            if (dataCount < 0) {
               return ValidationResult.error("Invalid array count for Data");
            }

            if (dataCount > 4096000) {
               return ValidationResult.error("Data exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);
            posx += dataCount * 1;
            if (posx > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Data");
            }
         }

         if ((nullBits & 8) != 0) {
            int buttonIdOffset = buffer.getIntLE(offset + 18);
            if (buttonIdOffset < 0) {
               return ValidationResult.error("Invalid offset for ButtonId");
            }

            int posxx = offset + 22 + buttonIdOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for ButtonId");
            }

            int buttonIdLen = VarInt.peek(buffer, posxx);
            if (buttonIdLen < 0) {
               return ValidationResult.error("Invalid string length for ButtonId");
            }

            if (buttonIdLen > 4096000) {
               return ValidationResult.error("ButtonId exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);
            posxx += buttonIdLen;
            if (posxx > buffer.writerIndex()) {
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
