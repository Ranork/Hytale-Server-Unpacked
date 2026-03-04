package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class ServerSetFluid implements Packet, ToClientPacket {
   public static final int PACKET_ID = 142;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 17;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 17;
   public static final int MAX_SIZE = 17;
   public int x;
   public int y;
   public int z;
   public int fluidId;
   public byte fluidLevel;

   @Override
   public int getId() {
      return 142;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Chunks;
   }

   public ServerSetFluid() {
   }

   public ServerSetFluid(int x, int y, int z, int fluidId, byte fluidLevel) {
      this.x = x;
      this.y = y;
      this.z = z;
      this.fluidId = fluidId;
      this.fluidLevel = fluidLevel;
   }

   public ServerSetFluid(@Nonnull ServerSetFluid other) {
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
      this.fluidId = other.fluidId;
      this.fluidLevel = other.fluidLevel;
   }

   @Nonnull
   public static ServerSetFluid deserialize(@Nonnull ByteBuf buf, int offset) {
      ServerSetFluid obj = new ServerSetFluid();
      obj.x = buf.getIntLE(offset + 0);
      obj.y = buf.getIntLE(offset + 4);
      obj.z = buf.getIntLE(offset + 8);
      obj.fluidId = buf.getIntLE(offset + 12);
      obj.fluidLevel = buf.getByte(offset + 16);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 17;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.x);
      buf.writeIntLE(this.y);
      buf.writeIntLE(this.z);
      buf.writeIntLE(this.fluidId);
      buf.writeByte(this.fluidLevel);
   }

   @Override
   public int computeSize() {
      return 17;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 17 ? ValidationResult.error("Buffer too small: expected at least 17 bytes") : ValidationResult.OK;
   }

   public ServerSetFluid clone() {
      ServerSetFluid copy = new ServerSetFluid();
      copy.x = this.x;
      copy.y = this.y;
      copy.z = this.z;
      copy.fluidId = this.fluidId;
      copy.fluidLevel = this.fluidLevel;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ServerSetFluid other)
            ? false
            : this.x == other.x && this.y == other.y && this.z == other.z && this.fluidId == other.fluidId && this.fluidLevel == other.fluidLevel;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y, this.z, this.fluidId, this.fluidLevel);
   }
}
