package com.hypixel.hytale.protocol.packets.interaction;

import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class MountNPC implements Packet, ToClientPacket {
   public static final int PACKET_ID = 293;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 16;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 16;
   public static final int MAX_SIZE = 16;
   public float anchorX;
   public float anchorY;
   public float anchorZ;
   public int entityId;

   @Override
   public int getId() {
      return 293;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public MountNPC() {
   }

   public MountNPC(float anchorX, float anchorY, float anchorZ, int entityId) {
      this.anchorX = anchorX;
      this.anchorY = anchorY;
      this.anchorZ = anchorZ;
      this.entityId = entityId;
   }

   public MountNPC(@Nonnull MountNPC other) {
      this.anchorX = other.anchorX;
      this.anchorY = other.anchorY;
      this.anchorZ = other.anchorZ;
      this.entityId = other.entityId;
   }

   @Nonnull
   public static MountNPC deserialize(@Nonnull ByteBuf buf, int offset) {
      MountNPC obj = new MountNPC();
      obj.anchorX = buf.getFloatLE(offset + 0);
      obj.anchorY = buf.getFloatLE(offset + 4);
      obj.anchorZ = buf.getFloatLE(offset + 8);
      obj.entityId = buf.getIntLE(offset + 12);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 16;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.anchorX);
      buf.writeFloatLE(this.anchorY);
      buf.writeFloatLE(this.anchorZ);
      buf.writeIntLE(this.entityId);
   }

   @Override
   public int computeSize() {
      return 16;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 16 ? ValidationResult.error("Buffer too small: expected at least 16 bytes") : ValidationResult.OK;
   }

   public MountNPC clone() {
      MountNPC copy = new MountNPC();
      copy.anchorX = this.anchorX;
      copy.anchorY = this.anchorY;
      copy.anchorZ = this.anchorZ;
      copy.entityId = this.entityId;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof MountNPC other)
            ? false
            : this.anchorX == other.anchorX && this.anchorY == other.anchorY && this.anchorZ == other.anchorZ && this.entityId == other.entityId;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.anchorX, this.anchorY, this.anchorZ, this.entityId);
   }
}
