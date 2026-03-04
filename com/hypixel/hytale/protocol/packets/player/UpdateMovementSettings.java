package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.ToClientPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class UpdateMovementSettings implements Packet, ToClientPacket {
   public static final int PACKET_ID = 110;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 252;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 252;
   public static final int MAX_SIZE = 252;
   @Nullable
   public MovementSettings movementSettings;

   @Override
   public int getId() {
      return 110;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public UpdateMovementSettings() {
   }

   public UpdateMovementSettings(@Nullable MovementSettings movementSettings) {
      this.movementSettings = movementSettings;
   }

   public UpdateMovementSettings(@Nonnull UpdateMovementSettings other) {
      this.movementSettings = other.movementSettings;
   }

   @Nonnull
   public static UpdateMovementSettings deserialize(@Nonnull ByteBuf buf, int offset) {
      UpdateMovementSettings obj = new UpdateMovementSettings();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.movementSettings = MovementSettings.deserialize(buf, offset + 1);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 252;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.movementSettings != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      if (this.movementSettings != null) {
         this.movementSettings.serialize(buf);
      } else {
         buf.writeZero(251);
      }
   }

   @Override
   public int computeSize() {
      return 252;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 252 ? ValidationResult.error("Buffer too small: expected at least 252 bytes") : ValidationResult.OK;
   }

   public UpdateMovementSettings clone() {
      UpdateMovementSettings copy = new UpdateMovementSettings();
      copy.movementSettings = this.movementSettings != null ? this.movementSettings.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof UpdateMovementSettings other ? Objects.equals(this.movementSettings, other.movementSettings) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.movementSettings);
   }
}
