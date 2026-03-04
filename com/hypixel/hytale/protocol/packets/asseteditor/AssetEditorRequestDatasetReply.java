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

public class AssetEditorRequestDatasetReply implements Packet, ToClientPacket {
   public static final int PACKET_ID = 334;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String name;
   @Nullable
   public String[] ids;

   @Override
   public int getId() {
      return 334;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public AssetEditorRequestDatasetReply() {
   }

   public AssetEditorRequestDatasetReply(@Nullable String name, @Nullable String[] ids) {
      this.name = name;
      this.ids = ids;
   }

   public AssetEditorRequestDatasetReply(@Nonnull AssetEditorRequestDatasetReply other) {
      this.name = other.name;
      this.ids = other.ids;
   }

   @Nonnull
   public static AssetEditorRequestDatasetReply deserialize(@Nonnull ByteBuf buf, int offset) {
      AssetEditorRequestDatasetReply obj = new AssetEditorRequestDatasetReply();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         int varPos0 = offset + 9 + buf.getIntLE(offset + 1);
         int nameLen = VarInt.peek(buf, varPos0);
         if (nameLen < 0) {
            throw ProtocolException.negativeLength("Name", nameLen);
         }

         if (nameLen > 4096000) {
            throw ProtocolException.stringTooLong("Name", nameLen, 4096000);
         }

         obj.name = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 2) != 0) {
         int varPos1 = offset + 9 + buf.getIntLE(offset + 5);
         int idsCount = VarInt.peek(buf, varPos1);
         if (idsCount < 0) {
            throw ProtocolException.negativeLength("Ids", idsCount);
         }

         if (idsCount > 4096000) {
            throw ProtocolException.arrayTooLong("Ids", idsCount, 4096000);
         }

         int varIntLen = VarInt.length(buf, varPos1);
         if (varPos1 + varIntLen + idsCount * 1L > buf.readableBytes()) {
            throw ProtocolException.bufferTooSmall("Ids", varPos1 + varIntLen + idsCount * 1, buf.readableBytes());
         }

         obj.ids = new String[idsCount];
         int elemPos = varPos1 + varIntLen;

         for (int i = 0; i < idsCount; i++) {
            int strLen = VarInt.peek(buf, elemPos);
            if (strLen < 0) {
               throw ProtocolException.negativeLength("ids[" + i + "]", strLen);
            }

            if (strLen > 4096000) {
               throw ProtocolException.stringTooLong("ids[" + i + "]", strLen, 4096000);
            }

            int strVarLen = VarInt.length(buf, elemPos);
            obj.ids[i] = PacketIO.readVarString(buf, elemPos);
            elemPos += strVarLen + strLen;
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
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         int pos1 = offset + 9 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.length(buf, pos1);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos1);
            pos1 += VarInt.length(buf, pos1) + sl;
         }

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
      if (this.name != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.ids != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int nameOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int idsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.name != null) {
         buf.setIntLE(nameOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.name, 4096000);
      } else {
         buf.setIntLE(nameOffsetSlot, -1);
      }

      if (this.ids != null) {
         buf.setIntLE(idsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.ids.length > 4096000) {
            throw ProtocolException.arrayTooLong("Ids", this.ids.length, 4096000);
         }

         VarInt.write(buf, this.ids.length);

         for (String item : this.ids) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(idsOffsetSlot, -1);
      }
   }

   @Override
   public int computeSize() {
      int size = 9;
      if (this.name != null) {
         size += PacketIO.stringSize(this.name);
      }

      if (this.ids != null) {
         int idsSize = 0;

         for (String elem : this.ids) {
            idsSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.ids.length) + idsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int nameOffset = buffer.getIntLE(offset + 1);
            if (nameOffset < 0) {
               return ValidationResult.error("Invalid offset for Name");
            }

            int pos = offset + 9 + nameOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Name");
            }

            int nameLen = VarInt.peek(buffer, pos);
            if (nameLen < 0) {
               return ValidationResult.error("Invalid string length for Name");
            }

            if (nameLen > 4096000) {
               return ValidationResult.error("Name exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += nameLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Name");
            }
         }

         if ((nullBits & 2) != 0) {
            int idsOffset = buffer.getIntLE(offset + 5);
            if (idsOffset < 0) {
               return ValidationResult.error("Invalid offset for Ids");
            }

            int posx = offset + 9 + idsOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Ids");
            }

            int idsCount = VarInt.peek(buffer, posx);
            if (idsCount < 0) {
               return ValidationResult.error("Invalid array count for Ids");
            }

            if (idsCount > 4096000) {
               return ValidationResult.error("Ids exceeds max length 4096000");
            }

            posx += VarInt.length(buffer, posx);

            for (int i = 0; i < idsCount; i++) {
               int strLen = VarInt.peek(buffer, posx);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in Ids");
               }

               posx += VarInt.length(buffer, posx);
               posx += strLen;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in Ids");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public AssetEditorRequestDatasetReply clone() {
      AssetEditorRequestDatasetReply copy = new AssetEditorRequestDatasetReply();
      copy.name = this.name;
      copy.ids = this.ids != null ? Arrays.copyOf(this.ids, this.ids.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AssetEditorRequestDatasetReply other)
            ? false
            : Objects.equals(this.name, other.name) && Arrays.equals((Object[])this.ids, (Object[])other.ids);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.name);
      return 31 * result + Arrays.hashCode((Object[])this.ids);
   }
}
