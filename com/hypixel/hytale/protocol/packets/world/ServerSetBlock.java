package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class ServerSetBlock implements Packet, ToClientPacket {
   public static final int PACKET_ID = 140;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 19;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 19;
   public static final int MAX_SIZE = 19;
   public int x;
   public int y;
   public int z;
   public int blockId;
   public short filler;
   public byte rotation;

   @Override
   public int getId() {
      return 140;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Chunks;
   }

   public ServerSetBlock() {
   }

   public ServerSetBlock(int x, int y, int z, int blockId, short filler, byte rotation) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.blockId = blockId;
      this.filler = filler;
      this.rotation = rotation;
   }

   public ServerSetBlock(@Nonnull ServerSetBlock other) {
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
      this.blockId = other.blockId;
      this.filler = other.filler;
      this.rotation = other.rotation;
   }

   @Nonnull
   public static ServerSetBlock deserialize(@Nonnull ByteBuf buf, int offset) {
      ServerSetBlock obj = new ServerSetBlock();
      obj.x = buf.getIntLE(offset + 0);
      obj.y = buf.getIntLE(offset + 4);
      obj.z = buf.getIntLE(offset + 8);
      obj.blockId = buf.getIntLE(offset + 12);
      obj.filler = buf.getShortLE(offset + 16);
      obj.rotation = buf.getByte(offset + 18);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 19;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.x);
      buf.writeIntLE(this.y);
      buf.writeIntLE(this.z);
      buf.writeIntLE(this.blockId);
      buf.writeShortLE(this.filler);
      buf.writeByte(this.rotation);
   }

   @Override
   public int computeSize() {
      return 19;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 19 ? ValidationResult.error("Buffer too small: expected at least 19 bytes") : ValidationResult.OK;
   }

   public ServerSetBlock clone() {
      ServerSetBlock copy = new ServerSetBlock();
      copy.x = this.x;
      copy.y = this.y;
      copy.z = this.z;
      copy.blockId = this.blockId;
      copy.filler = this.filler;
      copy.rotation = this.rotation;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ServerSetBlock other)
            ? false
            : this.x == other.x
               && this.y == other.y
               && this.z == other.z
               && this.blockId == other.blockId
               && this.filler == other.filler
               && this.rotation == other.rotation;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y, this.z, this.blockId, this.filler, this.rotation);
   }
}
