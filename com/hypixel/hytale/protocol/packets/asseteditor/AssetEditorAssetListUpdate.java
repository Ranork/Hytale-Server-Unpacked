package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Arrays;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AssetEditorAssetListUpdate implements Packet, ToClientPacket {
   public static final int PACKET_ID = 320;
   public static final boolean IS_COMPRESSED = true;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 3;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String pack;
   @Nullable
   public AssetEditorFileEntry[] additions;
   @Nullable
   public AssetEditorFileEntry[] deletions;

   @Override
   public int getId() {
      return 320;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorAssetListUpdate() {
   }

   public AssetEditorAssetListUpdate(@Nullable String pack, @Nullable AssetEditorFileEntry[] additions, @Nullable AssetEditorFileEntry[] deletions) {
      this.pack = pack;
      this.additions = additions;
      this.deletions = deletions;
   }

   public AssetEditorAssetListUpdate(@Nonnull AssetEditorAssetListUpdate other) {
      this.pack = other.pack;
      this.additions = other.additions;
      this.deletions = other.deletions;
   }

   @Nonnull
   public static AssetEditorAssetListUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetEditorAssetListUpdate obj = new AssetEditorAssetListUpdate();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 13 + buf.getIntLE(offset + 1);
         int packLen = VarInt.peek(buf, varPos0);
         if (packLen < 0) {
            throw ProtocolException.negativeLength("Pack", packLen);
         }

         if (packLen > 4096000) {
            throw ProtocolException.stringTooLong("Pack", packLen, 4096000);
         }

         obj.pack = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 13 + buf.getIntLE(offset + 5);
         int additionsCount = VarInt.peek(buf, varPos1);
         if (additionsCount < 0) {
            throw ProtocolException.negativeLength("Additions", additionsCount);
         }

         if (additionsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Additions", additionsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + additionsCount * 2L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Additions", varPos1 + varIntLen + additionsCount * 2, buf.readableBytes());
         }

         obj.additions = new AssetEditorFileEntry[additionsCount];
         int elemPos = varPos1 + varIntLen;

         for (int i = 0; i < additionsCount; i++) {
            obj.additions[i] = AssetEditorFileEntry.deserialize(buf, elemPos);
            elemPos += AssetEditorFileEntry.computeBytesConsumed(buf, elemPos);
         }
      }

      if ((nullBits & 4) != 0) {
         int varPos2 = offset + 13 + buf.getIntLE(offset + 9);
         int deletionsCount = VarInt.peek(buf, varPos2);
         if (deletionsCount < 0) {
            throw ProtocolException.negativeLength("Deletions", deletionsCount);
         }

         if (deletionsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Deletions", deletionsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos2);
         if (varPos2 + varIntLen + deletionsCount * 2L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Deletions", varPos2 + varIntLen + deletionsCount * 2, buf.readableBytes());
         }

         obj.deletions = new AssetEditorFileEntry[deletionsCount];
         int elemPos = varPos2 + varIntLen;

         for (int i = 0; i < deletionsCount; i++) {
            obj.deletions[i] = AssetEditorFileEntry.deserialize(buf, elemPos);
            elemPos += AssetEditorFileEntry.computeBytesConsumed(buf, elemPos);
         }
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         int pos0 = offset + 13 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 13 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < arrLen; i++) {
            pos1 += AssetEditorFileEntry.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         int pos2 = offset + 13 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.length(buf, pos2);

         for (int i = 0; i < arrLen; i++) {
            pos2 += AssetEditorFileEntry.computeBytesConsumed(buf, pos2);
         }

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
      if (this.pack != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.additions != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.deletions != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      int packOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int additionsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int deletionsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.pack != null) {
         buf.setIntLE(packOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.pack, 4096000);
      } else {
         buf.setIntLE(packOffsetSlot, -1);
      }

      if (this.additions != null) {
         buf.setIntLE(additionsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.additions.length > 4096000) {
            throw ProtocolException.arrayTooLong("Additions", this.additions.length, 4096000);
         }

         VarInt.write(buf, this.additions.length);

         for (AssetEditorFileEntry item : this.additions) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(additionsOffsetSlot, -1);
      }

      if (this.deletions != null) {
         buf.setIntLE(deletionsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.deletions.length > 4096000) {
            throw ProtocolException.arrayTooLong("Deletions", this.deletions.length, 4096000);
         }

         VarInt.write(buf, this.deletions.length);

         for (AssetEditorFileEntry item : this.deletions) {
            item.serialize(buf);
         }
      } else {
         buf.setIntLE(deletionsOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 13;
      if (this.pack != null) {
         size += PacketIO.stringSize(this.pack);
      }

      if (this.additions != null) {
         int additionsSize = 0;

         for (AssetEditorFileEntry elem : this.additions) {
            additionsSize += elem.computeSize();
         }

         size += VarInt.size(this.additions.length) + additionsSize;
      }

      if (this.deletions != null) {
         int deletionsSize = 0;

         for (AssetEditorFileEntry elem : this.deletions) {
            deletionsSize += elem.computeSize();
         }

         size += VarInt.size(this.deletions.length) + deletionsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 13) {
         return ValidationResult.error("Buffer too small: expected at least 13 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int packOffset = buffer.getIntLE(offset + 1);
            if (packOffset < 0) {
               return ValidationResult.error("Invalid offset for Pack");
            }

            int pos = offset + 13 + packOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Pack");
            }

            int packLen = VarInt.peek(buffer, pos);
            if (packLen < 0) {
               return ValidationResult.error("Invalid string length for Pack");
            }

            if (packLen > 4096000) {
               return ValidationResult.error("Pack exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += packLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Pack");
            }
         }

         if ((nullBits & 2) != 0) {
            int additionsOffset = buffer.getIntLE(offset + 5);
            if (additionsOffset < 0) {
               return ValidationResult.error("Invalid offset for Additions");
            }

            int posx = offset + 13 + additionsOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Additions");
            }

            int additionsCount = VarInt.peek(buffer, posx);
            if (additionsCount < 0) {
               return ValidationResult.error("Invalid array count for Additions");
            }

            if (additionsCount > 4096000) {
               return ValidationResult.error("Additions exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int i = 0; i < additionsCount; i++) {
               ValidationResult structResult = AssetEditorFileEntry.validateStructure(buffer, posx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid AssetEditorFileEntry in Additions[" + i + "]: " + structResult.error());
               }

               posx += AssetEditorFileEntry.computeBytesConsumed(buffer, posx);
            }
         }

         if ((nullBits & 4) != 0) {
            int deletionsOffset = buffer.getIntLE(offset + 9);
            if (deletionsOffset < 0) {
               return ValidationResult.error("Invalid offset for Deletions");
            }

            int posxx = offset + 13 + deletionsOffset;
            if (posxx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Deletions");
            }

            int deletionsCount = VarInt.peek(buffer, posxx);
            if (deletionsCount < 0) {
               return ValidationResult.error("Invalid array count for Deletions");
            }

            if (deletionsCount > 4096000) {
               return ValidationResult.error("Deletions exceeds max length 4096000");
            }

            posxx += VarInt.length(buffer, posxx);

            for (int i = 0; i < deletionsCount; i++) {
               ValidationResult structResult = AssetEditorFileEntry.validateStructure(buffer, posxx);
               if (!structResult.isValid()) {
                  return ValidationResult.error("Invalid AssetEditorFileEntry in Deletions[" + i + "]: " + structResult.error());
               }

               posxx += AssetEditorFileEntry.computeBytesConsumed(buffer, posxx);
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorAssetListUpdate clone() {
      AssetEditorAssetListUpdate copy = new AssetEditorAssetListUpdate();
      copy.pack = this.pack;
      copy.additions = this.additions != null ? Arrays.stream(this.additions).map(e -> e.clone()).toArray(AssetEditorFileEntry[]::new) : null;
      copy.deletions = this.deletions != null ? Arrays.stream(this.deletions).map(e -> e.clone()).toArray(AssetEditorFileEntry[]::new) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorAssetListUpdate other)
            ? false
            : Objects.equals(this.pack, other.pack)
               && Arrays.equals((Object[])this.additions, (Object[])other.additions)
               && Arrays.equals((Object[])this.deletions, (Object[])other.deletions);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.pack);
      result = 31 * result + Arrays.hashCode((Object[])this.additions);
      return 31 * result + Arrays.hashCode((Object[])this.deletions);
   }
}
