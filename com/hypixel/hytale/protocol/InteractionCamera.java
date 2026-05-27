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

public class InteractionCamera {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 29;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 29;
   public static final int MAX_SIZE = 29;
   public float time;
   @Nonnull
   public Vector3fc position = PacketIO.ZERO_VECTOR3;
   @Nullable
   public Direction rotation;

   public InteractionCamera() {
   }

   public InteractionCamera(float time, @Nonnull Vector3fc position, @Nullable Direction rotation) {
      this.time = time;
      this.position = position;
      this.rotation = rotation;
   }

   public InteractionCamera(@Nonnull InteractionCamera other) {
      this.time = other.time;
      this.position = other.position;
      this.rotation = other.rotation;
   }

   @Nonnull
   public static InteractionCamera deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 29) {
         throw ProtocolException.bufferTooSmall("InteractionCamera", 29, buf.readableBytes() - offset);
      } else {
         InteractionCamera obj = new InteractionCamera();
         byte nullBits = buf.getByte(offset);
         obj.time = buf.getFloatLE(offset + 1);
         obj.position = PacketIO.readVector3f(buf, offset + 5);
         if ((nullBits & 1) != 0) {
            obj.rotation = Direction.deserialize(buf, offset + 17);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 29;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 29L;
   }

   public static float getTime(MemorySegment mem) {
      return getTime(mem, 0);
   }

   public static float getTime(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static Vector3fc getPosition(MemorySegment mem) {
      return getPosition(mem, 0);
   }

   public static Vector3fc getPosition(MemorySegment mem, int offset) {
      return PacketIO.readVector3f(mem, offset + 5);
   }

   @Nullable
   public static Direction getRotation(MemorySegment mem) {
      return getRotation(mem, 0);
   }

   @Nullable
   public static Direction getRotation(MemorySegment mem, int offset) {
      return hasRotation(mem, offset) ? Direction.toObject(mem, offset + 17) : null;
   }

   public static boolean hasRotation(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static InteractionCamera toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static InteractionCamera toObject(MemorySegment mem, int offset) {
      if (offset + 29 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("InteractionCamera", offset + 29, (int)mem.byteSize());
      } else {
         return new InteractionCamera(
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            PacketIO.readVector3f(mem, offset + 5),
            hasRotation(mem, offset) ? Direction.toObject(mem, offset + 17) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.rotation != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeFloatLE(this.time);
      PacketIO.writeVector3f(buf, this.position);
      if (this.rotation != null) {
         this.rotation.serialize(buf);
      } else {
         buf.writeZero(12);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.rotation != null) {
         nullBits = (byte)(nullBits | 1);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.time);
      PacketIO.writeVector3f(mem, offset + 5, this.position);
      if (this.rotation != null) {
         this.rotation.serialize(mem, offset + 17);
      } else {
         mem.asSlice(offset + 17, 12L).fill((byte)0);
      }

      return 29;
   }

   public int computeSize() {
      return 29;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 29) {
         return ValidationResult.error("Buffer too small: expected at least 29 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public InteractionCamera clone() {
      InteractionCamera copy = new InteractionCamera();
      copy.time = this.time;
      copy.position = this.position;
      copy.rotation = this.rotation != null ? this.rotation.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof InteractionCamera other)
            ? false
            : this.time == other.time && Objects.equals(this.position, other.position) && Objects.equals(this.rotation, other.rotation);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.time, this.position, this.rotation);
   }
}
