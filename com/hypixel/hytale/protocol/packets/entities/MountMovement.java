package com.hypixel.hytale.protocol.packets.entities;

import com.hypixel.hytale.protocol.Direction;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.protocol.NetworkChannel;
import com.hypixel.hytale.protocol.Packet;
import com.hypixel.hytale.protocol.Position;
import com.hypixel.hytale.protocol.ToServerPacket;
import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MountMovement implements Packet, ToServerPacket {
   public static final int PACKET_ID = 166;
   public static final boolean IS_COMPRESSED = false;
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 60;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 60;
   public static final int MAX_SIZE = 60;
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
      if (buf.readableBytes() - offset < 60) {
         throw ProtocolException.bufferTooSmall("MountMovement", 60, buf.readableBytes() - offset);
      } else {
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
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 60;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 60L;
   }

   @Nullable
   public static Position getAbsolutePosition(MemorySegment mem) {
      return getAbsolutePosition(mem, 0);
   }

   @Nullable
   public static Position getAbsolutePosition(MemorySegment mem, int offset) {
      return hasAbsolutePosition(mem, offset) ? Position.toObject(mem, offset + 1) : null;
   }

   @Nullable
   public static Direction getBodyOrientation(MemorySegment mem) {
      return getBodyOrientation(mem, 0);
   }

   @Nullable
   public static Direction getBodyOrientation(MemorySegment mem, int offset) {
      return hasBodyOrientation(mem, offset) ? Direction.toObject(mem, offset + 25) : null;
   }

   @Nullable
   public static MovementStates getMovementStates(MemorySegment mem) {
      return getMovementStates(mem, 0);
   }

   @Nullable
   public static MovementStates getMovementStates(MemorySegment mem, int offset) {
      return hasMovementStates(mem, offset) ? MovementStates.toObject(mem, offset + 37) : null;
   }

   public static boolean hasAbsolutePosition(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasBodyOrientation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasMovementStates(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static MountMovement toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static MountMovement toObject(MemorySegment mem, int offset) {
      if (offset + 60 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("MountMovement", offset + 60, (int)mem.byteSize());
      } else {
         return new MountMovement(
            hasAbsolutePosition(mem, offset) ? Position.toObject(mem, offset + 1) : null,
            hasBodyOrientation(mem, offset) ? Direction.toObject(mem, offset + 25) : null,
            hasMovementStates(mem, offset) ? MovementStates.toObject(mem, offset + 37) : null
         );
      }
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
         buf.writeZero(23);
      }
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
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

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.absolutePosition != null) {
         this.absolutePosition.serialize(mem, offset + 1);
      } else {
         mem.asSlice(offset + 1, 24L).fill((byte)0);
      }

      if (this.bodyOrientation != null) {
         this.bodyOrientation.serialize(mem, offset + 25);
      } else {
         mem.asSlice(offset + 25, 12L).fill((byte)0);
      }

      if (this.movementStates != null) {
         this.movementStates.serialize(mem, offset + 37);
      } else {
         mem.asSlice(offset + 37, 23L).fill((byte)0);
      }

      return 60;
   }

   @Override
   public int computeSize() {
      return 60;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 60) {
         return ValidationResult.error("Buffer too small: expected at least 60 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
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
