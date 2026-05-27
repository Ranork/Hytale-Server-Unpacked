package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InitialVelocity {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 25;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 25;
   public static final int MAX_SIZE = 25;
   @Nullable
   public Rangef yaw;
   @Nullable
   public Rangef pitch;
   @Nullable
   public Rangef speed;

   public InitialVelocity() {
   }

   public InitialVelocity(@Nullable Rangef yaw, @Nullable Rangef pitch, @Nullable Rangef speed) {
      this.yaw = yaw;
      this.pitch = pitch;
      this.speed = speed;
   }

   public InitialVelocity(@Nonnull InitialVelocity other) {
      this.yaw = other.yaw;
      this.pitch = other.pitch;
      this.speed = other.speed;
   }

   @Nonnull
   public static InitialVelocity deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 25) {
         throw ProtocolException.bufferTooSmall("InitialVelocity", 25, buf.readableBytes() - offset);
      } else {
         InitialVelocity obj = new InitialVelocity();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.yaw = Rangef.deserialize(buf, offset + 1);
         }

         if ((nullBits & 2) != 0) {
            obj.pitch = Rangef.deserialize(buf, offset + 9);
         }

         if ((nullBits & 4) != 0) {
            obj.speed = Rangef.deserialize(buf, offset + 17);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 25;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 25L;
   }

   @Nullable
   public static Rangef getYaw(MemorySegment mem) {
      return getYaw(mem, 0);
   }

   @Nullable
   public static Rangef getYaw(MemorySegment mem, int offset) {
      return hasYaw(mem, offset) ? Rangef.toObject(mem, offset + 1) : null;
   }

   @Nullable
   public static Rangef getPitch(MemorySegment mem) {
      return getPitch(mem, 0);
   }

   @Nullable
   public static Rangef getPitch(MemorySegment mem, int offset) {
      return hasPitch(mem, offset) ? Rangef.toObject(mem, offset + 9) : null;
   }

   @Nullable
   public static Rangef getSpeed(MemorySegment mem) {
      return getSpeed(mem, 0);
   }

   @Nullable
   public static Rangef getSpeed(MemorySegment mem, int offset) {
      return hasSpeed(mem, offset) ? Rangef.toObject(mem, offset + 17) : null;
   }

   public static boolean hasYaw(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasPitch(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasSpeed(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static InitialVelocity toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static InitialVelocity toObject(MemorySegment mem, int offset) {
      if (offset + 25 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("InitialVelocity", offset + 25, (int)mem.byteSize());
      } else {
         return new InitialVelocity(
            hasYaw(mem, offset) ? Rangef.toObject(mem, offset + 1) : null,
            hasPitch(mem, offset) ? Rangef.toObject(mem, offset + 9) : null,
            hasSpeed(mem, offset) ? Rangef.toObject(mem, offset + 17) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.yaw != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.pitch != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.speed != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      if (this.yaw != null) {
         this.yaw.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.pitch != null) {
         this.pitch.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.speed != null) {
         this.speed.serialize(buf);
      } else {
         buf.writeZero(8);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.yaw != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.pitch != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.speed != null) {
         nullBits = (byte)(nullBits | 4);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.yaw != null) {
         this.yaw.serialize(mem, offset + 1);
      } else {
         mem.asSlice(offset + 1, 8L).fill((byte)0);
      }

      if (this.pitch != null) {
         this.pitch.serialize(mem, offset + 9);
      } else {
         mem.asSlice(offset + 9, 8L).fill((byte)0);
      }

      if (this.speed != null) {
         this.speed.serialize(mem, offset + 17);
      } else {
         mem.asSlice(offset + 17, 8L).fill((byte)0);
      }

      return 25;
   }

   public int computeSize() {
      return 25;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 25) {
         return ValidationResult.error("Buffer too small: expected at least 25 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public InitialVelocity clone() {
      InitialVelocity copy = new InitialVelocity();
      copy.yaw = this.yaw != null ? this.yaw.clone() : null;
      copy.pitch = this.pitch != null ? this.pitch.clone() : null;
      copy.speed = this.speed != null ? this.speed.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof InitialVelocity other)
            ? false
            : Objects.equals(this.yaw, other.yaw) && Objects.equals(this.pitch, other.pitch) && Objects.equals(this.speed, other.speed);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.yaw, this.pitch, this.speed);
   }
}
