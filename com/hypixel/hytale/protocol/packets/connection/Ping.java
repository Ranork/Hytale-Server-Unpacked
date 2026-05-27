package com.hypixel.hytale.protocol.packets.connection;

import com.hypixel.hytale.protocol.InstantData;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Ping implements Packet, ToClientPacket {
   public static final int PACKET_ID = 3;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 29;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 29;
   public static final int MAX_SIZE = 29;
   public int id;
   @Nullable
   public InstantData time;
   public int lastPingValueRaw;
   public int lastPingValueDirect;
   public int lastPingValueTick;

   @Override
   public int getId() {
      return 3;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public Ping() {
   }

   public Ping(int id, @Nullable InstantData time, int lastPingValueRaw, int lastPingValueDirect, int lastPingValueTick) {
      this.id = id;
      this.time = time;
      this.lastPingValueRaw = lastPingValueRaw;
      this.lastPingValueDirect = lastPingValueDirect;
      this.lastPingValueTick = lastPingValueTick;
   }

   public Ping(@Nonnull Ping other) {
      this.id = other.id;
      this.time = other.time;
      this.lastPingValueRaw = other.lastPingValueRaw;
      this.lastPingValueDirect = other.lastPingValueDirect;
      this.lastPingValueTick = other.lastPingValueTick;
   }

   @Nonnull
   public static Ping deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 29) {
         throw ProtocolException.bufferTooSmall("Ping", 29, buf.readableBytes() - offset);
      } else {
         Ping obj = new Ping();
         byte nullBits = buf.getByte(offset);
         obj.id = buf.getIntLE(offset + 1);
         if ((nullBits & 1) != 0) {
            obj.time = InstantData.deserialize(buf, offset + 5);
         }

         obj.lastPingValueRaw = buf.getIntLE(offset + 17);
         obj.lastPingValueDirect = buf.getIntLE(offset + 21);
         obj.lastPingValueTick = buf.getIntLE(offset + 25);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 29;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 29L;
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

   public static int getLastPingValueRaw(MemorySegment mem) {
      return getLastPingValueRaw(mem, 0);
   }

   public static int getLastPingValueRaw(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 17));
   }

   public static int getLastPingValueDirect(MemorySegment mem) {
      return getLastPingValueDirect(mem, 0);
   }

   public static int getLastPingValueDirect(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 21));
   }

   public static int getLastPingValueTick(MemorySegment mem) {
      return getLastPingValueTick(mem, 0);
   }

   public static int getLastPingValueTick(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 25));
   }

   public static boolean hasTime(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static Ping toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static Ping toObject(MemorySegment mem, int offset) {
      if (offset + 29 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("Ping", offset + 29, (int)mem.byteSize());
      } else {
         return new Ping(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            hasTime(mem, offset) ? InstantData.toObject(mem, offset + 5) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 17)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 21)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 25))
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

      buf.writeIntLE(this.lastPingValueRaw);
      buf.writeIntLE(this.lastPingValueDirect);
      buf.writeIntLE(this.lastPingValueTick);
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

      mem.set(PacketIO.PROTO_INT, (long)(offset + 17), this.lastPingValueRaw);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 21), this.lastPingValueDirect);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 25), this.lastPingValueTick);
      return 29;
   }

   @Override
   public int computeSize() {
      return 29;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 29) {
         return ValidationResult.error("Buffer too small: expected at least 29 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public Ping clone() {
      Ping copy = new Ping();
      copy.id = this.id;
      copy.time = this.time != null ? this.time.clone() : null;
      copy.lastPingValueRaw = this.lastPingValueRaw;
      copy.lastPingValueDirect = this.lastPingValueDirect;
      copy.lastPingValueTick = this.lastPingValueTick;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Ping other)
            ? false
            : this.id == other.id
               && Objects.equals(this.time, other.time)
               && this.lastPingValueRaw == other.lastPingValueRaw
               && this.lastPingValueDirect == other.lastPingValueDirect
               && this.lastPingValueTick == other.lastPingValueTick;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.id, this.time, this.lastPingValueRaw, this.lastPingValueDirect, this.lastPingValueTick);
   }
}
