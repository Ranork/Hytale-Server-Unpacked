package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.joml.Vector3fc;

public class SelectedHitEntity {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 53;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 53;
   public static final int MAX_SIZE = 53;
   public int networkId;
   @Nullable
   public Vector3fc hitLocation;
   @Nullable
   public Position position;
   @Nullable
   public Direction bodyRotation;

   public SelectedHitEntity() {
   }

   public SelectedHitEntity(int networkId, @Nullable Vector3fc hitLocation, @Nullable Position position, @Nullable Direction bodyRotation) {
      this.networkId = networkId;
      this.hitLocation = hitLocation;
      this.position = position;
      this.bodyRotation = bodyRotation;
   }

   public SelectedHitEntity(@Nonnull SelectedHitEntity other) {
      this.networkId = other.networkId;
      this.hitLocation = other.hitLocation;
      this.position = other.position;
      this.bodyRotation = other.bodyRotation;
   }

   @Nonnull
   public static SelectedHitEntity deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 53) {
         throw ProtocolException.bufferTooSmall("SelectedHitEntity", 53, buf.readableBytes() - offset);
      } else {
         SelectedHitEntity obj = new SelectedHitEntity();
         byte nullBits = buf.getByte(offset);
         obj.networkId = buf.getIntLE(offset + 1);
         if ((nullBits & 1) != 0) {
            obj.hitLocation = PacketIO.readVector3f(buf, offset + 5);
         }

         if ((nullBits & 2) != 0) {
            obj.position = Position.deserialize(buf, offset + 17);
         }

         if ((nullBits & 4) != 0) {
            obj.bodyRotation = Direction.deserialize(buf, offset + 41);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 53;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 53L;
   }

   public static int getNetworkId(MemorySegment mem) {
      return getNetworkId(mem, 0);
   }

   public static int getNetworkId(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 1));
   }

   @Nullable
   public static Vector3fc getHitLocation(MemorySegment mem) {
      return getHitLocation(mem, 0);
   }

   @Nullable
   public static Vector3fc getHitLocation(MemorySegment mem, int offset) {
      return hasHitLocation(mem, offset) ? PacketIO.readVector3f(mem, offset + 5) : null;
   }

   @Nullable
   public static Position getPosition(MemorySegment mem) {
      return getPosition(mem, 0);
   }

   @Nullable
   public static Position getPosition(MemorySegment mem, int offset) {
      return hasPosition(mem, offset) ? Position.toObject(mem, offset + 17) : null;
   }

   @Nullable
   public static Direction getBodyRotation(MemorySegment mem) {
      return getBodyRotation(mem, 0);
   }

   @Nullable
   public static Direction getBodyRotation(MemorySegment mem, int offset) {
      return hasBodyRotation(mem, offset) ? Direction.toObject(mem, offset + 41) : null;
   }

   public static boolean hasHitLocation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasPosition(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasBodyRotation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static SelectedHitEntity toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static SelectedHitEntity toObject(MemorySegment mem, int offset) {
      if (offset + 53 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("SelectedHitEntity", offset + 53, (int)mem.byteSize());
      } else {
         return new SelectedHitEntity(
            mem.get(PacketIO.PROTO_INT, (long)(offset + 1)),
            hasHitLocation(mem, offset) ? PacketIO.readVector3f(mem, offset + 5) : null,
            hasPosition(mem, offset) ? Position.toObject(mem, offset + 17) : null,
            hasBodyRotation(mem, offset) ? Direction.toObject(mem, offset + 41) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.hitLocation != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.position != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.bodyRotation != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.networkId);
      if (this.hitLocation != null) {
         PacketIO.writeVector3f(buf, this.hitLocation);
      } else {
         buf.writeZero(12);
      }

      if (this.position != null) {
         this.position.serialize(buf);
      } else {
         buf.writeZero(24);
      }

      if (this.bodyRotation != null) {
         this.bodyRotation.serialize(buf);
      } else {
         buf.writeZero(12);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.hitLocation != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.position != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.bodyRotation != null) {
         nullBits = (byte)(nullBits | 4);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 1), this.networkId);
      if (this.hitLocation != null) {
         PacketIO.writeVector3f(mem, offset + 5, this.hitLocation);
      } else {
         mem.asSlice(offset + 5, 12L).fill((byte)0);
      }

      if (this.position != null) {
         this.position.serialize(mem, offset + 17);
      } else {
         mem.asSlice(offset + 17, 24L).fill((byte)0);
      }

      if (this.bodyRotation != null) {
         this.bodyRotation.serialize(mem, offset + 41);
      } else {
         mem.asSlice(offset + 41, 12L).fill((byte)0);
      }

      return 53;
   }

   public int computeSize() {
      return 53;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 53) {
         return ValidationResult.error("Buffer too small: expected at least 53 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public SelectedHitEntity clone() {
      SelectedHitEntity copy = new SelectedHitEntity();
      copy.networkId = this.networkId;
      copy.hitLocation = this.hitLocation;
      copy.position = this.position != null ? this.position.clone() : null;
      copy.bodyRotation = this.bodyRotation != null ? this.bodyRotation.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SelectedHitEntity other)
            ? false
            : this.networkId == other.networkId
               && Objects.equals(this.hitLocation, other.hitLocation)
               && Objects.equals(this.position, other.position)
               && Objects.equals(this.bodyRotation, other.bodyRotation);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.networkId, this.hitLocation, this.position, this.bodyRotation);
   }
}
