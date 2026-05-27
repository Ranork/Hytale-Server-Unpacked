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
import org.joml.Vector3fc;

public class TriggerVolumeToolSetColor implements Packet, ToServerPacket {
   public static final int PACKET_ID = 492;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 12;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 16384017;
   @Nonnull
   public String volumeId = "";
   @Nonnull
   public Vector3fc color = PacketIO.ZERO_VECTOR3;

   @Override
   public int getId() {
      return 492;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public TriggerVolumeToolSetColor() {
   }

   public TriggerVolumeToolSetColor(@Nonnull String volumeId, @Nonnull Vector3fc color) {
      this.volumeId = volumeId;
      this.color = color;
   }

   public TriggerVolumeToolSetColor(@Nonnull TriggerVolumeToolSetColor other) {
      this.volumeId = other.volumeId;
      this.color = other.color;
   }

   @Nonnull
   public static TriggerVolumeToolSetColor deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 12) {
         throw ProtocolException.bufferTooSmall("TriggerVolumeToolSetColor", 12, buf.readableBytes() - offset);
      } else {
         TriggerVolumeToolSetColor obj = new TriggerVolumeToolSetColor();
         obj.color = PacketIO.readVector3f(buf, offset + 0);
         int pos = offset + 12;
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
      int pos = offset + 12;
      int sl = VarInt.peek(buf, pos);
      pos += VarInt.size(sl) + sl;
      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 12L;
   }

   public static String getVolumeId(MemorySegment mem) {
      return getVolumeId(mem, 0);
   }

   public static String getVolumeId(MemorySegment mem, int offset) {
      return PacketIO.readVarString("VolumeId", mem, offset + 12, 4096000, PacketIO.UTF8);
   }

   public static Vector3fc getColor(MemorySegment mem) {
      return getColor(mem, 0);
   }

   public static Vector3fc getColor(MemorySegment mem, int offset) {
      return PacketIO.readVector3f(mem, offset + 0);
   }

   public static TriggerVolumeToolSetColor toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static TriggerVolumeToolSetColor toObject(MemorySegment mem, int offset) {
      if (offset + 12 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("TriggerVolumeToolSetColor", offset + 12, (int)mem.byteSize());
      } else {
         return new TriggerVolumeToolSetColor(
            PacketIO.readVarString("VolumeId", mem, offset + 12, 4096000, PacketIO.UTF8), PacketIO.readVector3f(mem, offset + 0)
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      PacketIO.writeVector3f(buf, this.color);
      PacketIO.writeVarString(buf, this.volumeId, 4096000);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      PacketIO.writeVector3f(mem, offset + 0, this.color);
      int varOffset = offset + 12;
      varOffset += PacketIO.writeVarString(mem, varOffset, this.volumeId, 4096000);
      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 12;
      return size + PacketIO.stringSize(this.volumeId);
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 12) {
         return ValidationResult.error("Buffer too small: expected at least 12 bytes");
      } else {
         int pos = offset + 12;
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

   public TriggerVolumeToolSetColor clone() {
      TriggerVolumeToolSetColor copy = new TriggerVolumeToolSetColor();
      copy.volumeId = this.volumeId;
      copy.color = this.color;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof TriggerVolumeToolSetColor other)
            ? false
            : Objects.equals(this.volumeId, other.volumeId) && Objects.equals(this.color, other.color);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.volumeId, this.color);
   }
}
