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
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ClientMovement implements Packet, ToServerPacket {
   public static final int PACKET_ID = 108;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 2;
   public static final int FIXED_BLOCK_SIZE = 155;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 155;
   public static final int MAX_SIZE = 155;
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
      if (buf.readableBytes() - offset < 155) {
         throw ProtocolException.bufferTooSmall("ClientMovement", 155, buf.readableBytes() - offset);
      } else {
         ClientMovement obj = new ClientMovement();
         byte[] nullBits = PacketIO.readBytes(buf, offset, 2);
         if ((nullBits[0] & 1) != 0) {
            obj.movementStates = MovementStates.deserialize(buf, offset + 2);
         }

         if ((nullBits[0] & 2) != 0) {
            obj.relativePosition = HalfFloatPosition.deserialize(buf, offset + 25);
         }

         if ((nullBits[0] & 4) != 0) {
            obj.absolutePosition = Position.deserialize(buf, offset + 31);
         }

         if ((nullBits[0] & 8) != 0) {
            obj.bodyOrientation = Direction.deserialize(buf, offset + 55);
         }

         if ((nullBits[0] & 16) != 0) {
            obj.lookOrientation = Direction.deserialize(buf, offset + 67);
         }

         if ((nullBits[0] & 32) != 0) {
            obj.teleportAck = TeleportAck.deserialize(buf, offset + 79);
         }

         if ((nullBits[0] & 64) != 0) {
            obj.wishMovement = Position.deserialize(buf, offset + 80);
         }

         if ((nullBits[0] & 128) != 0) {
            obj.velocity = Vector3d.deserialize(buf, offset + 104);
         }

         obj.mountedTo = buf.getIntLE(offset + 128);
         if ((nullBits[1] & 1) != 0) {
            obj.riderMovementStates = MovementStates.deserialize(buf, offset + 132);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 155;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 155L;
   }

   @Nullable
   public static MovementStates getMovementStates(MemorySegment mem) {
      return getMovementStates(mem, 0);
   }

   @Nullable
   public static MovementStates getMovementStates(MemorySegment mem, int offset) {
      return hasMovementStates(mem, offset) ? MovementStates.toObject(mem, offset + 2) : null;
   }

   @Nullable
   public static HalfFloatPosition getRelativePosition(MemorySegment mem) {
      return getRelativePosition(mem, 0);
   }

   @Nullable
   public static HalfFloatPosition getRelativePosition(MemorySegment mem, int offset) {
      return hasRelativePosition(mem, offset) ? HalfFloatPosition.toObject(mem, offset + 25) : null;
   }

   @Nullable
   public static Position getAbsolutePosition(MemorySegment mem) {
      return getAbsolutePosition(mem, 0);
   }

   @Nullable
   public static Position getAbsolutePosition(MemorySegment mem, int offset) {
      return hasAbsolutePosition(mem, offset) ? Position.toObject(mem, offset + 31) : null;
   }

   @Nullable
   public static Direction getBodyOrientation(MemorySegment mem) {
      return getBodyOrientation(mem, 0);
   }

   @Nullable
   public static Direction getBodyOrientation(MemorySegment mem, int offset) {
      return hasBodyOrientation(mem, offset) ? Direction.toObject(mem, offset + 55) : null;
   }

   @Nullable
   public static Direction getLookOrientation(MemorySegment mem) {
      return getLookOrientation(mem, 0);
   }

   @Nullable
   public static Direction getLookOrientation(MemorySegment mem, int offset) {
      return hasLookOrientation(mem, offset) ? Direction.toObject(mem, offset + 67) : null;
   }

   @Nullable
   public static TeleportAck getTeleportAck(MemorySegment mem) {
      return getTeleportAck(mem, 0);
   }

   @Nullable
   public static TeleportAck getTeleportAck(MemorySegment mem, int offset) {
      return hasTeleportAck(mem, offset) ? TeleportAck.toObject(mem, offset + 79) : null;
   }

   @Nullable
   public static Position getWishMovement(MemorySegment mem) {
      return getWishMovement(mem, 0);
   }

   @Nullable
   public static Position getWishMovement(MemorySegment mem, int offset) {
      return hasWishMovement(mem, offset) ? Position.toObject(mem, offset + 80) : null;
   }

   @Nullable
   public static Vector3d getVelocity(MemorySegment mem) {
      return getVelocity(mem, 0);
   }

   @Nullable
   public static Vector3d getVelocity(MemorySegment mem, int offset) {
      return hasVelocity(mem, offset) ? Vector3d.toObject(mem, offset + 104) : null;
   }

   public static int getMountedTo(MemorySegment mem) {
      return getMountedTo(mem, 0);
   }

   public static int getMountedTo(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 128));
   }

   @Nullable
   public static MovementStates getRiderMovementStates(MemorySegment mem) {
      return getRiderMovementStates(mem, 0);
   }

   @Nullable
   public static MovementStates getRiderMovementStates(MemorySegment mem, int offset) {
      return hasRiderMovementStates(mem, offset) ? MovementStates.toObject(mem, offset + 132) : null;
   }

   public static boolean hasMovementStates(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasRelativePosition(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasAbsolutePosition(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasBodyOrientation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasLookOrientation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static boolean hasTeleportAck(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 32) != 0;
   }

   public static boolean hasWishMovement(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 64) != 0;
   }

   public static boolean hasVelocity(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 128) != 0;
   }

   public static boolean hasRiderMovementStates(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 1));
      return (b & 1) != 0;
   }

   public static ClientMovement toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ClientMovement toObject(MemorySegment mem, int offset) {
      if (offset + 155 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ClientMovement", offset + 155, (int)mem.byteSize());
      } else {
         return new ClientMovement(
            hasMovementStates(mem, offset) ? MovementStates.toObject(mem, offset + 2) : null,
            hasRelativePosition(mem, offset) ? HalfFloatPosition.toObject(mem, offset + 25) : null,
            hasAbsolutePosition(mem, offset) ? Position.toObject(mem, offset + 31) : null,
            hasBodyOrientation(mem, offset) ? Direction.toObject(mem, offset + 55) : null,
            hasLookOrientation(mem, offset) ? Direction.toObject(mem, offset + 67) : null,
            hasTeleportAck(mem, offset) ? TeleportAck.toObject(mem, offset + 79) : null,
            hasWishMovement(mem, offset) ? Position.toObject(mem, offset + 80) : null,
            hasVelocity(mem, offset) ? Vector3d.toObject(mem, offset + 104) : null,
            mem.get(PacketIO.PROTO_INT, (long)(offset + 128)),
            hasRiderMovementStates(mem, offset) ? MovementStates.toObject(mem, offset + 132) : null
         );
      }
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
         buf.writeZero(23);
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
         buf.writeZero(23);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.movementStates != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.relativePosition != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.absolutePosition != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.bodyOrientation != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.lookOrientation != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.teleportAck != null) {
         nullBits = (byte)(nullBits | 32);
      }

      if (this.wishMovement != null) {
         nullBits = (byte)(nullBits | 64);
      }

      if (this.velocity != null) {
         nullBits = (byte)(nullBits | 128);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      nullBits = 0;
      if (this.riderMovementStates != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 1), nullBits);
      if (this.movementStates != null) {
         this.movementStates.serialize(mem, offset + 2);
      } else {
         mem.asSlice(offset + 2, 23L).fill((byte)0);
      }

      if (this.relativePosition != null) {
         this.relativePosition.serialize(mem, offset + 25);
      } else {
         mem.asSlice(offset + 25, 6L).fill((byte)0);
      }

      if (this.absolutePosition != null) {
         this.absolutePosition.serialize(mem, offset + 31);
      } else {
         mem.asSlice(offset + 31, 24L).fill((byte)0);
      }

      if (this.bodyOrientation != null) {
         this.bodyOrientation.serialize(mem, offset + 55);
      } else {
         mem.asSlice(offset + 55, 12L).fill((byte)0);
      }

      if (this.lookOrientation != null) {
         this.lookOrientation.serialize(mem, offset + 67);
      } else {
         mem.asSlice(offset + 67, 12L).fill((byte)0);
      }

      if (this.teleportAck != null) {
         this.teleportAck.serialize(mem, offset + 79);
      } else {
         mem.asSlice(offset + 79, 1L).fill((byte)0);
      }

      if (this.wishMovement != null) {
         this.wishMovement.serialize(mem, offset + 80);
      } else {
         mem.asSlice(offset + 80, 24L).fill((byte)0);
      }

      if (this.velocity != null) {
         this.velocity.serialize(mem, offset + 104);
      } else {
         mem.asSlice(offset + 104, 24L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_INT, (long)(offset + 128), this.mountedTo);
      if (this.riderMovementStates != null) {
         this.riderMovementStates.serialize(mem, offset + 132);
      } else {
         mem.asSlice(offset + 132, 23L).fill((byte)0);
      }

      return 155;
   }

   @Override
   public int computeSize() {
      return 155;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 155) {
         return ValidationResult.error("Buffer too small: expected at least 155 bytes");
      } else {
         byte[] nullBits = PacketIO.readBytes(buffer, offset, 2);
         return ValidationResult.OK;
      }
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
