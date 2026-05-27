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

public class TriggerVolumeToolGroupMove implements Packet, ToServerPacket {
   public static final int PACKET_ID = 489;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 12;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 16384017;
   @Nonnull
   public String groupId = "";
   @Nonnull
   public Vector3fc moveDelta = PacketIO.ZERO_VECTOR3;

   @Override
   public int getId() {
      return 489;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public TriggerVolumeToolGroupMove() {
   }

   public TriggerVolumeToolGroupMove(@Nonnull String groupId, @Nonnull Vector3fc moveDelta) {
      this.groupId = groupId;
      this.moveDelta = moveDelta;
   }

   public TriggerVolumeToolGroupMove(@Nonnull TriggerVolumeToolGroupMove other) {
      this.groupId = other.groupId;
      this.moveDelta = other.moveDelta;
   }

   @Nonnull
   public static TriggerVolumeToolGroupMove deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 12) {
         throw ProtocolException.bufferTooSmall("TriggerVolumeToolGroupMove", 12, buf.readableBytes() - offset);
      } else {
         TriggerVolumeToolGroupMove obj = new TriggerVolumeToolGroupMove();
         obj.moveDelta = PacketIO.readVector3f(buf, offset + 0);
         int pos = offset + 12;
         int groupIdLen = VarInt.peek(buf, pos);
         if (groupIdLen < 0) {
            throw ProtocolException.invalidVarInt("GroupId");
         } else {
            int groupIdVarLen = VarInt.size(groupIdLen);
            if (groupIdLen > 4096000) {
               throw ProtocolException.stringTooLong("GroupId", groupIdLen, 4096000);
            } else if (pos + groupIdVarLen + groupIdLen > buf.readableBytes()) {
               throw ProtocolException.bufferTooSmall("GroupId", pos + groupIdVarLen + groupIdLen, buf.readableBytes());
            } else {
               obj.groupId = PacketIO.readVarString(buf, pos, PacketIO.UTF8);
               pos += groupIdVarLen + groupIdLen;
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

   public static String getGroupId(MemorySegment mem) {
      return getGroupId(mem, 0);
   }

   public static String getGroupId(MemorySegment mem, int offset) {
      return PacketIO.readVarString("GroupId", mem, offset + 12, 4096000, PacketIO.UTF8);
   }

   public static Vector3fc getMoveDelta(MemorySegment mem) {
      return getMoveDelta(mem, 0);
   }

   public static Vector3fc getMoveDelta(MemorySegment mem, int offset) {
      return PacketIO.readVector3f(mem, offset + 0);
   }

   public static TriggerVolumeToolGroupMove toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static TriggerVolumeToolGroupMove toObject(MemorySegment mem, int offset) {
      if (offset + 12 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("TriggerVolumeToolGroupMove", offset + 12, (int)mem.byteSize());
      } else {
         return new TriggerVolumeToolGroupMove(
            PacketIO.readVarString("GroupId", mem, offset + 12, 4096000, PacketIO.UTF8), PacketIO.readVector3f(mem, offset + 0)
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      PacketIO.writeVector3f(buf, this.moveDelta);
      PacketIO.writeVarString(buf, this.groupId, 4096000);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      PacketIO.writeVector3f(mem, offset + 0, this.moveDelta);
      int varOffset = offset + 12;
      varOffset += PacketIO.writeVarString(mem, varOffset, this.groupId, 4096000);
      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 12;
      return size + PacketIO.stringSize(this.groupId);
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 12) {
         return ValidationResult.error("Buffer too small: expected at least 12 bytes");
      } else {
         int pos = offset + 12;
         int groupIdLen = VarInt.peek(buffer, pos);
         if (groupIdLen < 0) {
            return ValidationResult.error("Invalid string length for GroupId");
         } else if (groupIdLen > 4096000) {
            return ValidationResult.error("GroupId exceeds max length 4096000");
         } else {
            pos += VarInt.size(groupIdLen);
            pos += groupIdLen;
            return pos > buffer.writerIndex() ? ValidationResult.error("Buffer overflow reading GroupId") : ValidationResult.OK;
         }
      }
   }

   public TriggerVolumeToolGroupMove clone() {
      TriggerVolumeToolGroupMove copy = new TriggerVolumeToolGroupMove();
      copy.groupId = this.groupId;
      copy.moveDelta = this.moveDelta;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof TriggerVolumeToolGroupMove other)
            ? false
            : Objects.equals(this.groupId, other.groupId) && Objects.equals(this.moveDelta, other.moveDelta);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.groupId, this.moveDelta);
   }
}
