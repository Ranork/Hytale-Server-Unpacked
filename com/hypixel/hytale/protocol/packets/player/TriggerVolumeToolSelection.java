package com.hypixel.hytale.protocol.packets.player;

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

public class TriggerVolumeToolSelection implements Packet, ToClientPacket {
   public static final int PACKET_ID = 507;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 1677721600;
   @Nullable
   public String primaryVolumeId;
   @Nullable
   public String[] volumeIds;

   @Override
   public int getId() {
      return 507;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public TriggerVolumeToolSelection() {
   }

   public TriggerVolumeToolSelection(@Nullable String primaryVolumeId, @Nullable String[] volumeIds) {
      this.primaryVolumeId = primaryVolumeId;
      this.volumeIds = volumeIds;
   }

   public TriggerVolumeToolSelection(@Nonnull TriggerVolumeToolSelection other) {
      this.primaryVolumeId = other.primaryVolumeId;
      this.volumeIds = other.volumeIds;
   }

   @Nonnull
   public static TriggerVolumeToolSelection deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("TriggerVolumeToolSelection", 9, buf.readableBytes() - offset);
      } else {
         TriggerVolumeToolSelection obj = new TriggerVolumeToolSelection();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("PrimaryVolumeId", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 9 + varPosBase0;
            int primaryVolumeIdLen = VarInt.peek(buf, varPos0);
            if (primaryVolumeIdLen < 0) {
               throw ProtocolException.invalidVarInt("PrimaryVolumeId");
            }

            int primaryVolumeIdVarIntLen = VarInt.size(primaryVolumeIdLen);
            if (primaryVolumeIdLen > 4096000) {
               throw ProtocolException.stringTooLong("PrimaryVolumeId", primaryVolumeIdLen, 4096000);
            }

            if (varPos0 + primaryVolumeIdVarIntLen + primaryVolumeIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("PrimaryVolumeId", varPos0 + primaryVolumeIdVarIntLen + primaryVolumeIdLen, buf.readableBytes());
            }

            obj.primaryVolumeId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("VolumeIds", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 9 + varPosBase1;
            int volumeIdsCount = VarInt.peek(buf, varPos1);
            if (volumeIdsCount < 0) {
               throw ProtocolException.invalidVarInt("VolumeIds");
            }

            int varIntLen = VarInt.size(volumeIdsCount);
            if (volumeIdsCount > 4096000) {
               throw ProtocolException.arrayTooLong("VolumeIds", volumeIdsCount, 4096000);
            }

            if (varPos1 + varIntLen + volumeIdsCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("VolumeIds", varPos1 + varIntLen + volumeIdsCount * 1, buf.readableBytes());
            }

            obj.volumeIds = new String[volumeIdsCount];
            int elemPos = varPos1 + varIntLen;

            for (int i = 0; i < volumeIdsCount; i++) {
               int strLen = VarInt.peek(buf, elemPos);
               if (strLen < 0) {
                  throw ProtocolException.invalidVarInt("volumeIds[" + i + "]");
               }

               int strVarLen = VarInt.size(strLen);
               if (strLen > 4096000) {
                  throw ProtocolException.stringTooLong("volumeIds[" + i + "]", strLen, 4096000);
               }

               if (elemPos + strVarLen + strLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("volumeIds[" + i + "]", elemPos + strVarLen + strLen, buf.readableBytes());
               }

               obj.volumeIds[i] = PacketIO.readVarString(buf, elemPos);
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
            throw ProtocolException.invalidOffset("PrimaryVolumeId", fieldOffset0, maxEnd);
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
            throw ProtocolException.invalidOffset("VolumeIds", fieldOffset1, maxEnd);
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
   public static String getPrimaryVolumeId(MemorySegment mem) {
      return getPrimaryVolumeId(mem, 0);
   }

   @Nullable
   public static String getPrimaryVolumeId(MemorySegment mem, int offset) {
      return hasPrimaryVolumeId(mem, offset)
         ? PacketIO.readVarString("PrimaryVolumeId", mem, offset + getValidatedOffset(mem, offset, 1, 9, "PrimaryVolumeId"), 4096000, PacketIO.UTF8)
         : null;
   }

   @Nullable
   public static String[] getVolumeIds(MemorySegment mem) {
      return getVolumeIds(mem, 0);
   }

   @Nullable
   public static String[] getVolumeIds(MemorySegment mem, int offset) {
      if (!hasVolumeIds(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 5, 9, "VolumeIds");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("VolumeIds", len);
         } else if (len > 4096000) {
            throw ProtocolException.arrayTooLong("VolumeIds", len, 4096000);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("VolumeIds", off + lenOffset + len, (int)mem.byteSize());
            } else {
               off += lenOffset;
               String[] data = new String[len];

               for (int i = 0; i < len; i++) {
                  long sp = VarInt.getWithLength(mem, off);
                  int n = (int)sp + (int)(sp >>> 32);
                  data[i] = PacketIO.readVarString("VolumeIds", mem, off, 16384000, PacketIO.UTF8);
                  off += n;
               }

               return data;
            }
         }
      }
   }

   public static boolean hasPrimaryVolumeId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasVolumeIds(MemorySegment mem, int offset) {
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

   public static TriggerVolumeToolSelection toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static TriggerVolumeToolSelection toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("TriggerVolumeToolSelection", offset + 9, (int)mem.byteSize());
      } else {
         String[] volumeIds = null;
         if (hasVolumeIds(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 5, 9, "VolumeIds");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("VolumeIds", len);
            }

            if (len > 4096000) {
               throw ProtocolException.arrayTooLong("VolumeIds", len, 4096000);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("VolumeIds", off + lenOffset + len, (int)mem.byteSize());
            }

            off += lenOffset;
            volumeIds = new String[len];

            for (int i = 0; i < len; i++) {
               long sp = VarInt.getWithLength(mem, off);
               int n = (int)sp + (int)(sp >>> 32);
               volumeIds[i] = PacketIO.readVarString("VolumeIds", mem, off, 16384000, PacketIO.UTF8);
               off += n;
            }
         }

         return new TriggerVolumeToolSelection(
            hasPrimaryVolumeId(mem, offset)
               ? PacketIO.readVarString("PrimaryVolumeId", mem, offset + getValidatedOffset(mem, offset, 1, 9, "PrimaryVolumeId"), 4096000, PacketIO.UTF8)
               : null,
            volumeIds
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.primaryVolumeId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.volumeIds != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int primaryVolumeIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int volumeIdsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.primaryVolumeId != null) {
         buf.setIntLE(primaryVolumeIdOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.primaryVolumeId, 4096000);
      } else {
         buf.setIntLE(primaryVolumeIdOffsetSlot, -1);
      }

      if (this.volumeIds != null) {
         buf.setIntLE(volumeIdsOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.volumeIds.length > 4096000) {
            throw ProtocolException.arrayTooLong("VolumeIds", this.volumeIds.length, 4096000);
         }

         VarInt.write(buf, this.volumeIds.length);

         for (String item : this.volumeIds) {
            PacketIO.writeVarString(buf, item, 4096000);
         }
      } else {
         buf.setIntLE(volumeIdsOffsetSlot, -1);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.primaryVolumeId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.volumeIds != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 9;
      if (this.primaryVolumeId != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 9);
         varOffset += PacketIO.writeVarString(mem, varOffset, this.primaryVolumeId, 4096000);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.volumeIds != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 9);
         if (this.volumeIds.length > 4096000) {
            throw ProtocolException.arrayTooLong("VolumeIds", this.volumeIds.length, 4096000);
         }

         varOffset += VarInt.set(mem, varOffset, this.volumeIds.length);
         int volumeIdsValueOffset = 0;

         for (int i = 0; i < this.volumeIds.length; i++) {
            volumeIdsValueOffset += PacketIO.writeVarString(mem, varOffset + volumeIdsValueOffset, this.volumeIds[i], 16384000);
         }

         varOffset += volumeIdsValueOffset;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 9;
      if (this.primaryVolumeId != null) {
         size += PacketIO.stringSize(this.primaryVolumeId);
      }

      if (this.volumeIds != null) {
         int volumeIdsSize = 0;

         for (String elem : this.volumeIds) {
            volumeIdsSize += PacketIO.stringSize(elem);
         }

         size += VarInt.size(this.volumeIds.length) + volumeIdsSize;
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 9) {
         return ValidationResult.error("Buffer too small: expected at least 9 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 1) != 0) {
            int primaryVolumeIdOffset = buffer.getIntLE(offset + 1);
            if (primaryVolumeIdOffset < 0 || primaryVolumeIdOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for PrimaryVolumeId");
            }

            int pos = offset + 9 + primaryVolumeIdOffset;
            int primaryVolumeIdLen = VarInt.peek(buffer, pos);
            if (primaryVolumeIdLen < 0) {
               return ValidationResult.error("Invalid string length for PrimaryVolumeId");
            }

            if (primaryVolumeIdLen > 4096000) {
               return ValidationResult.error("PrimaryVolumeId exceeds max length 4096000");
            }

            pos += VarInt.size(primaryVolumeIdLen);
            pos += primaryVolumeIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading PrimaryVolumeId");
            }
         }

         if ((nullBits & 2) != 0) {
            int volumeIdsOffset = buffer.getIntLE(offset + 5);
            if (volumeIdsOffset < 0 || volumeIdsOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for VolumeIds");
            }

            int posx = offset + 9 + volumeIdsOffset;
            int volumeIdsCount = VarInt.peek(buffer, posx);
            if (volumeIdsCount < 0) {
               return ValidationResult.error("Invalid array count for VolumeIds");
            }

            if (volumeIdsCount > 4096000) {
               return ValidationResult.error("VolumeIds exceeds max length 4096000");
            }

            posx += VarInt.size(volumeIdsCount);

            for (int i = 0; i < volumeIdsCount; i++) {
               int strLen = VarInt.peek(buffer, posx);
               if (strLen < 0) {
                  return ValidationResult.error("Invalid string length in VolumeIds");
               }

               posx += VarInt.size(strLen);
               posx += strLen;
               if (posx > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading string in VolumeIds");
               }
            }
         }

         return ValidationResult.OK;
      }
   }

   public TriggerVolumeToolSelection clone() {
      TriggerVolumeToolSelection copy = new TriggerVolumeToolSelection();
      copy.primaryVolumeId = this.primaryVolumeId;
      copy.volumeIds = this.volumeIds != null ? Arrays.copyOf(this.volumeIds, this.volumeIds.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof TriggerVolumeToolSelection other)
            ? false
            : Objects.equals(this.primaryVolumeId, other.primaryVolumeId) && Arrays.equals((Object[])this.volumeIds, (Object[])other.volumeIds);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.primaryVolumeId);
      return 31 * result + Arrays.hashCode((Object[])this.volumeIds);
   }
}
