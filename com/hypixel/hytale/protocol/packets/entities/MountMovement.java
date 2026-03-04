package com.hypixel.hytale.protocol.packets.entities;

import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MountMovement implements Packet, ToServerPacket {
   public static final int PACKET_ID = 166;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 59;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 59;
   public static final int MAX_SIZE = 59;
   @Nullable
   public Position absolutePosition;
   @Nullable
   public Direction bodyOrientation;
   @Nullable
   public MovementStates movementStates;

   @Override
   public int getId() {
      return 166;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public MountMovement() {
   }

   public MountMovement(@Nullable Position absolutePosition, @Nullable Direction bodyOrientation, @Nullable MovementStates movementStates) {
      this.absolutePosition = absolutePosition;
      this.bodyOrientation = bodyOrientation;
      this.movementStates = movementStates;
   }

   public MountMovement(@Nonnull MountMovement other) {
      this.absolutePosition = other.absolutePosition;
      this.bodyOrientation = other.bodyOrientation;
      this.movementStates = other.movementStates;
   }

   @Nonnull
   public static MountMovement deserialize(@Nonnull ByteBuf buf, int offset) {
      MountMovement obj = new MountMovement();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.absolutePosition = Position.deserialize(buf, offset + 1);
      }

      if ((nullBits & 2) != 0) {
         obj.bodyOrientation = Direction.deserialize(buf, offset + 25);
      }

      if ((nullBits & 4) != 0) {
         obj.movementStates = MovementStates.deserialize(buf, offset + 37);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 59;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.absolutePosition != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.bodyOrientation != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.movementStates != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      if (this.absolutePosition != null) {
         this.absolutePosition.serialize(buf);
      } else {
         buf.writeZero(24);
      }

      if (this.bodyOrientation != null) {
         this.bodyOrientation.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.movementStates != null) {
         this.movementStates.serialize(buf);
      } else {
         buf.writeZero(22);
      }
   }

   @Override
   public int computeSize() {
      return 59;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 59 ? ValidationResult.error("Buffer too small: expected at least 59 bytes") : ValidationResult.OK;
   }

   public MountMovement clone() {
      MountMovement copy = new MountMovement();
      copy.absolutePosition = this.absolutePosition != null ? this.absolutePosition.clone() : null;
      copy.bodyOrientation = this.bodyOrientation != null ? this.bodyOrientation.clone() : null;
      copy.movementStates = this.movementStates != null ? this.movementStates.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof MountMovement other)
            ? false
            : Objects.equals(this.absolutePosition, other.absolutePosition)
               && Objects.equals(this.bodyOrientation, other.bodyOrientation)
               && Objects.equals(this.movementStates, other.movementStates);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.absolutePosition, this.bodyOrientation, this.movementStates);
   }
}
