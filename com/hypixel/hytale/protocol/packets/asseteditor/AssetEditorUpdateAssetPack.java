package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorUpdateAssetPack implements Packet, ToServerPacket, ToClientPacket {
   public static final int PACKET_ID = 315;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String id;
   @Nullable
   public AssetPackManifest manifest;

   @Override
   public int getId() {
      return 315;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorUpdateAssetPack() {
   }

   public AssetEditorUpdateAssetPack(@Nullable String id, @Nullable AssetPackManifest manifest) {
      this.id = id;
      this.manifest = manifest;
   }

   public AssetEditorUpdateAssetPack(@Nonnull AssetEditorUpdateAssetPack other) {
      this.id = other.id;
      this.manifest = other.manifest;
   }

   @Nonnull
   public static AssetEditorUpdateAssetPack deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetEditorUpdateAssetPack obj = new AssetEditorUpdateAssetPack();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 9 + buf.getIntLE(offset + 1);
         int idLen = VarInt.peek(buf, varPos0);
         if (idLen < 0) {
            throw ProtocolException.negativeLength("Id", idLen);
         }

         if (idLen > 4096000) {
            throw ProtocolException.stringTooLong("Id", idLen, 4096000);
         }

         obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 9 + buf.getIntLE(offset + 5);
         obj.manifest = AssetPackManifest.deserialize(buf, varPos1);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 9 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 9 + fieldOffset1;
         pos1 += AssetPackManifest.computeBytesConsumed(buf, pos1);
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
      if (this.id != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.manifest != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int manifestOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.manifest != null) {
         buf.setIntLE(manifestOffsetSlot, buf.writerIndex() - varBlockStart);
         this.manifest.serialize(buf);
      } else {
         buf.setIntLE(manifestOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 9;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.manifest != null) {
         size += this.manifest.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int idOffset = buffer.getIntLE(offset + 1);
            if (idOffset < 0) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 9 + idOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Id");
            }

            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            }

            if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += idLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Id");
            }
         }

         if ((nullBits & 2) != 0) {
            int manifestOffset = buffer.getIntLE(offset + 5);
            if (manifestOffset < 0) {
               return ValidationResult.error("Invalid offset for Manifest");
            }

            int posx = offset + 9 + manifestOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Manifest");
            }

            ValidationResult manifestResult = AssetPackManifest.validateStructure(buffer, posx);
            if (!manifestResult.isValid()) {
               return ValidationResult.error("Invalid Manifest: " + manifestResult.error());
            }

            posx += AssetPackManifest.computeBytesConsumed(buffer, posx);
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorUpdateAssetPack clone() {
      AssetEditorUpdateAssetPack copy = new AssetEditorUpdateAssetPack();
      copy.id = this.id;
      copy.manifest = this.manifest != null ? this.manifest.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorUpdateAssetPack other) ? false : Objects.equals(this.id, other.id) && Objects.equals(this.manifest, other.manifest);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.manifest);
   }
}
