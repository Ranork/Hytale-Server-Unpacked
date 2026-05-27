package com.hypixel.hytale.protocol.packets.auth;

import com.hypixel.hytale.protocol.HostAddress;
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

public class ClientReferral implements Packet, ToClientPacket {
   public static final int PACKET_ID = 18;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 5141;
   @Nullable
   public HostAddress hostTo;
   @Nullable
   public byte[] data;

   @Override
   public int getId() {
      return 18;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ClientReferral() {
   }

   public ClientReferral(@Nullable HostAddress hostTo, @Nullable byte[] data) {
      this.hostTo = hostTo;
      this.data = data;
   }

   public ClientReferral(@Nonnull ClientReferral other) {
      this.hostTo = other.hostTo;
      this.data = other.data;
   }

   @Nonnull
   public static ClientReferral deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 9) {
         throw ProtocolException.bufferTooSmall("ClientReferral", 9, buf.readableBytes() - offset);
      } else {
         ClientReferral obj = new ClientReferral();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            int varPosBase0 = buf.getIntLE(offset + 1);
            if (varPosBase0 < 0 || varPosBase0 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("HostTo", varPosBase0, buf.readableBytes());
            }

            int varPos0 = offset + 9 + varPosBase0;
            obj.hostTo = HostAddress.deserialize(buf, varPos0);
         }

