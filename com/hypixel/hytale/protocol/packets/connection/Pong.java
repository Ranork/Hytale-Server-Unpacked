package com.hypixel.hytale.protocol.packets.connection;

import com.hypixel.hytale.protocol.InstantData;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class Pong implements Packet, ToServerPacket {
   public static final int PACKET_ID = 3;
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
      return 3;
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

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 20;
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
   public int computeSize() {
      return 20;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 20 ? ValidationResult.error("Buffer too small: expected at least 20 bytes") : ValidationResult.OK;
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
