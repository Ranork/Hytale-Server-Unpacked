package com.hypixel.hytale.protocol.packets.buildertools;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BuilderToolLaserPointer implements Packet, ToClientPacket {
   public static final int PACKET_ID = 419;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 36;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 36;
   public static final int MAX_SIZE = 36;
   public int playerNetworkId;
   public float startX;
   public float startY;
   public float startZ;
   public float endX;
   public float endY;
   public float endZ;
   public int color;
   public int durationMs;

   @Override
   public int getId() {
      return 419;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public BuilderToolLaserPointer() {
   }

   public BuilderToolLaserPointer(int playerNetworkId, float startX, float startY, float startZ, float endX, float endY, float endZ, int color, int durationMs) {
      this.playerNetworkId = playerNetworkId;
      this.startX = startX;
      this.startY = startY;
      this.startZ = startZ;
      this.endX = endX;
      this.endY = endY;
      this.endZ = endZ;
      this.color = color;
      this.durationMs = durationMs;
   }

   public BuilderToolLaserPointer(@Nonnull BuilderToolLaserPointer other) {
      this.playerNetworkId = other.playerNetworkId;
      this.startX = other.startX;
      this.startY = other.startY;
      this.startZ = other.startZ;
      this.endX = other.endX;
      this.endY = other.endY;
      this.endZ = other.endZ;
      this.color = other.color;
      this.durationMs = other.durationMs;
   }

   @Nonnull
   public static BuilderToolLaserPointer deserialize(@Nonnull ByteBuf buf, int offset) {
      BuilderToolLaserPointer obj = new BuilderToolLaserPointer();
      obj.playerNetworkId = buf.getIntLE(offset + 0);
      obj.startX = buf.getFloatLE(offset + 4);
      obj.startY = buf.getFloatLE(offset + 8);
      obj.startZ = buf.getFloatLE(offset + 12);
      obj.endX = buf.getFloatLE(offset + 16);
      obj.endY = buf.getFloatLE(offset + 20);
      obj.endZ = buf.getFloatLE(offset + 24);
      obj.color = buf.getIntLE(offset + 28);
      obj.durationMs = buf.getIntLE(offset + 32);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 36;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.playerNetworkId);
      buf.writeFloatLE(this.startX);
      buf.writeFloatLE(this.startY);
      buf.writeFloatLE(this.startZ);
      buf.writeFloatLE(this.endX);
      buf.writeFloatLE(this.endY);
      buf.writeFloatLE(this.endZ);
      buf.writeIntLE(this.color);
      buf.writeIntLE(this.durationMs);
   }

   @Override
   public int computeSize() {
      return 36;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 36 ? ValidationResult.error("Buffer too small: expected at least 36 bytes") : ValidationResult.OK;
   }

   public BuilderToolLaserPointer clone() {
      BuilderToolLaserPointer copy = new BuilderToolLaserPointer();
      copy.playerNetworkId = this.playerNetworkId;
      copy.startX = this.startX;
      copy.startY = this.startY;
      copy.startZ = this.startZ;
      copy.endX = this.endX;
      copy.endY = this.endY;
      copy.endZ = this.endZ;
      copy.color = this.color;
      copy.durationMs = this.durationMs;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BuilderToolLaserPointer other)
            ? false
            : this.playerNetworkId == other.playerNetworkId
               && this.startX == other.startX
               && this.startY == other.startY
               && this.startZ == other.startZ
               && this.endX == other.endX
               && this.endY == other.endY
               && this.endZ == other.endZ
               && this.color == other.color
               && this.durationMs == other.durationMs;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.playerNetworkId, this.startX, this.startY, this.startZ, this.endX, this.endY, this.endZ, this.color, this.durationMs);
   }
}
