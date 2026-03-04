package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorAssetUpdated implements Packet, ToClientPacket {
   public static final int PACKET_ID = 326;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 36864033;
   @Nullable
   public AssetPath path;
   @Nullable
   public byte[] data;

   @Override
   public int getId() {
      return 326;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorAssetUpdated() {
   }

   public AssetEditorAssetUpdated(@Nullable AssetPath path, @Nullable byte[] data) {
      this.path = path;
      this.data = data;
   }

   public AssetEditorAssetUpdated(@Nonnull AssetEditorAssetUpdated other) {
      this.path = other.path;
      this.data = other.data;
   }

   @Nonnull
   public static AssetEditorAssetUpdated deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetEditorAssetUpdated obj = new AssetEditorAssetUpdated();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 9 + buf.getIntLE(offset + 1);
         obj.path = AssetPath.deserialize(buf, varPos0);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 9 + buf.getIntLE(offset + 5);
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

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 9 + fieldOffset0;
         pos0 += AssetPath.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 9 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1) + arrLen * 1;
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
      if (this.path != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.data != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int pathOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int dataOffsetSlot = buf.writerIndex();
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
   }

   @Override
   public int computeSize() {
      int size = 9;
      if (this.path != null) {
         size += this.path.computeSize();
      }

      if (this.data != null) {
         size += VarInt.size(this.data.length) + this.data.length * 1;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int pathOffset = buffer.getIntLE(offset + 1);
            if (pathOffset < 0) {
               return ValidationResult.error("Invalid offset for Path");
            }

            int pos = offset + 9 + pathOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Path");
            }

            ValidationResult pathResult = AssetPath.validateStructure(buffer, pos);
            if (!pathResult.isValid()) {
               return ValidationResult.error("Invalid Path: " + pathResult.error());
            }

            pos += AssetPath.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 2) != 0) {
            int dataOffset = buffer.getIntLE(offset + 5);
            if (dataOffset < 0) {
               return ValidationResult.error("Invalid offset for Data");
            }

            int posx = offset + 9 + dataOffset;
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

         return ValidationResult.OK;
      }
   }

   public AssetEditorAssetUpdated clone() {
      AssetEditorAssetUpdated copy = new AssetEditorAssetUpdated();
      copy.path = this.path != null ? this.path.clone() : null;
      copy.data = this.data != null ? Arrays.copyOf(this.data, this.data.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorAssetUpdated other) ? false : Objects.equals(this.path, other.path) && Arrays.equals(this.data, other.data);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.path);
      return 31 * result + Arrays.hashCode(this.data);
   }
}
