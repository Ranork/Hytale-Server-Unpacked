package com.hypixel.hytale.protocol.packets.asseteditor;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
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
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("AssetEditorRequestDatasetReply", 9, buf.readableBytes() - offset);
      } else {
         AssetEditorRequestDatasetReply obj = new AssetEditorRequestDatasetReply();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("Name", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 9 + varPosBase0;
            int nameLen = VarInt.peek(buf, varPos0);
            if (nameLen < 0) {
               throw ProtocolException.invalidVarInt("Name");
            }

            int nameVarIntLen = VarInt.size(nameLen);
            if (nameLen > 4096000) {
               throw ProtocolException.stringTooLong("Name", nameLen, 4096000);
            }

            if (varPos0 + nameVarIntLen + nameLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Name", varPos0 + nameVarIntLen + nameLen, buf.readableBytes());
            }

            obj.name = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("Ids", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 9 + varPosBase1;
            int idsCount = VarInt.peek(buf, varPos1);
            if (idsCount < 0) {
               throw ProtocolException.invalidVarInt("Ids");
            }

            int varIntLen = VarInt.size(idsCount);
            if (idsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Ids", idsCount, 4096000);
            }

            if (varPos1 + varIntLen + idsCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Ids", varPos1 + varIntLen + idsCount * 1, buf.readableBytes());
            }

            obj.ids = new String[idsCount];
            int elemPos = varPos1 + varIntLen;

            for (int i = 0; i < idsCount; i++) {
               int strLen = VarInt.peek(buf, elemPos);
               if (strLen < 0) {
                  throw ProtocolException.invalidVarInt("ids[" + i + "]");
               }

               int strVarLen = VarInt.size(strLen);
               if (strLen > 4096000) {
                  throw ProtocolException.stringTooLong("ids[" + i + "]", strLen, 4096000);
               }

               if (elemPos + strVarLen + strLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("ids[" + i + "]", elemPos + strVarLen + strLen, buf.readableBytes());
               }

               obj.ids[i] = PacketIO.readVarString(buf, elemPos);
               elemPos += strVarLen + strLen;
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("Name", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 9 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("Ids", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 9 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            int sl = VarInt.peek(buf, pos1);
            pos1 += VarInt.size(sl) + sl;
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   @Nullable
   public static String getName(MemorySegment mem) {
      return getName(mem, 0);
   }

   @Nullable
   public static String getName(MemorySegment mem, int offset) {
      return hasName(mem, offset) ? PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 1, 9, "Name"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static String[] getIds(MemorySegment mem) {
      return getIds(mem, 0);
   }

   @Nullable
   public static String[] getIds(MemorySegment mem, int offset) {
      if (!hasIds(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 5, 9, "Ids");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Ids", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Ids", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Ids", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("Ids", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   public static boolean hasName(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasIds(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static AssetEditorRequestDatasetReply toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorRequestDatasetReply toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorRequestDatasetReply", offset + 9, (int)mem.byteSize());
      } else {
         String[] ids = null;
         if (hasIds(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 5, 9, "Ids");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Ids", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Ids", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Ids", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            ids = new String[len];

            for (int i = 0; i < len; i++) {
               long sp = VarInt.getWithLength(mem, off);
               int n = (int)sp + (int)(sp >>> 32);
               ids[i] = PacketIO.readVarString("Ids", mem, off, 16384000, PacketIO.UTF8);
               off += n;
            }
         }

         return new AssetEditorRequestDatasetReply(
            hasName(mem, offset) ? PacketIO.readVarString("Name", mem, offset + getValidatedOffset(mem, offset, 1, 9, "Name"), 4096000, PacketIO.UTF8) : null,
            ids
         );
      }
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.name != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.ids != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 9;
      if (this.name != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 9);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.name, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.ids != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 9);
         if (this.ids.length > 4096000) {
            throw ProtocolException.arrayTooLong("Ids", this.ids.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.ids.length);
         int idsValueOffset = 0;

         for (int i = 0; i < this.ids.length; i++) {
            idsValueOffset += PacketIO.writeVarString(mem, varOffset + idsValueOffset, this.ids[i], 16384000);
         }

         varOffset += idsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      return varOffset - offset;
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
            if (nameOffset < 0 || nameOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for Name");
            }

            int pos = offset + 9 + nameOffset;
            int nameLen = VarInt.peek(buffer, pos);
            if (nameLen < 0) {
               return ValidationResult.error("Invalid string length for Name");
            }

            if (nameLen > 4096000) {
               return ValidationResult.error("Name exceeds max length 4096000");
            }

            pos += VarInt.size(nameLen);
            pos += nameLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Name");
            }
         }

         if ((nullBits & 2) != 0) {
            int idsOffset = buffer.getIntLE(offset + 5);
            if (idsOffset < 0 || idsOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for Ids");
            }

            int posx = offset + 9 + idsOffset;
            int idsCount = VarInt.peek(buffer, posx);
            if (idsCount < 0) {
               return ValidationResult.error("Invalid array count for Ids");
            }

            if (idsCount > 4096000) {
               return ValidationResult.error("Ids exceeds max length 4096000");
            }

            posx += VarInt.size(idsCount);

            for (int i = 0; i < idsCount; i++) {
               int strLen = VarInt.peek(buffer, posx);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in Ids");
               }

               posx += VarInt.size(strLen);
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
