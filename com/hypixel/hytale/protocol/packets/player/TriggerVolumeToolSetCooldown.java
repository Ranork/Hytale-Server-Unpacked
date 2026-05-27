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

public class TriggerVolumeToolSetCooldown implements Packet, ToServerPacket {
   public static final int PACKET_ID = 501;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 16384010;
   @Nonnull
   public String volumeId = "";
   public float cooldown;
   public byte cooldownMode;

   @Override
   public int getId() {
      return 501;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public TriggerVolumeToolSetCooldown() {
   }

   public TriggerVolumeToolSetCooldown(@Nonnull String volumeId, float cooldown, byte cooldownMode) {
      this.volumeId = volumeId;
      this.cooldown = cooldown;
      this.cooldownMode = cooldownMode;
   }

   public TriggerVolumeToolSetCooldown(@Nonnull TriggerVolumeToolSetCooldown other) {
      this.volumeId = other.volumeId;
      this.cooldown = other.cooldown;
      this.cooldownMode = other.cooldownMode;
   }

   @Nonnull
   public static TriggerVolumeToolSetCooldown deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 5) {
         throw ProtocolException.bufferTooSmall("TriggerVolumeToolSetCooldown", 5, buf.readableBytes() - offset);
      } else {
         TriggerVolumeToolSetCooldown obj = new TriggerVolumeToolSetCooldown();
         obj.cooldown = buf.getFloatLE(offset + 0);
         obj.cooldownMode = buf.getByte(offset + 4);
         int pos = offset + 5;
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
      int pos = offset + 5;
      int sl = VarInt.peek(buf, pos);
      pos += VarInt.size(sl) + sl;
      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 5L;
   }

   public static String getVolumeId(MemorySegment mem) {
      return getVolumeId(mem, 0);
   }

   public static String getVolumeId(MemorySegment mem, int offset) {
      return PacketIO.readVarString("VolumeId", mem, offset + 5, 4096000, PacketIO.UTF8);
   }

   public static float getCooldown(MemorySegment mem) {
      return getCooldown(mem, 0);
   }

   public static float getCooldown(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0));
   }

   public static byte getCooldownMode(MemorySegment mem) {
      return getCooldownMode(mem, 0);
   }

   public static byte getCooldownMode(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BYTE, (long)(offset + 4));
   }

   public static TriggerVolumeToolSetCooldown toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static TriggerVolumeToolSetCooldown toObject(MemorySegment mem, int offset) {
      if (offset + 5 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("TriggerVolumeToolSetCooldown", offset + 5, (int)mem.byteSize());
      } else {
         return new TriggerVolumeToolSetCooldown(
            PacketIO.readVarString("VolumeId", mem, offset + 5, 4096000, PacketIO.UTF8),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_BYTE, (long)(offset + 4))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.cooldown);
      buf.writeByte(this.cooldownMode);
      PacketIO.writeVarString(buf, this.volumeId, 4096000);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 0), this.cooldown);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 4), this.cooldownMode);
      int varOffset = offset + 5;
      varOffset += PacketIO.writeVarString(mem, varOffset, this.volumeId, 4096000);
      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 5;
      return size + PacketIO.stringSize(this.volumeId);
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 5) {
         return ValidationResult.error("Buffer too small: expected at least 5 bytes");
      } else {
         int pos = offset + 5;
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

   public TriggerVolumeToolSetCooldown clone() {
      TriggerVolumeToolSetCooldown copy = new TriggerVolumeToolSetCooldown();
      copy.volumeId = this.volumeId;
      copy.cooldown = this.cooldown;
      copy.cooldownMode = this.cooldownMode;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof TriggerVolumeToolSetCooldown other)
            ? false
            : Objects.equals(this.volumeId, other.volumeId) && this.cooldown == other.cooldown && this.cooldownMode == other.cooldownMode;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.volumeId, this.cooldown, this.cooldownMode);
   }
}
