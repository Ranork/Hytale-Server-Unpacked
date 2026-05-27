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

public class TriggerVolumeToolCreateResponse implements Packet, ToClientPacket {
   public static final int PACKET_ID = 485;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 1677721600;
   @Nonnull
   public String volumeId = "";
   @Nullable
   public String[] volumeIds;

   @Override
   public int getId() {
      return 485;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public TriggerVolumeToolCreateResponse() {
   }

   public TriggerVolumeToolCreateResponse(@Nonnull String volumeId, @Nullable String[] volumeIds) {
      this.volumeId = volumeId;
      this.volumeIds = volumeIds;
   }

   public TriggerVolumeToolCreateResponse(@Nonnull TriggerVolumeToolCreateResponse other) {
      this.volumeId = other.volumeId;
      this.volumeIds = other.volumeIds;
   }

   @Nonnull
   public static TriggerVolumeToolCreateResponse deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("TriggerVolumeToolCreateResponse", 9, buf.readableBytes() - offset);
      } else {
         TriggerVolumeToolCreateResponse obj = new TriggerVolumeToolCreateResponse();
         byte nullBits = buf.getByte(offset);
         int varPosBase0 = buf.getIntLE(offset + 1);
         if (varPosBase0 >= 0 && varPosBase0 <= buf.writerIndex() - offset - 9) {
            int varPos0 = offset + 9 + varPosBase0;
            int volumeIdLen = VarInt.peek(buf, varPos0);
            if (volumeIdLen < 0) {
               throw ProtocolException.invalidVarInt("VolumeId");
            } else {
               int volumeIdVarIntLen = VarInt.size(volumeIdLen);
               if (volumeIdLen > 4096000) {
                  throw ProtocolException.stringTooLong("VolumeId", volumeIdLen, 4096000);
               } else if (varPos0 + volumeIdVarIntLen + volumeIdLen > buf.readableBytes()) {
                  throw ProtocolException.bufferTooSmall("VolumeId", varPos0 + volumeIdVarIntLen + volumeIdLen, buf.readableBytes());
               } else {
                  obj.volumeId = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
                  if ((nullBits & 1) != 0) {
                     varPosBase0 = buf.getIntLE(offset + 5);
                     if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 9) {
                        throw ProtocolException.invalidOffset("VolumeIds", varPosBase0, buf.readableBytes());
                     }

                     varPos0 = offset + 9 + varPosBase0;
                     volumeIdLen = VarInt.peek(buf, varPos0);
                     if (volumeIdLen < 0) {
                        throw ProtocolException.invalidVarInt("VolumeIds");
                     }

                     volumeIdVarIntLen = VarInt.size(volumeIdLen);
                     if (volumeIdLen > 4096000) {
                        throw ProtocolException.arrayTooLong("VolumeIds", volumeIdLen, 4096000);
                     }

                     if (varPos0 + volumeIdVarIntLen + volumeIdLen * 1L > buf.readableBytes()) {
                        throw ProtocolException.bufferTooSmall("VolumeIds", varPos0 + volumeIdVarIntLen + volumeIdLen * 1, buf.readableBytes());
                     }

                     obj.volumeIds = new String[volumeIdLen];
                     int elemPos = varPos0 + volumeIdVarIntLen;

                     for (int i = 0; i < volumeIdLen; i++) {
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
         } else {
            throw ProtocolException.invalidOffset("VolumeId", varPosBase0, buf.readableBytes());
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      int fieldOffset0 = buf.getIntLE(offset + 1);
      if (fieldOffset0 >= 0 && fieldOffset0 <= buf.writerIndex() - offset - 9) {
         int pos0 = offset + 9 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.size(sl) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }

         if ((nullBits & 1) != 0) {
            fieldOffset0 = buf.getIntLE(offset + 5);
            if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("VolumeIds", fieldOffset0, maxEnd);
            }

            pos0 = offset + 9 + fieldOffset0;
            sl = VarInt.peek(buf, pos0);
            pos0 += VarInt.size(sl);

            for (int i = 0; i < sl; i++) {
               int slx = VarInt.peek(buf, pos0);
               pos0 += VarInt.size(slx) + slx;
            }

            if (pos0 - offset > maxEnd) {
               maxEnd = pos0 - offset;
            }
         }

         return maxEnd;
      } else {
         throw ProtocolException.invalidOffset("VolumeId", fieldOffset0, maxEnd);
      }
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 9L;
   }

   public static String getVolumeId(MemorySegment mem) {
      return getVolumeId(mem, 0);
   }

   public static String getVolumeId(MemorySegment mem, int offset) {
      return PacketIO.readVarString("VolumeId", mem, offset + getValidatedOffset(mem, offset, 1, 9, "VolumeId"), 4096000, PacketIO.UTF8);
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

   public static boolean hasVolumeIds(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   private static int getValidatedOffset(MemorySegment buffer, int base, int slotPosition, int varBlockStart, String fieldName) {
      int offset = buffer.get(PacketIO.PROTO_INT, (long)(base + slotPosition));
      if (offset >= 0 && offset <= buffer.byteSize() - base - varBlockStart) {
         return varBlockStart + offset;
      } else {
         throw ProtocolException.invalidOffset(fieldName, offset, (int)buffer.byteSize());
      }
   }

   public static TriggerVolumeToolCreateResponse toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static TriggerVolumeToolCreateResponse toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("TriggerVolumeToolCreateResponse", offset + 9, (int)mem.byteSize());
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

         return new TriggerVolumeToolCreateResponse(
            PacketIO.readVarString("VolumeId", mem, offset + getValidatedOffset(mem, offset, 1, 9, "VolumeId"), 4096000, PacketIO.UTF8), volumeIds
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.volumeIds != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      int volumeIdOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int volumeIdsOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      buf.setIntLE(volumeIdOffsetSlot, buf.writerIndex() - varBlockStart);
      PacketIO.writeVarString(buf, this.volumeId, 4096000);
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
      if (this.volumeIds != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 9;
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 9);
      varOffset += PacketIO.writeVarString(mem, varOffset, this.volumeId, 4096000);
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
      size += PacketIO.stringSize(this.volumeId);
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
         int volumeIdOffset = buffer.getIntLE(offset + 1);
         if (volumeIdOffset >= 0 && volumeIdOffset <= buffer.writerIndex() - offset - 9) {
            int pos = offset + 9 + volumeIdOffset;
            int volumeIdLen = VarInt.peek(buffer, pos);
            if (volumeIdLen < 0) {
               return ValidationResult.error("Invalid string length for VolumeId");
            } else if (volumeIdLen > 4096000) {
               return ValidationResult.error("VolumeId exceeds max length 4096000");
            } else {
               pos += VarInt.size(volumeIdLen);
               pos += volumeIdLen;
               if (pos > buffer.writerIndex()) {
                  return ValidationResult.error("Buffer overflow reading VolumeId");
               } else {
                  if ((nullBits & 1) != 0) {
                     volumeIdOffset = buffer.getIntLE(offset + 5);
                     if (volumeIdOffset < 0 || volumeIdOffset > buffer.writerIndex() - offset - 9) {
                        return ValidationResult.error("Invalid offset for VolumeIds");
                     }

                     pos = offset + 9 + volumeIdOffset;
                     volumeIdLen = VarInt.peek(buffer, pos);
                     if (volumeIdLen < 0) {
                        return ValidationResult.error("Invalid array count for VolumeIds");
                     }

                     if (volumeIdLen > 4096000) {
                        return ValidationResult.error("VolumeIds exceeds max length 4096000");
                     }

                     pos += VarInt.size(volumeIdLen);

                     for (int i = 0; i < volumeIdLen; i++) {
                        int strLen = VarInt.peek(buffer, pos);
                        if (strLen < 0) {
                           return ValidationResult.error("Invalid string length in VolumeIds");
                        }

                        pos += VarInt.size(strLen);
                        pos += strLen;
                        if (pos > buffer.writerIndex()) {
                           return ValidationResult.error("Buffer overflow reading string in VolumeIds");
                        }
                     }
                  }

                  return ValidationResult.OK;
               }
            }
         } else {
            return ValidationResult.error("Invalid offset for VolumeId");
         }
      }
   }

   public TriggerVolumeToolCreateResponse clone() {
      TriggerVolumeToolCreateResponse copy = new TriggerVolumeToolCreateResponse();
      copy.volumeId = this.volumeId;
      copy.volumeIds = this.volumeIds != null ? Arrays.copyOf(this.volumeIds, this.volumeIds.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof TriggerVolumeToolCreateResponse other)
            ? false
            : Objects.equals(this.volumeId, other.volumeId) && Arrays.equals((Object[])this.volumeIds, (Object[])other.volumeIds);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.volumeId);
      return 31 * result + Arrays.hashCode((Object[])this.volumeIds);
   }
}
