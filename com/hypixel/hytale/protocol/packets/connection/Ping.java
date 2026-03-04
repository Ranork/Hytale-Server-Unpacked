package com.hypixel.hytale.protocol.packets.connection;

import com.hypixel.hytale.protocol.InstantData;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Ping implements Packet, ToClientPacket {
   public static final int PACKET_ID = 2;
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
      return 2;
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

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 29;
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
   public int computeSize() {
      return 29;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 29 ? ValidationResult.error("Buffer too small: expected at least 29 bytes") : ValidationResult.OK;
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
