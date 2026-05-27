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
import java.util.Objects;
import javax.annotation.Nonnull;

public class RemoveTriggerVolumeDisplay implements Packet, ToClientPacket {
   public static final int PACKET_ID = 472;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 0;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 0;
   public static final int MAX_SIZE = 16384005;
   @Nonnull
   public String volumeId = "";

   @Override
   public int getId() {
      return 472;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public RemoveTriggerVolumeDisplay() {
   }

   public RemoveTriggerVolumeDisplay(@Nonnull String volumeId) {
      this.volumeId = volumeId;
   }

   public RemoveTriggerVolumeDisplay(@Nonnull RemoveTriggerVolumeDisplay other) {
      this.volumeId = other.volumeId;
   }

   @Nonnull
   public static RemoveTriggerVolumeDisplay deserialize(@Nonnull ByteBuf buf, int offset) {
      RemoveTriggerVolumeDisplay obj = new RemoveTriggerVolumeDisplay();
      int pos = offset + 0;
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

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 0;
      int sl = VarInt.peek(buf, pos);
      pos += VarInt.size(sl) + sl;
      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 0L;
   }

   public static String getVolumeId(MemorySegment mem) {
      return getVolumeId(mem, 0);
   }

   public static String getVolumeId(MemorySegment mem, int offset) {
      return PacketIO.readVarString("VolumeId", mem, offset + 0, 4096000, PacketIO.UTF8);
   }

   public static RemoveTriggerVolumeDisplay toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static RemoveTriggerVolumeDisplay toObject(MemorySegment mem, int offset) {
      if (offset + 0 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("RemoveTriggerVolumeDisplay", offset + 0, (int)mem.byteSize());
      } else {
         return new RemoveTriggerVolumeDisplay(PacketIO.readVarString("VolumeId", mem, offset + 0, 4096000, PacketIO.UTF8));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      PacketIO.writeVarString(buf, this.volumeId, 4096000);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      int varOffset = offset + 0;
      varOffset += PacketIO.writeVarString(mem, varOffset, this.volumeId, 4096000);
      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 0;
      return size + PacketIO.stringSize(this.volumeId);
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 0) {
         return ValidationResult.error("Buffer too small: expected at least 0 bytes");
      } else {
         int pos = offset + 0;
         int volumeIdLen = VarInt.peek(buffer, pos);
         if (volumeIdLen < 0) {
            return ValidationResult.error("Invalid string length for VolumeId");
         } else if (volumeIdLen > 4096000) {
            return ValidationResult.error("VolumeId exceeds max length 4096000");
         } else {
            pos += VarInt.size(volumeIdLen);
            pos += volumeIdLen;
            return pos > buffer.writerIndex() ? ValidationResult.error("Buffer overflow reading VolumeId") : ValidationResult.OK;
         }
      }
   }

   public RemoveTriggerVolumeDisplay clone() {
      RemoveTriggerVolumeDisplay copy = new RemoveTriggerVolumeDisplay();
      copy.volumeId = this.volumeId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof RemoveTriggerVolumeDisplay other ? Objects.equals(this.volumeId, other.volumeId) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.volumeId);
   }
}
