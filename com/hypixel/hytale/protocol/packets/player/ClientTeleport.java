package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.ModelTransform;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ClientTeleport implements Packet, ToClientPacket {
   public static final int PACKET_ID = 109;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 52;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 52;
   public static final int MAX_SIZE = 52;
   public byte teleportId;
   @Nullable
   public ModelTransform modelTransform;
   public boolean resetVelocity;

   @Override
   public int getId() {
      return 109;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ClientTeleport() {
   }

   public ClientTeleport(byte teleportId, @Nullable ModelTransform modelTransform, boolean resetVelocity) {
      this.teleportId = teleportId;
      this.modelTransform = modelTransform;
      this.resetVelocity = resetVelocity;
   }

   public ClientTeleport(@Nonnull ClientTeleport other) {
      this.teleportId = other.teleportId;
      this.modelTransform = other.modelTransform;
      this.resetVelocity = other.resetVelocity;
   }

   @Nonnull
   public static ClientTeleport deserialize(@Nonnull ByteBuf buf, int offset) {
      ClientTeleport obj = new ClientTeleport();
      byte nullBits = buf.getByte(offset);
      obj.teleportId = buf.getByte(offset + 1);
      if ((nullBits & 1) != 0) {
         obj.modelTransform = ModelTransform.deserialize(buf, offset + 2);
      }

      obj.resetVelocity = buf.getByte(offset + 51) != 0;
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 52;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.modelTransform != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.teleportId);
      if (this.modelTransform != null) {
         this.modelTransform.serialize(buf);
      } else {
         buf.writeZero(49);
      }

      buf.writeByte(this.resetVelocity ? 1 : 0);
   }

   @Override
   public int computeSize() {
      return 52;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 52 ? ValidationResult.error("Buffer too small: expected at least 52 bytes") : ValidationResult.OK;
   }

   public ClientTeleport clone() {
      ClientTeleport copy = new ClientTeleport();
      copy.teleportId = this.teleportId;
      copy.modelTransform = this.modelTransform != null ? this.modelTransform.clone() : null;
      copy.resetVelocity = this.resetVelocity;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ClientTeleport other)
            ? false
            : this.teleportId == other.teleportId && Objects.equals(this.modelTransform, other.modelTransform) && this.resetVelocity == other.resetVelocity;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.teleportId, this.modelTransform, this.resetVelocity);
   }
}
