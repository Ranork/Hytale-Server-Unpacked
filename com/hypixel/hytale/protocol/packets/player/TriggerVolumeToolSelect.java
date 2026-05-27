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
import javax.annotation.Nullable;

public class TriggerVolumeToolSelect implements Packet, ToServerPacket {
   public static final int PACKET_ID = 490;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 1;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 1;
   public static final int MAX_SIZE = 16384006;
   @Nullable
   public String volumeId;

   @Override
   public int getId() {
      return 490;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public TriggerVolumeToolSelect() {
   }

   public TriggerVolumeToolSelect(@Nullable String volumeId) {
      this.volumeId = volumeId;
   }

   public TriggerVolumeToolSelect(@Nonnull TriggerVolumeToolSelect other) {
      this.volumeId = other.volumeId;
   }

   @Nonnull
   public static TriggerVolumeToolSelect deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 1) {
         throw ProtocolException.bufferTooSmall("TriggerVolumeToolSelect", 1, buf.readableBytes() - offset);
      } else {
         TriggerVolumeToolSelect obj = new TriggerVolumeToolSelect();
         byte nullBits = buf.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int volumeIdLen = VarInt.peek(buf, pos);
            if (volumeIdLen < 0) {
               throw ProtocolException.invalidVarInt("VolumeId");
            }

            int volumeIdVarLen = VarInt.size(volumeIdLen);
            if (volumeIdLen > 4096000) {
               throw ProtocolException.stringTooLong("VolumeId", volumeIdLen, 4096000);
            }

            if (pos + volumeIdVarLen + volumeIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("VolumeId", pos + volumeIdVarLen + volumeIdLen, buf.readableBytes());
            }

            obj.volumeId = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
            pos += volumeIdVarLen + volumeIdLen;
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int pos = offset + 1;
      if ((nullBits & 1) != 0) {
         int sl = VarInt.peek(buf, pos);
         pos += VarInt.size(sl) + sl;
      }

      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 1L;
   }

   @Nullable
   public static String getVolumeId(MemorySegment mem) {
      return getVolumeId(mem, 0);
   }

   @Nullable
   public static String getVolumeId(MemorySegment mem, int offset) {
      return hasVolumeId(mem, offset) ? PacketIO.readVarString("VolumeId", mem, offset + 1, 4096000, PacketIO.UTF8) : null;
   }

   public static boolean hasVolumeId(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static TriggerVolumeToolSelect toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static TriggerVolumeToolSelect toObject(MemorySegment mem, int offset) {
      if (offset + 1 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("TriggerVolumeToolSelect", offset + 1, (int)mem.byteSize());
      } else {
         return new TriggerVolumeToolSelect(hasVolumeId(mem, offset) ? PacketIO.readVarString("VolumeId", mem, offset + 1, 4096000, PacketIO.UTF8) : null);
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.volumeId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.volumeId != null) {
         PacketIO.writeVarString(buf, this.volumeId, 4096000);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.volumeId != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      int varOffset = offset + 1;
      if (this.volumeId != null) {
         varOffset += PacketIO.writeVarString(mem, varOffset, this.volumeId, 4096000);
      }

      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 1;
      if (this.volumeId != null) {
         size += PacketIO.stringSize(this.volumeId);
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 1) {
         return ValidationResult.error("Buffer too small: expected at least 1 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int pos = offset + 1;
         if ((nullBits & 1) != 0) {
            int volumeIdLen = VarInt.peek(buffer, pos);
            if (volumeIdLen < 0) {
               return ValidationResult.error("Invalid string length for VolumeId");
            }

            if (volumeIdLen > 4096000) {
               return ValidationResult.error("VolumeId exceeds max length 4096000");
            }

            pos += VarInt.size(volumeIdLen);
            pos += volumeIdLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading VolumeId");
            }
         }

         return ValidationResult.OK;
      }
   }

   public TriggerVolumeToolSelect clone() {
      TriggerVolumeToolSelect copy = new TriggerVolumeToolSelect();
      copy.volumeId = this.volumeId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof TriggerVolumeToolSelect other ? Objects.equals(this.volumeId, other.volumeId) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.volumeId);
   }
}
