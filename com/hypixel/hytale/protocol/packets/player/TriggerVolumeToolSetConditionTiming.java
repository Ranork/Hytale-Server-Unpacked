package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class TriggerVolumeToolSetConditionTiming implements Packet, ToServerPacket {
   public static final int PACKET_ID = 506;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 16384006;
   @Nonnull
   public String volumeId = "";
   @Nonnull
   public TriggerVolumeConditionTiming conditionTiming = TriggerVolumeConditionTiming.BeforeVolumeDelay;

   @Override
   public int getId() {
      return 506;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public TriggerVolumeToolSetConditionTiming() {
   }

   public TriggerVolumeToolSetConditionTiming(@Nonnull String volumeId, @Nonnull TriggerVolumeConditionTiming conditionTiming) {
      this.volumeId = volumeId;
      this.conditionTiming = conditionTiming;
   }

   public TriggerVolumeToolSetConditionTiming(@Nonnull TriggerVolumeToolSetConditionTiming other) {
      this.volumeId = other.volumeId;
      this.conditionTiming = other.conditionTiming;
   }

   @Nonnull
   public static TriggerVolumeToolSetConditionTiming deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("TriggerVolumeToolSetConditionTiming", 1, buf.readableBytes() - offset);
      } else {
         TriggerVolumeToolSetConditionTiming obj = new TriggerVolumeToolSetConditionTiming();
         obj.conditionTiming = TriggerVolumeConditionTiming.fromValue(buf.getByte(offset + 0));
         int pos = offset + 1;
         int volumeIdLen = VarInt.peek(buf, pos);
         if (volumeIdLen < 0) {
            throw ProtocolException.invalidVarInt("VolumeId");
         } else {
            int volumeIdVarLen = VarInt.size(volumeIdLen);
            if (volumeIdLen > 4096000) {
               throw ProtocolException.stringTooLong("VolumeId", volumeIdLen, 4096000);
            } else if (pos + volumeIdVarLen + volumeIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("VolumeId", pos + volumeIdVarLen + volumeIdLen, buf.readableBytes());
            } else {
               obj.volumeId = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
               pos += volumeIdVarLen + volumeIdLen;
               return obj;
            }
         }
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 1;
      int sl = VarInt.peek(buf, pos);
      pos += VarInt.size(sl) + sl;
      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   public static String getVolumeId(MemorySegment mem) {
      return getVolumeId(mem, 0);
   }

   public static String getVolumeId(MemorySegment mem, int offset) {
      return PacketIO.readVarString("VolumeId", mem, offset + 1, 4096000, PacketIO.UTF8);
   }

   public static TriggerVolumeConditionTiming getConditionTiming(MemorySegment mem) {
      return getConditionTiming(mem, 0);
   }

   public static TriggerVolumeConditionTiming getConditionTiming(MemorySegment mem, int offset) {
      return TriggerVolumeConditionTiming.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)));
   }

   public static TriggerVolumeToolSetConditionTiming toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static TriggerVolumeToolSetConditionTiming toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("TriggerVolumeToolSetConditionTiming", offset + 1, (int)mem.byteSize());
      } else {
         return new TriggerVolumeToolSetConditionTiming(
            PacketIO.readVarString("VolumeId", mem, offset + 1, 4096000, PacketIO.UTF8),
            TriggerVolumeConditionTiming.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.conditionTiming.getValue());
      PacketIO.writeVarString(buf, this.volumeId, 4096000);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), (byte)this.conditionTiming.getValue());
      int varOffset = offset + 1;
      varOffset += PacketIO.writeVarString(mem, varOffset, this.volumeId, 4096000);
      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 1;
      return size + PacketIO.stringSize(this.volumeId);
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 1) {
         return ValidationResult.error("Buffer too small: expected at least 1 bytes");
      } else {
         int v = buffer.getByte(offset + 0) & 255;
         if (v >= 2) {
            return ValidationResult.error("Invalid TriggerVolumeConditionTiming value for ConditionTiming");
         } else {
            v = offset + 1;
            int volumeIdLen = VarInt.peek(buffer, v);
            if (volumeIdLen < 0) {
               return ValidationResult.error("Invalid string length for VolumeId");
            } else if (volumeIdLen > 4096000) {
               return ValidationResult.error("VolumeId exceeds max length 4096000");
            } else {
               v += VarInt.size(volumeIdLen);
               v += volumeIdLen;
               return v > buffer.writerIndex() ? ValidationResult.error("Buffer overflow reading VolumeId") : ValidationResult.OK;
            }
         }
      }
   }

   public TriggerVolumeToolSetConditionTiming clone() {
      TriggerVolumeToolSetConditionTiming copy = new TriggerVolumeToolSetConditionTiming();
      copy.volumeId = this.volumeId;
      copy.conditionTiming = this.conditionTiming;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof TriggerVolumeToolSetConditionTiming other)
            ? false
            : Objects.equals(this.volumeId, other.volumeId) && Objects.equals(this.conditionTiming, other.conditionTiming);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.volumeId, this.conditionTiming);
   }
}
