package com.hypixel.hytale.protocol.packets.voice;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class RelayedVoiceData implements Packet, ToClientPacket {
   public static final int PACKET_ID = 451;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 52;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 52;
   public static final int MAX_SIZE = 569;
   @Nonnull
   public UUID speakerId = new UUID(0L, 0L);
   public int entityId;
   public short sequenceNumber;
   public int timestamp;
   @Nullable
   public Position speakerPosition;
   public boolean speakerIsUnderwater;
   @Nonnull
   public byte[] opusData = new byte[0];

   @Override
   public int getId() {
      return 451;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Voice;
   }

   public RelayedVoiceData() {
   }

   public RelayedVoiceData(
      @Nonnull UUID speakerId,
      int entityId,
      short sequenceNumber,
      int timestamp,
      @Nullable Position speakerPosition,
      boolean speakerIsUnderwater,
      @Nonnull byte[] opusData
   ) {
      this.speakerId = speakerId;
      this.entityId = entityId;
      this.sequenceNumber = sequenceNumber;
      this.timestamp = timestamp;
      this.speakerPosition = speakerPosition;
      this.speakerIsUnderwater = speakerIsUnderwater;
      this.opusData = opusData;
   }

   public RelayedVoiceData(@Nonnull RelayedVoiceData other) {
      this.speakerId = other.speakerId;
      this.entityId = other.entityId;
      this.sequenceNumber = other.sequenceNumber;
      this.timestamp = other.timestamp;
      this.speakerPosition = other.speakerPosition;
      this.speakerIsUnderwater = other.speakerIsUnderwater;
      this.opusData = other.opusData;
   }

   @Nonnull
   public static RelayedVoiceData deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 52) {
         throw ProtocolException.bufferTooSmall("RelayedVoiceData", 52, buf.readableBytes() - offset);
      } else {
         RelayedVoiceData obj = new RelayedVoiceData();
         byte nullBits = buf.getByte(offset);
         obj.speakerId = PacketIO.readUUID(buf, offset + 1);
         obj.entityId = buf.getIntLE(offset + 17);
         obj.sequenceNumber = buf.getShortLE(offset + 21);
         obj.timestamp = buf.getIntLE(offset + 23);
         if ((nullBits & 1) != 0) {
            obj.speakerPosition = Position.deserialize(buf, offset + 27);
         }

         obj.speakerIsUnderwater = buf.getByte(offset + 51) != 0;
         int pos = offset + 52;
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
      byte nullBits = buf.getByte(offset);
      int pos = offset + 52;
      int arrLen = VarInt.peek(buf, pos);
      pos += VarInt.size(arrLen) + arrLen * 1;
      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 52L;
   }

   public static UUID getSpeakerId(MemorySegment mem) {
      return getSpeakerId(mem, 0);
   }

   public static UUID getSpeakerId(MemorySegment mem, int offset) {
      return PacketIO.readUUID(mem, offset + 1);
   }

   public static int getEntityId(MemorySegment mem) {
      return getEntityId(mem, 0);
   }

   public static int getEntityId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 17));
   }

   public static short getSequenceNumber(MemorySegment mem) {
      return getSequenceNumber(mem, 0);
   }

   public static short getSequenceNumber(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_SHORT, (long)(offset + 21));
   }

   public static int getTimestamp(MemorySegment mem) {
      return getTimestamp(mem, 0);
   }

   public static int getTimestamp(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 23));
   }

   @Nullable
   public static Position getSpeakerPosition(MemorySegment mem) {
      return getSpeakerPosition(mem, 0);
   }

   @Nullable
   public static Position getSpeakerPosition(MemorySegment mem, int offset) {
      return hasSpeakerPosition(mem, offset) ? Position.toObject(mem, offset + 27) : null;
   }

   public static boolean getSpeakerIsUnderwater(MemorySegment mem) {
      return getSpeakerIsUnderwater(mem, 0);
   }

   public static boolean getSpeakerIsUnderwater(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 51));
   }

   public static byte[] getOpusData(MemorySegment mem) {
      return getOpusData(mem, 0);
   }

   public static byte[] getOpusData(MemorySegment mem, int offset) {
      int off = offset + 52;
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

   public static boolean hasSpeakerPosition(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static RelayedVoiceData toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static RelayedVoiceData toObject(MemorySegment mem, int offset) {
      if (offset + 52 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("RelayedVoiceData", offset + 52, (int)mem.byteSize());
      } else {
         int off = offset + 52;
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
               return new RelayedVoiceData(
                  PacketIO.readUUID(mem, offset + 1),
                  mem.get(PacketIO.PROTO_INT, (long)(offset + 17)),
                  mem.get(PacketIO.PROTO_SHORT, (long)(offset + 21)),
                  mem.get(PacketIO.PROTO_INT, (long)(offset + 23)),
                  hasSpeakerPosition(mem, offset) ? Position.toObject(mem, offset + 27) : null,
                  mem.get(PacketIO.PROTO_BOOL, (long)(offset + 51)),
                  opusData
               );
            }
         }
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.speakerPosition != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      PacketIO.writeUUID(buf, this.speakerId);
      buf.writeIntLE(this.entityId);
      buf.writeShortLE(this.sequenceNumber);
      buf.writeIntLE(this.timestamp);
      if (this.speakerPosition != null) {
         this.speakerPosition.serialize(buf);
      } else {
         buf.writeZero(24);
      }

      buf.writeByte(this.speakerIsUnderwater ? 1 : 0);
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
      byte nullBits = 0;
      if (this.speakerPosition != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      PacketIO.writeUUID(mem, offset + 1, this.speakerId);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 17), this.entityId);
      mem.set(PacketIO.PROTO_SHORT, (long)(offset + 21), this.sequenceNumber);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 23), this.timestamp);
      if (this.speakerPosition != null) {
         this.speakerPosition.serialize(mem, offset + 27);
      } else {
         mem.asSlice(offset + 27, 24L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BOOL, offset + 51, this.speakerIsUnderwater);
      int varOffset = offset + 52;
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
      int size = 52;
      return size + VarInt.size(this.opusData.length) + this.opusData.length * 1;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 52) {
         return ValidationResult.error("Buffer too small: expected at least 52 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 52;
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

   public RelayedVoiceData clone() {
      RelayedVoiceData copy = new RelayedVoiceData();
      copy.speakerId = this.speakerId;
      copy.entityId = this.entityId;
      copy.sequenceNumber = this.sequenceNumber;
      copy.timestamp = this.timestamp;
      copy.speakerPosition = this.speakerPosition != null ? this.speakerPosition.clone() : null;
      copy.speakerIsUnderwater = this.speakerIsUnderwater;
      copy.opusData = Arrays.copyOf(this.opusData, this.opusData.length);
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof RelayedVoiceData other)
            ? false
            : Objects.equals(this.speakerId, other.speakerId)
               && this.entityId == other.entityId
               && this.sequenceNumber == other.sequenceNumber
               && this.timestamp == other.timestamp
               && Objects.equals(this.speakerPosition, other.speakerPosition)
               && this.speakerIsUnderwater == other.speakerIsUnderwater
               && Arrays.equals(this.opusData, other.opusData);
      }
   }

   @Override
   public int hashCode() {
      int result = 1;
      result = 31 * result + Objects.hashCode(this.speakerId);
      result = 31 * result + Integer.hashCode(this.entityId);
      result = 31 * result + Short.hashCode(this.sequenceNumber);
      result = 31 * result + Integer.hashCode(this.timestamp);
      result = 31 * result + Objects.hashCode(this.speakerPosition);
      result = 31 * result + Boolean.hashCode(this.speakerIsUnderwater);
      return 31 * result + Arrays.hashCode(this.opusData);
   }
}
