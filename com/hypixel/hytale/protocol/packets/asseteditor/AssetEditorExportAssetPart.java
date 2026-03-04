package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorExportAssetPart implements Packet, ToClientPacket {
   public static final int PACKET_ID = 344;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 4096006;
   @Nullable
   public byte[] part;

   @Override
   public int getId() {
      return 344;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorExportAssetPart() {
   }

   public AssetEditorExportAssetPart(@Nullable byte[] part) {
      this.part = part;
   }

   public AssetEditorExportAssetPart(@Nonnull AssetEditorExportAssetPart other) {
      this.part = other.part;
   }

   @Nonnull
   public static AssetEditorExportAssetPart deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetEditorExportAssetPart obj = new AssetEditorExportAssetPart();
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int partCount = VarInt.peek(buf, pos);
         if (partCount < 0) {
            throw ProtocolException.negativeLength("Part", partCount);
         }

         if (partCount > 4096000) {
            throw ProtocolException.arrayTooLong("Part", partCount, 4096000);
         }

         int partVarLen = VarInt.size(partCount);
         if (pos + partVarLen + partCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Part", pos + partVarLen + partCount * 1, buf.readableBytes());
         }

         pos += partVarLen;
         obj.part = new byte[partCount];

         for (int i = 0; i < partCount; i++) {
            obj.part[i] = buf.getByte(pos + i * 1);
         }

         pos += partCount * 1;
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int arrLen = VarInt.peek(buf, pos);
         pos += VarInt.length(buf, pos) + arrLen * 1;
      }

      return pos - offset;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.part != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.part != null) {
         if (this.part.length > 4096000) {
            throw ProtocolException.arrayTooLong("Part", this.part.length, 4096000);
         }

         VarInt.write(buf, this.part.length);

         for (byte item : this.part) {
            buf.writeByte(item);
         }
      }
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.part != null) {
         size += VarInt.size(this.part.length) + this.part.length * 1;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 1) {
         return ValidationResult.error("Buffer too small: expected at least 1 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int partCount = VarInt.peek(buffer, pos);
            if (partCount < 0) {
               return ValidationResult.error("Invalid array count for Part");
            }

            if (partCount > 4096000) {
               return ValidationResult.error("Part exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += partCount * 1;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Part");
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorExportAssetPart clone() {
      AssetEditorExportAssetPart copy = new AssetEditorExportAssetPart();
      copy.part = this.part != null ? Arrays.copyOf(this.part, this.part.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof AssetEditorExportAssetPart other ? Arrays.equals(this.part, other.part) : false;
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      return 31 * result + Arrays.hashCode(this.part);
   }
}
