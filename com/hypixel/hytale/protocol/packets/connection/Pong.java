package com.hypixel.hytale.protocol.packets.connection;

import com.hypixel.hytale.protocol.InstantData;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Pong implements Packet, ToServerPacket {
   public static final int PACKET_ID = 4;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 20;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 20;
   public static final int MAX_SIZE = 20;
   public int id;
   @Nullable
   public InstantData time;
   @Nonnull
   public PongType type = PongType.Raw;
   public short packetQueueSize;

   @Override
   public int getId() {
      return 4;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public Pong() {
   }

   public Pong(int id, @Nullable InstantData time, @Nonnull PongType type, short packetQueueSize) {
      this.id = id;
      this.time = time;
      this.type = type;
      this.packetQueueSize = packetQueueSize;
   }

   public Pong(@Nonnull Pong other) {
      this.id = other.id;
      this.time = other.time;
      this.type = other.type;
      this.packetQueueSize = other.packetQueueSize;
   }

   @Nonnull
   public static Pong deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 20) {
         throw ProtocolException.bufferTooSmall("Pong", 20, buf.readableBytes() - offset);
      } else {
         Pong obj = new Pong();
         byte nullBits = buf.getByte(offset);
         obj.id = buf.getIntLE(offset + 1);
         if ((nullBits & 1) != 0) {
            obj.time = InstantData.deserialize(buf, offset + 5);
         }

         obj.type = PongType.fromValue(buf.getByte(offset + 17));
         obj.packetQueueSize = buf.getShortLE(offset + 18);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 20;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 20L;
   }

   public static int getId(MemorySegment mem) {
      return getId(mem, 0);
   }

   public static int getId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static InstantData getTime(MemorySegment mem) {
      return getTime(mem, 0);
   }

   @Nullable
   public static InstantData getTime(MemorySegment mem, int offset) {
      return hasTime(mem, offset) ? InstantData.toObject(mem, offset + 5) : null;
   }

   public static PongType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static PongType getType(MemorySegment mem, int offset) {
      return PongType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 17)));
   }

   public static short getPacketQueueSize(MemorySegment mem) {
      return getPacketQueueSize(mem, 0);
   }

   public static short getPacketQueueSize(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_SHORT, (long)(offset + 18));
   }

   public static boolean hasTime(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static Pong toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static Pong toObject(MemorySegment mem, int offset) {
      if (offset + 20 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("Pong", offset + 20, (int)mem.byteSize());
      } else {
         return new Pong(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            hasTime(mem, offset) ? InstantData.toObject(mem, offset + 5) : null,
            PongType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 17))),
            mem.get(PacketIO.PROTO_SHORT, (long)(offset + 18))
         );
      }
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.time != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.id);
      if (this.time != null) {
         this.time.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeByte(this.type.getValue());
      buf.writeShortLE(this.packetQueueSize);
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.time != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.id);
      if (this.time != null) {
         this.time.serialize(mem, offset + 5);
      } else {
         mem.asSlice(offset + 5, 12L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 17), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_SHORT, (long)(offset + 18), this.packetQueueSize);
      return 20;
   }

   @Override
   public int computeSize() {
      return 20;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 20) {
         return ValidationResult.error("Buffer too small: expected at least 20 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         int v = buffer.getByte(offset + 17) & 255;
         return v >= 3 ? ValidationResult.error("Invalid PongType value for Type") : ValidationResult.OK;
      }
   }

   public Pong clone() {
      Pong copy = new Pong();
      copy.id = this.id;
      copy.time = this.time != null ? this.time.clone() : null;
      copy.type = this.type;
      copy.packetQueueSize = this.packetQueueSize;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Pong other)
            ? false
            : this.id == other.id
               && Objects.equals(this.time, other.time)
               && Objects.equals(this.type, other.type)
               && this.packetQueueSize == other.packetQueueSize;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.time, this.type, this.packetQueueSize);
   }
}
