package com.hypixel.hytale.protocol.packets.player;

import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.HalfFloatPosition;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.TeleportAck;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.Vector3d;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ClientMovement implements Packet, ToServerPacket {
   public static final int PACKET_ID = 108;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 2;
   public static final int FIXED_BLOCK_SIZE = 153;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 153;
   public static final int MAX_SIZE = 153;
   @Nullable
   public MovementStates movementStates;
   @Nullable
   public HalfFloatPosition relativePosition;
   @Nullable
   public Position absolutePosition;
   @Nullable
   public Direction bodyOrientation;
   @Nullable
   public Direction lookOrientation;
   @Nullable
   public TeleportAck teleportAck;
   @Nullable
   public Position wishMovement;
   @Nullable
   public Vector3d velocity;
   public int mountedTo;
   @Nullable
   public MovementStates riderMovementStates;

   @Override
   public int getId() {
      return 108;
   }

   @Override
   public NetworkChannel getChannel() {
      return NetworkChannel.Default;
   }

   public ClientMovement() {
   }

   public ClientMovement(
      @Nullable MovementStates movementStates,
      @Nullable HalfFloatPosition relativePosition,
      @Nullable Position absolutePosition,
      @Nullable Direction bodyOrientation,
      @Nullable Direction lookOrientation,
      @Nullable TeleportAck teleportAck,
      @Nullable Position wishMovement,
      @Nullable Vector3d velocity,
      int mountedTo,
      @Nullable MovementStates riderMovementStates
   ) {
      this.movementStates = movementStates;
      this.relativePosition = relativePosition;
      this.absolutePosition = absolutePosition;
      this.bodyOrientation = bodyOrientation;
      this.lookOrientation = lookOrientation;
      this.teleportAck = teleportAck;
      this.wishMovement = wishMovement;
      this.velocity = velocity;
      this.mountedTo = mountedTo;
      this.riderMovementStates = riderMovementStates;
   }

   public ClientMovement(@Nonnull ClientMovement other) {
      this.movementStates = other.movementStates;
      this.relativePosition = other.relativePosition;
      this.absolutePosition = other.absolutePosition;
      this.bodyOrientation = other.bodyOrientation;
      this.lookOrientation = other.lookOrientation;
      this.teleportAck = other.teleportAck;
      this.wishMovement = other.wishMovement;
      this.velocity = other.velocity;
      this.mountedTo = other.mountedTo;
      this.riderMovementStates = other.riderMovementStates;
   }

   @Nonnull
   public static ClientMovement deserialize(@Nonnull ByteBuf buf, int offset) {
      ClientMovement obj = new ClientMovement();
      byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
      if ((nullBits[0] & 1) != 0) {
         obj.movementStates = MovementStates.deserialize(buf, offset + 2);
      }

      if ((nullBits[0] & 2) != 0) {
         obj.relativePosition = HalfFloatPosition.deserialize(buf, offset + 24);
      }

      if ((nullBits[0] & 4) != 0) {
         obj.absolutePosition = Position.deserialize(buf, offset + 30);
      }

      if ((nullBits[0] & 8) != 0) {
         obj.bodyOrientation = Direction.deserialize(buf, offset + 54);
      }

      if ((nullBits[0] & 16) != 0) {
         obj.lookOrientation = Direction.deserialize(buf, offset + 66);
      }

      if ((nullBits[0] & 32) != 0) {
         obj.teleportAck = TeleportAck.deserialize(buf, offset + 78);
      }

      if ((nullBits[0] & 64) != 0) {
         obj.wishMovement = Position.deserialize(buf, offset + 79);
      }

      if ((nullBits[0] & 128) != 0) {
         obj.velocity = Vector3d.deserialize(buf, offset + 103);
      }

      obj.mountedTo = buf.getIntLE(offset + 127);
      if ((nullBits[1] & 1) != 0) {
         obj.riderMovementStates = MovementStates.deserialize(buf, offset + 131);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 153;
   }

   @Override
   public void serialize(@Nonnull ByteBuf buf) {
      byte[] nullBits = new byte[2];
      if (this.movementStates != null) {
         nullBits[0] = (byte)(nullBits[0] | 1);
      }

      if (this.relativePosition != null) {
         nullBits[0] = (byte)(nullBits[0] | 2);
      }

      if (this.absolutePosition != null) {
         nullBits[0] = (byte)(nullBits[0] | 4);
      }

      if (this.bodyOrientation != null) {
         nullBits[0] = (byte)(nullBits[0] | 8);
      }

      if (this.lookOrientation != null) {
         nullBits[0] = (byte)(nullBits[0] | 16);
      }

      if (this.teleportAck != null) {
         nullBits[0] = (byte)(nullBits[0] | 32);
      }

      if (this.wishMovement != null) {
         nullBits[0] = (byte)(nullBits[0] | 64);
      }

      if (this.velocity != null) {
         nullBits[0] = (byte)(nullBits[0] | 128);
      }

      if (this.riderMovementStates != null) {
         nullBits[1] = (byte)(nullBits[1] | 1);
      }

      buf.writeBytes(nullBits);
      if (this.movementStates != null) {
         this.movementStates.serialize(buf);
      } else {
         buf.writeZero(22);
      }

      if (this.relativePosition != null) {
         this.relativePosition.serialize(buf);
      } else {
         buf.writeZero(6);
      }

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

      if (this.lookOrientation != null) {
         this.lookOrientation.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.teleportAck != null) {
         this.teleportAck.serialize(buf);
      } else {
         buf.writeZero(1);
      }

      if (this.wishMovement != null) {
         this.wishMovement.serialize(buf);
      } else {
         buf.writeZero(24);
      }

      if (this.velocity != null) {
         this.velocity.serialize(buf);
      } else {
         buf.writeZero(24);
      }

      buf.writeIntLE(this.mountedTo);
      if (this.riderMovementStates != null) {
         this.riderMovementStates.serialize(buf);
      } else {
         buf.writeZero(22);
      }
   }

   @Override
   public int computeSize() {
      return 153;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 153 ? ValidationResult.error("Buffer too small: expected at least 153 bytes") : ValidationResult.OK;
   }

   public ClientMovement clone() {
      ClientMovement copy = new ClientMovement();
      copy.movementStates = this.movementStates != null ? this.movementStates.clone() : null;
      copy.relativePosition = this.relativePosition != null ? this.relativePosition.clone() : null;
      copy.absolutePosition = this.absolutePosition != null ? this.absolutePosition.clone() : null;
      copy.bodyOrientation = this.bodyOrientation != null ? this.bodyOrientation.clone() : null;
      copy.lookOrientation = this.lookOrientation != null ? this.lookOrientation.clone() : null;
      copy.teleportAck = this.teleportAck != null ? this.teleportAck.clone() : null;
      copy.wishMovement = this.wishMovement != null ? this.wishMovement.clone() : null;
      copy.velocity = this.velocity != null ? this.velocity.clone() : null;
      copy.mountedTo = this.mountedTo;
      copy.riderMovementStates = this.riderMovementStates != null ? this.riderMovementStates.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ClientMovement other)
            ? false
            : Objects.equals(this.movementStates, other.movementStates)
               && Objects.equals(this.relativePosition, other.relativePosition)
               && Objects.equals(this.absolutePosition, other.absolutePosition)
               && Objects.equals(this.bodyOrientation, other.bodyOrientation)
               && Objects.equals(this.lookOrientation, other.lookOrientation)
               && Objects.equals(this.teleportAck, other.teleportAck)
               && Objects.equals(this.wishMovement, other.wishMovement)
               && Objects.equals(this.velocity, other.velocity)
               && this.mountedTo == other.mountedTo
               && Objects.equals(this.riderMovementStates, other.riderMovementStates);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.movementStates,
         this.relativePosition,
         this.absolutePosition,
         this.bodyOrientation,
         this.lookOrientation,
         this.teleportAck,
         this.wishMovement,
         this.velocity,
         this.mountedTo,
         this.riderMovementStates
      );
   }
}
