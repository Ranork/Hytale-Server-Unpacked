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

public class TriggerVolumeToolUngroup implements Packet, ToServerPacket {
   public static final int PACKET_ID = 488;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 0;
   public static final int VARIABLE_FIELD_COUNT = 1;
   public static final int VARIABLE_BLOCK_START = 0;
   public static final int MAX_SIZE = 16384005;
   @Nonnull
   public String groupId = "";

   @Override
   public int getId() {
      return 488;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public TriggerVolumeToolUngroup() {
   }

   public TriggerVolumeToolUngroup(@Nonnull String groupId) {
      this.groupId = groupId;
   }

   public TriggerVolumeToolUngroup(@Nonnull TriggerVolumeToolUngroup other) {
      this.groupId = other.groupId;
   }

   @Nonnull
   public static TriggerVolumeToolUngroup deserialize(@Nonnull ByteBuf buf, int offset) {
      TriggerVolumeToolUngroup obj = new TriggerVolumeToolUngroup();
      int pos = offset + 0;
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

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      int pos = offset + 0;
      int sl = VarInt.peek(buf, pos);
      pos += VarInt.size(sl) + sl;
      return pos - offset;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 0L;
   }

   public static String getGroupId(MemorySegment mem) {
      return getGroupId(mem, 0);
   }

   public static String getGroupId(MemorySegment mem, int offset) {
      return PacketIO.readVarString("GroupId", mem, offset + 0, 4096000, PacketIO.UTF8);
   }

   public static TriggerVolumeToolUngroup toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static TriggerVolumeToolUngroup toObject(MemorySegment mem, int offset) {
      if (offset + 0 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("TriggerVolumeToolUngroup", offset + 0, (int)mem.byteSize());
      } else {
         return new TriggerVolumeToolUngroup(PacketIO.readVarString("GroupId", mem, offset + 0, 4096000, PacketIO.UTF8));
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      PacketIO.writeVarString(buf, this.groupId, 4096000);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      int varOffset = offset + 0;
      varOffset += PacketIO.writeVarString(mem, varOffset, this.groupId, 4096000);
      return varOffset - offset;
   }

   @Override
   public int computeSize() {
      int size = 0;
      return size + PacketIO.stringSize(this.groupId);
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 0) {
         return ValidationResult.error("Buffer too small: expected at least 0 bytes");
      } else {
         int pos = offset + 0;
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

   public TriggerVolumeToolUngroup clone() {
      TriggerVolumeToolUngroup copy = new TriggerVolumeToolUngroup();
      copy.groupId = this.groupId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof TriggerVolumeToolUngroup other ? Objects.equals(this.groupId, other.groupId) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.groupId);
   }
}