         if ((nullBits & 2) != 0) {
            int varPosBase1 = buf.getIntLE(offset + 5);
            if (varPosBase1 < 0 || varPosBase1 > buf.writerIndex() - offset - 9) {
               throw ProtocolException.invalidOffset("Data", varPosBase1, buf.readableBytes());
            }

            int varPos1 = offset + 9 + varPosBase1;
            int dataCount = VarInt.peek(buf, varPos1);
            if (dataCount < 0) {
               throw ProtocolException.invalidVarInt("Data");
            }

            int varIntLen = VarInt.size(dataCount);
            if (dataCount > 4096) {
               throw ProtocolException.arrayTooLong("Data", dataCount, 4096);
            }

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
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 9;
      if ((nullBits & 1) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 1);
         if (fieldOffset0 < 0 || fieldOffset0 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("HostTo", fieldOffset0, maxEnd);
         }

         int pos0 = offset + 9 + fieldOffset0;
         pos0 += HostAddress.computeBytesConsumed(buf, pos0);
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 2) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 5);
         if (fieldOffset1 < 0 || fieldOffset1 > buf.writerIndex() - offset - 9) {
            throw ProtocolException.invalidOffset("Data", fieldOffset1, maxEnd);
         }

         int pos1 = offset + 9 + fieldOffset1;
         int arrLen = VarInt.peek(buf, pos1);
         pos1 += VarInt.size(arrLen) + arrLen * 1;
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
   public static HostAddress getHostTo(MemorySegment mem) {
      return getHostTo(mem, 0);
   }

   @Nullable
   public static HostAddress getHostTo(MemorySegment mem, int offset) {
      return hasHostTo(mem, offset) ? HostAddress.toObject(mem, offset + getValidatedOffset(mem, offset, 1, 9, "HostTo")) : null;
   }

   @Nullable
   public static byte[] getData(MemorySegment mem) {
      return getData(mem, 0);
   }

   @Nullable
   public static byte[] getData(MemorySegment mem, int offset) {
      if (!hasData(mem, offset)) {
         return null;
      } else {
         int off = offset + getValidatedOffset(mem, offset, 5, 9, "Data");
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("Data", len);
         } else if (len > 4096) {
            throw ProtocolException.arrayTooLong("Data", len, 4096);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Data", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               byte[] data = new byte[len];
               MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, data, 0, len);
               return data;
            }
         }
      }
   }

   public static boolean hasHostTo(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasData(MemorySegment mem, int offset) {
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

   public static ClientReferral toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ClientReferral toObject(MemorySegment mem, int offset) {
      if (offset + 9 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ClientReferral", offset + 9, (int)mem.byteSize());
      } else {
         byte[] data = null;
         if (hasData(mem, offset)) {
            int off = offset + getValidatedOffset(mem, offset, 5, 9, "Data");
            long packed = VarInt.getWithLength(mem, off);
            int len = (int)packed;
            if (len < 0) {
               throw ProtocolException.negativeLength("Data", len);
            }

            if (len > 4096) {
               throw ProtocolException.arrayTooLong("Data", len, 4096);
            }

            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("Data", off + lenOffset + len * 1, (int)mem.byteSize());
            }

            off += lenOffset;
            data = new byte[len];
            MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, data, 0, len);
         }

         return new ClientReferral(hasHostTo(mem, offset) ? HostAddress.toObject(mem, offset + getValidatedOffset(mem, offset, 1, 9, "HostTo")) : null, data);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.hostTo != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.data != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      int hostToOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int dataOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.hostTo != null) {
         buf.setIntLE(hostToOffsetSlot, buf.writerIndex() - varBlockStart);
         this.hostTo.serialize(buf);
      } else {
         buf.setIntLE(hostToOffsetSlot, -1);
      }

      if (this.data != null) {
         buf.setIntLE(dataOffsetSlot, buf.writerIndex() - varBlockStart);
         if (this.data.length > 4096) {
            throw ProtocolException.arrayTooLong("Data", this.data.length, 4096);
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
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.hostTo != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.data != null) {
         nullBits = (byte)(nullBits | 2);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 9;
      if (this.hostTo != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), varOffset - offset - 9);
         varOffset += this.hostTo.serialize(mem, varOffset);
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 1), -1);
      }

      if (this.data != null) {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), varOffset - offset - 9);
         if (this.data.length > 4096) {
            throw ProtocolException.arrayTooLong("Data", this.data.length, 4096);
         }

         varOffset += VarInt.set(mem, varOffset, this.data.length);
         MemorySegment.copy(this.data, 0, mem, PacketIO.PROTO_BYTE, varOffset, this.data.length);
         varOffset += this.data.length * 1;
      } else {
         mem.set(PacketIO.PROTO_INT, (long)(offset + 5), -1);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 9;
      if (this.hostTo != null) {
         size += this.hostTo.computeSize();
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
            int hostToOffset = buffer.getIntLE(offset + 1);
            if (hostToOffset < 0 || hostToOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for HostTo");
            }

            int pos = offset + 9 + hostToOffset;
            ValidationResult hostToResult = HostAddress.validateStructure(buffer, pos);
            if (!hostToResult.isValid()) {
               return ValidationResult.error("Invalid HostTo: " + hostToResult.error());
            }

            pos += HostAddress.computeBytesConsumed(buffer, pos);
         }

         if ((nullBits & 2) != 0) {
            int dataOffset = buffer.getIntLE(offset + 5);
            if (dataOffset < 0 || dataOffset > buffer.writerIndex() - offset - 9) {
               return ValidationResult.error("Invalid offset for Data");
            }

            int pos = offset + 9 + dataOffset;
            int dataCount = VarInt.peek(buffer, pos);
            if (dataCount < 0) {
               return ValidationResult.error("Invalid array count for Data");
            }

            if (dataCount > 4096) {
               return ValidationResult.error("Data exceeds max length 4096");
            }

            pos += VarInt.size(dataCount);
            pos += dataCount * 1;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Data");
            }
         }

         return ValidationResult.OK;
      }
   }

   public ClientReferral clone() {
      ClientReferral copy = new ClientReferral();
      copy.hostTo = this.hostTo != null ? this.hostTo.clone() : null;
      copy.data = this.data != null ? Arrays.copyOf(this.data, this.data.length) : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ClientReferral other) ? false : Objects.equals(this.hostTo, other.hostTo) && Arrays.equals(this.data, other.data);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.hostTo);
      return 31 * result + Arrays.hashCode(this.data);
   }
}
