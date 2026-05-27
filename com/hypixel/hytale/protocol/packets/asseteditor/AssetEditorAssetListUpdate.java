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
      if (buf.readableBytes() - offset < 13) {
         throw ProtocolException.bufferTooSmall("AssetEditorAssetListUpdate", 13, buf.readableBytes() - offset);
      } else {
         AssetEditorAssetListUpdate obj = new AssetEditorAssetListUpdate();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("Pack", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 13 + varPosBase0;
            int packLen = VarInt.peek(buf, varPos0);
            if (packLen < 0) {
               throw ProtocolException.invalidVarInt("Pack");
            }

            int packVarIntLen = VarInt.size(packLen);
            if (packLen > 4096000) {
               throw ProtocolException.stringTooLong("Pack", packLen, 4096000);
            }

            if (varPos0 + packVarIntLen + packLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Pack", varPos0 + packVarIntLen + packLen, buf.readableBytes());
            }

            obj.pack = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("Additions", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 13 + varPosBase1;
            int additionsCount = VarInt.peek(buf, varPos1);
            if (additionsCount < 0) {
               throw ProtocolException.invalidVarInt("Additions");
            }

            int varIntLen = VarInt.size(additionsCount);
            if (additionsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Additions", additionsCount, 4096000);
            }

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
            int varPosBase2 = buf.getIntLE(offset + 9);
            if (varPosBase2 < 0 || varPosBase2 > buf.writerIndex() - offset - 13) {
               throw ProtocolException.invalidOffset("Deletions", varPosBase2, buf.readableBytes());
            }

            int varPos2 = offset + 13 + varPosBase2;
            int deletionsCount = VarInt.peek(buf, varPos2);
            if (deletionsCount < 0) {
               throw ProtocolException.invalidVarInt("Deletions");
            }

            int varIntLenx = VarInt.size(deletionsCount);
            if (deletionsCount > 4096000) {
               throw ProtocolException.arrayTooLong("Deletions", deletionsCount, 4096000);
            }

            if (varPos2 + varIntLenx + deletionsCount * 2L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("Deletions", varPos2 + varIntLenx + deletionsCount * 2, buf.readableBytes());
            }

            obj.deletions = new AssetEditorFileEntry[deletionsCount];
            int elemPos = varPos2 + varIntLenx;

            for (int i = 0; i < deletionsCount; i++) {
               obj.deletions[i] = AssetEditorFileEntry.deserialize(buf, elemPos);
               elemPos += AssetEditorFileEntry.computeBytesConsumed(buf, elemPos);
            }
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 13;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("Pack", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 13 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("Additions", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 13 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos1 += AssetEditorFileEntry.computeBytesConsumed(buf, pos1);
         }

         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      if ((nullBits & 4) != 0) {
         int fieldOffset2 = buf.getIntLE(offset + 9);
         if (fieldOffset2 < 0 || fieldOffset2 > buf.writerIndex() - offset - 13) {
            throw ProtocolException.invalidOffset("Deletions", fieldOffset2, maxEnd);
         }

         int pos2 = offset + 13 + fieldOffset2;
         int arrLen = VarInt.peek(buf, pos2);
         pos2 += VarInt.size(arrLen);

         for (int i = 0; i < arrLen; i++) {
            pos2 += AssetEditorFileEntry.computeBytesConsumed(buf, pos2);
         }

         if (pos2 - offset > maxEnd) {
            maxEnd = pos2 - offset;
         }
      }

      return maxEnd;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 13L;
   }

   @Nullable
   public static String getPack(MemorySegment mem) {
      return getPack(mem, 0);
   }

   @Nullable
   public static String getPack(MemorySegment mem, int offset) {
      return hasPack(mem, offset) ? PacketIO.readVarString("Pack", mem, offset + getValidatedOffset(mem, offset, 1, 13, "Pack"), 4096000, PacketIO.UTF8) : null;
   }

   @Nullable
   public static AssetEditorFileEntry[] getAdditions(MemorySegment mem) {
      return getAdditions(mem, 0);
   }

   @Nullable
   public static AssetEditorFileEntry[] getAdditions(MemorySegment mem, int offset) {
      if (!hasAdditions(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 5, 13, "Additions");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Additions", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Additions", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Additions", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               AssetEditorFileEntry[] data = new AssetEditorFileEntry[len];

               for (int i = 0; i < len; i++) {
                  data[i] = AssetEditorFileEntry.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   @Nullable
   public static AssetEditorFileEntry[] getDeletions(MemorySegment mem) {
      return getDeletions(mem, 0);
   }

   @Nullable
   public static AssetEditorFileEntry[] getDeletions(MemorySegment mem, int offset) {
      if (!hasDeletions(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 9, 13, "Deletions");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Deletions", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("Deletions", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Deletions", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               AssetEditorFileEntry[] data = new AssetEditorFileEntry[len];

               for (int i = 0; i < len; i++) {
                  data[i] = AssetEditorFileEntry.toObject(mem, off);
                  off += data[i].computeSize();
               }

               return data;
            }
         }
      }
   }

   public static boolean hasPack(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasAdditions(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasDeletions(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static AssetEditorAssetListUpdate toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static AssetEditorAssetListUpdate toObject(MemorySegment mem, int offset) {
      if (offset + 13 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("AssetEditorAssetListUpdate", offset + 13, (int)mem.byteSize());
      } else {
         AssetEditorFileEntry[] additions = null;
         if (hasAdditions(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 5, 13, "Additions");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Additions", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("Additions", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Additions", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            additions = new AssetEditorFileEntry[len];

            for (int i = 0; i < len; i++) {
               additions[i] = AssetEditorFileEntry.toObject(mem, off);
               off += additions[i].computeSize();
            }
         }

         AssetEditorFileEntry[] deletions = null;
         if (hasDeletions(mem, offset)) {
            int offx = offset + getValidatedOffset(mem, offset, 9, 13, "Deletions");
            long packedx = VarInt.getWithLength(mem, offx);
            int lenx = (int)packedx;
            if (lenx < 0) {
               throw ProtocolException.negativeLength("Deletions", lenx);
            }

            if (lenx > 4096000) {
               throw ProtocolException.arrayTooLong("Deletions", lenx, 4096000);
            }

            int lenOffset = (int)(packedx >>> 32);
            if (offx + lenOffset + lenx > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Deletions", offx + lenOffset + lenx, (int)mem.byteSize());
            }

            offx += lenOffset;
            deletions = new AssetEditorFileEntry[lenx];

            for (int i = 0; i < lenx; i++) {
               deletions[i] = AssetEditorFileEntry.toObject(mem, offx);
               offx += deletions[i].computeSize();
            }
         }

         return new AssetEditorAssetListUpdate(
            hasPack(mem, offset) ? PacketIO.readVarString("Pack", mem, offset + getValidatedOffset(mem, offset, 1, 13, "Pack"), 4096000, PacketIO.UTF8) : null,
            additions,
            deletions
         );
      }
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
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

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 13;
      if (this.pack != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 13);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.pack, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.additions != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 13);
         if (this.additions.length > 4096000) {
            throw ProtocolException.arrayTooLong("Additions", this.additions.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.additions.length);
         int additionsValueOffset = 0;

         for (int i = 0; i < this.additions.length; i++) {
            additionsValueOffset += this.additions[i].serialize(mem, varOffset + additionsValueOffset);
         }

         varOffset += additionsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      if (this.deletions != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), varOffset - offset - 13);
         if (this.deletions.length > 4096000) {
            throw ProtocolException.arrayTooLong("Deletions", this.deletions.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.deletions.length);
         int deletionsValueOffset = 0;

         for (int i = 0; i < this.deletions.length; i++) {
            deletionsValueOffset += this.deletions[i].serialize(mem, varOffset + deletionsValueOffset);
         }

         varOffset += deletionsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 9), -1);
      }

      return varOffset - offset;
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
            if (packOffset < 0 || packOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for Pack");
            }

            int pos = offset + 13 + packOffset;
            int packLen = VarInt.peek(buffer, pos);
            if (packLen < 0) {
               return ValidationResult.error("Invalid string length for Pack");
            }

            if (packLen > 4096000) {
               return ValidationResult.error("Pack exceeds max length 4096000");
            }

            pos += VarInt.size(packLen);
            pos += packLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Pack");
            }
         }

         if ((nullBits & 2) != 0) {
            int additionsOffset = buffer.getIntLE(offset + 5);
            if (additionsOffset < 0 || additionsOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for Additions");
            }

            int posx = offset + 13 + additionsOffset;
            int additionsCount = VarInt.peek(buffer, posx);
            if (additionsCount < 0) {
               return ValidationResult.error("Invalid array count for Additions");
            }

            if (additionsCount > 4096000) {
               return ValidationResult.error("Additions exceeds max length 4096000");
            }

            posx += VarInt.size(additionsCount);

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
            if (deletionsOffset < 0 || deletionsOffset > buffer.writerIndex() - offset - 13) {
               return ValidationResult.error("Invalid offset for Deletions");
            }

            int posxx = offset + 13 + deletionsOffset;
            int deletionsCount = VarInt.peek(buffer, posxx);
            if (deletionsCount < 0) {
               return ValidationResult.error("Invalid array count for Deletions");
            }

            if (deletionsCount > 4096000) {
               return ValidationResult.error("Deletions exceeds max length 4096000");
            }

            posxx += VarInt.size(deletionsCount);

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
