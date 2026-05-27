package com.hypixel.hytale.protocol.packets.voice;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import javax.annotation.Nonnull;

public class VoiceData implements Packet, ToServerPacket {
   public static final int PACKET_ID = 450;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 6;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 6;
   public static final int MAX_SIZE = 523;
   public short sequenceNumber;
   public int timestamp;
   @Nonnull
   public byte[] opusData = new byte[0];

   @Override
   public int getId() {
      return 450;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Voice;
   }

   public VoiceData() {
   }

   public VoiceData(short sequenceNumber, int timestamp, @Nonnull byte[] opusData) {
      this.sequenceNumber = sequenceNumber;
      this.timestamp = timestamp;
      this.opusData = opusData;
   }

   public VoiceData(@Nonnull VoiceData other) {
      this.sequenceNumber = other.sequenceNumber;
      this.timestamp = other.timestamp;
      this.opusData = other.opusData;
   }

   @Nonnull
   public static VoiceData deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 6) {
         throw ProtocolException.bufferTooSmall("VoiceData", 6, buf.readableBytes() - offset);
      } else {
         VoiceData obj = new VoiceData();
         obj.sequenceNumber = buf.getShortLE(offset + 0);
         obj.timestamp = buf.getIntLE(offset + 2);
         int pos = offset + 6;
         int opusDataCount = VarInt.peek(buf, pos);
         if (opusDataCount < 0) {
            throw ProtocolException.invalidVarInt("OpusData");
         } else {
            int opusDataVarLen = VarInt.size(opusDataCount);
            if (opusDataCount > 512) {
               throw ProtocolException.arrayTooLong("OpusData", opusDataCount, 512);
            } else if (pos + opusDataVarLen + opusDataCount * 1L > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("OpusData", pos + opusDataVarLen + opusDataCount * 1, buf.readableBytes());
            } else {
               pos += opusDataVarLen;
               obj.opusData = new byte[opusDataCount];

               for (int i = 0; i < opusDataCount; i++) {
                  obj.opusData[i] = buf.getByte(pos + i * 1);
               }

               pos += opusDataCount * 1;
               return obj;
            }
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 6;
      int arrLen = VarInt.peek(buf, pos);
      pos += VarInt.size(arrLen) + arrLen * 1;
      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 6L;
   }

   public static short getSequenceNumber(MemorySegment mem) {
      return getSequenceNumber(mem, 0);
   }

   public static short getSequenceNumber(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_SHORT, (long)(offset + 0));
   }

   public static int getTimestamp(MemorySegment mem) {
      return getTimestamp(mem, 0);
   }

   public static int getTimestamp(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 2));
   }

   public static byte[] getOpusData(MemorySegment mem) {
      return getOpusData(mem, 0);
   }

   public static byte[] getOpusData(MemorySegment mem, int offset) {
      int off = offset + 6;
      long packed = VarInt.getWithLength(mem, off);
      int len = (int)packed;
      if (len < 0) {
         throw ProtocolException.negativeLength("OpusData", len);
      } else if (len > 512) {
         throw ProtocolException.arrayTooLong("OpusData", len, 512);
      } else {
         int lenOffset = (int)(packed >>> 32);
         if (off + lenOffset + len * 1L > mem.byteSize()) {
            throw ProtocolException.bufferTooSmall("OpusData", off + lenOffset + len * 1, (int)mem.byteSize());
         } else {
            off += lenOffset;
            byte[] data = new byte[len];
            MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, data, 0, len);
            return data;
         }
      }
   }

   public static VoiceData toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static VoiceData toObject(MemorySegment mem, int offset) {
      if (offset + 6 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("VoiceData", offset + 6, (int)mem.byteSize());
      } else {
         int off = offset + 6;
         long packed = VarInt.getWithLength(mem, off);
         int len = (int)packed;
         if (len < 0) {
            throw ProtocolException.negativeLength("OpusData", len);
         } else if (len > 512) {
            throw ProtocolException.arrayTooLong("OpusData", len, 512);
         } else {
            int lenOffset = (int)(packed >>> 32);
            if (off + lenOffset + len * 1L > mem.byteSize()) {
               throw ProtocolException.bufferTooSmall("OpusData", off + lenOffset + len * 1, (int)mem.byteSize());
            } else {
               off += lenOffset;
               byte[] opusData = new byte[len];
               MemorySegment.copy(mem, PacketIO.PROTO_BYTE, off, opusData, 0, len);
               return new VoiceData(mem.get(PacketIO.PROTO_SHORT, (long)(offset + 0)), mem.get(PacketIO.PROTO_INT, (long)(offset + 2)), opusData);
            }
         }
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeShortLE(this.sequenceNumber);
      buf.writeIntLE(this.timestamp);
      if (this.opusData.length > 512) {
         throw ProtocolException.arrayTooLong("OpusData", this.opusData.length, 512);
      } else {
         VarInt.write(buf, this.opusData.length);

         for (byte item : this.opusData) {
            buf.writeByte(item);
         }
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_SHORT, (long)(offset + 0), this.sequenceNumber);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 2), this.timestamp);
      int varOffset = offset + 6;
      if (this.opusData.length > 512) {
         throw ProtocolException.arrayTooLong("OpusData", this.opusData.length, 512);
      } else {
         varOffset += VarInt.set(mem, varOffset, this.opusData.length);
         MemorySegment.copy(this.opusData, 0, mem, PacketIO.PROTO_BYTE, varOffset, this.opusData.length);
         varOffset += this.opusData.length * 1;
         return varOffset - offset;
      }
   }

   @Override
   public int computeSize() {
      int size = 6;
      return size + VarInt.size(this.opusData.length) + this.opusData.length * 1;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 6) {
         return ValidationResult.error("Buffer too small: expected at least 6 bytes");
      } else {
         int pos = offset + 6;
         int opusDataCount = VarInt.peek(buffer, pos);
         if (opusDataCount < 0) {
            return ValidationResult.error("Invalid array count for OpusData");
         } else if (opusDataCount > 512) {
            return ValidationResult.error("OpusData exceeds max length 512");
         } else {
            pos += VarInt.size(opusDataCount);
            pos += opusDataCount * 1;
            return pos > buffer.writerIndex() ? ValidationResult.error("Buffer overflow reading OpusData") : ValidationResult.OK;
         }
      }
   }

   public VoiceData clone() {
      VoiceData copy = new VoiceData();
      copy.sequenceNumber = this.sequenceNumber;
      copy.timestamp = this.timestamp;
      copy.opusData = Arrays.copyOf(this.opusData, this.opusData.length);
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof VoiceData other)
            ? false
            : this.sequenceNumber == other.sequenceNumber && this.timestamp == other.timestamp && Arrays.equals(this.opusData, other.opusData);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Short.hashCode(this.sequenceNumber);
      result = 31 * result + Integer.hashCode(this.timestamp);
      return 31 * result + Arrays.hashCode(this.opusData);
   }
}
