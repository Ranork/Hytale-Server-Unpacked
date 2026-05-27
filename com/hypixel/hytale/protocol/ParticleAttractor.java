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

public class ParticleAttractor {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 85;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 85;
   public static final int MAX_SIZE = 85;
   @Nullable
   public Vector3fc position;
   @Nullable
   public Vector3fc radialAxis;
   public float trailPositionMultiplier;
   public float radius;
   public float radialAcceleration;
   public float radialTangentAcceleration;
   @Nullable
   public Vector3fc linearAcceleration;
   public float radialImpulse;
   public float radialTangentImpulse;
   @Nullable
   public Vector3fc linearImpulse;
   @Nullable
   public Vector3fc dampingMultiplier;

   public ParticleAttractor() {
   }

   public ParticleAttractor(
      @Nullable Vector3fc position,
      @Nullable Vector3fc radialAxis,
      float trailPositionMultiplier,
      float radius,
      float radialAcceleration,
      float radialTangentAcceleration,
      @Nullable Vector3fc linearAcceleration,
      float radialImpulse,
      float radialTangentImpulse,
      @Nullable Vector3fc linearImpulse,
      @Nullable Vector3fc dampingMultiplier
   ) {
      this.position = position;
      this.radialAxis = radialAxis;
      this.trailPositionMultiplier = trailPositionMultiplier;
      this.radius = radius;
      this.radialAcceleration = radialAcceleration;
      this.radialTangentAcceleration = radialTangentAcceleration;
      this.linearAcceleration = linearAcceleration;
      this.radialImpulse = radialImpulse;
      this.radialTangentImpulse = radialTangentImpulse;
      this.linearImpulse = linearImpulse;
      this.dampingMultiplier = dampingMultiplier;
   }

   public ParticleAttractor(@Nonnull ParticleAttractor other) {
      this.position = other.position;
      this.radialAxis = other.radialAxis;
      this.trailPositionMultiplier = other.trailPositionMultiplier;
      this.radius = other.radius;
      this.radialAcceleration = other.radialAcceleration;
      this.radialTangentAcceleration = other.radialTangentAcceleration;
      this.linearAcceleration = other.linearAcceleration;
      this.radialImpulse = other.radialImpulse;
      this.radialTangentImpulse = other.radialTangentImpulse;
      this.linearImpulse = other.linearImpulse;
      this.dampingMultiplier = other.dampingMultiplier;
   }

   @Nonnull
   public static ParticleAttractor deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 85) {
         throw ProtocolException.bufferTooSmall("ParticleAttractor", 85, buf.readableBytes() - offset);
      } else {
         ParticleAttractor obj = new ParticleAttractor();
         byte nullBits = buf.getByte(offset);
         if ((nullBits & 1) != 0) {
            obj.position = PacketIO.readVector3f(buf, offset + 1);
         }

         if ((nullBits & 2) != 0) {
            obj.radialAxis = PacketIO.readVector3f(buf, offset + 13);
         }

         obj.trailPositionMultiplier = buf.getFloatLE(offset + 25);
         obj.radius = buf.getFloatLE(offset + 29);
         obj.radialAcceleration = buf.getFloatLE(offset + 33);
         obj.radialTangentAcceleration = buf.getFloatLE(offset + 37);
         if ((nullBits & 4) != 0) {
            obj.linearAcceleration = PacketIO.readVector3f(buf, offset + 41);
         }

         obj.radialImpulse = buf.getFloatLE(offset + 53);
         obj.radialTangentImpulse = buf.getFloatLE(offset + 57);
         if ((nullBits & 8) != 0) {
            obj.linearImpulse = PacketIO.readVector3f(buf, offset + 61);
         }

         if ((nullBits & 16) != 0) {
            obj.dampingMultiplier = PacketIO.readVector3f(buf, offset + 73);
         }

         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 85;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 85L;
   }

   @Nullable
   public static Vector3fc getPosition(MemorySegment mem) {
      return getPosition(mem, 0);
   }

   @Nullable
   public static Vector3fc getPosition(MemorySegment mem, int offset) {
      return hasPosition(mem, offset) ? PacketIO.readVector3f(mem, offset + 1) : null;
   }

   @Nullable
   public static Vector3fc getRadialAxis(MemorySegment mem) {
      return getRadialAxis(mem, 0);
   }

   @Nullable
   public static Vector3fc getRadialAxis(MemorySegment mem, int offset) {
      return hasRadialAxis(mem, offset) ? PacketIO.readVector3f(mem, offset + 13) : null;
   }

   public static float getTrailPositionMultiplier(MemorySegment mem) {
      return getTrailPositionMultiplier(mem, 0);
   }

   public static float getTrailPositionMultiplier(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 25));
   }

   public static float getRadius(MemorySegment mem) {
      return getRadius(mem, 0);
   }

   public static float getRadius(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 29));
   }

   public static float getRadialAcceleration(MemorySegment mem) {
      return getRadialAcceleration(mem, 0);
   }

   public static float getRadialAcceleration(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 33));
   }

   public static float getRadialTangentAcceleration(MemorySegment mem) {
      return getRadialTangentAcceleration(mem, 0);
   }

   public static float getRadialTangentAcceleration(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 37));
   }

   @Nullable
   public static Vector3fc getLinearAcceleration(MemorySegment mem) {
      return getLinearAcceleration(mem, 0);
   }

   @Nullable
   public static Vector3fc getLinearAcceleration(MemorySegment mem, int offset) {
      return hasLinearAcceleration(mem, offset) ? PacketIO.readVector3f(mem, offset + 41) : null;
   }

   public static float getRadialImpulse(MemorySegment mem) {
      return getRadialImpulse(mem, 0);
   }

   public static float getRadialImpulse(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 53));
   }

   public static float getRadialTangentImpulse(MemorySegment mem) {
      return getRadialTangentImpulse(mem, 0);
   }

   public static float getRadialTangentImpulse(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 57));
   }

   @Nullable
   public static Vector3fc getLinearImpulse(MemorySegment mem) {
      return getLinearImpulse(mem, 0);
   }

   @Nullable
   public static Vector3fc getLinearImpulse(MemorySegment mem, int offset) {
      return hasLinearImpulse(mem, offset) ? PacketIO.readVector3f(mem, offset + 61) : null;
   }

   @Nullable
   public static Vector3fc getDampingMultiplier(MemorySegment mem) {
      return getDampingMultiplier(mem, 0);
   }

   @Nullable
   public static Vector3fc getDampingMultiplier(MemorySegment mem, int offset) {
      return hasDampingMultiplier(mem, offset) ? PacketIO.readVector3f(mem, offset + 73) : null;
   }

   public static boolean hasPosition(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 1) != 0;
   }

   public static boolean hasRadialAxis(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 2) != 0;
   }

   public static boolean hasLinearAcceleration(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 4) != 0;
   }

   public static boolean hasLinearImpulse(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 8) != 0;
   }

   public static boolean hasDampingMultiplier(MemorySegment mem, int offset) {
      byte b = mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0));
      return (b & 16) != 0;
   }

   public static ParticleAttractor toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static ParticleAttractor toObject(MemorySegment mem, int offset) {
      if (offset + 85 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("ParticleAttractor", offset + 85, (int)mem.byteSize());
      } else {
         return new ParticleAttractor(
            hasPosition(mem, offset) ? PacketIO.readVector3f(mem, offset + 1) : null,
            hasRadialAxis(mem, offset) ? PacketIO.readVector3f(mem, offset + 13) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 25)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 29)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 33)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 37)),
            hasLinearAcceleration(mem, offset) ? PacketIO.readVector3f(mem, offset + 41) : null,
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 53)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 57)),
            hasLinearImpulse(mem, offset) ? PacketIO.readVector3f(mem, offset + 61) : null,
            hasDampingMultiplier(mem, offset) ? PacketIO.readVector3f(mem, offset + 73) : null
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.position != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.radialAxis != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.linearAcceleration != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.linearImpulse != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.dampingMultiplier != null) {
         nullBits = (byte)(nullBits | 16);
      }

      buf.writeByte(nullBits);
      if (this.position != null) {
         PacketIO.writeVector3f(buf, this.position);
      } else {
         buf.writeZero(12);
      }

      if (this.radialAxis != null) {
         PacketIO.writeVector3f(buf, this.radialAxis);
      } else {
         buf.writeZero(12);
      }

      buf.writeFloatLE(this.trailPositionMultiplier);
      buf.writeFloatLE(this.radius);
      buf.writeFloatLE(this.radialAcceleration);
      buf.writeFloatLE(this.radialTangentAcceleration);
      if (this.linearAcceleration != null) {
         PacketIO.writeVector3f(buf, this.linearAcceleration);
      } else {
         buf.writeZero(12);
      }

      buf.writeFloatLE(this.radialImpulse);
      buf.writeFloatLE(this.radialTangentImpulse);
      if (this.linearImpulse != null) {
         PacketIO.writeVector3f(buf, this.linearImpulse);
      } else {
         buf.writeZero(12);
      }

      if (this.dampingMultiplier != null) {
         PacketIO.writeVector3f(buf, this.dampingMultiplier);
      } else {
         buf.writeZero(12);
      }
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      byte nullBits = 0;
      if (this.position != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.radialAxis != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.linearAcceleration != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.linearImpulse != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.dampingMultiplier != null) {
         nullBits = (byte)(nullBits | 16);
      }

      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), nullBits);
      if (this.position != null) {
         PacketIO.writeVector3f(mem, offset + 1, this.position);
      } else {
         mem.asSlice(offset + 1, 12L).fill((byte)0);
      }

      if (this.radialAxis != null) {
         PacketIO.writeVector3f(mem, offset + 13, this.radialAxis);
      } else {
         mem.asSlice(offset + 13, 12L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 25), this.trailPositionMultiplier);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 29), this.radius);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 33), this.radialAcceleration);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 37), this.radialTangentAcceleration);
      if (this.linearAcceleration != null) {
         PacketIO.writeVector3f(mem, offset + 41, this.linearAcceleration);
      } else {
         mem.asSlice(offset + 41, 12L).fill((byte)0);
      }

      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 53), this.radialImpulse);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 57), this.radialTangentImpulse);
      if (this.linearImpulse != null) {
         PacketIO.writeVector3f(mem, offset + 61, this.linearImpulse);
      } else {
         mem.asSlice(offset + 61, 12L).fill((byte)0);
      }

      if (this.dampingMultiplier != null) {
         PacketIO.writeVector3f(mem, offset + 73, this.dampingMultiplier);
      } else {
         mem.asSlice(offset + 73, 12L).fill((byte)0);
      }

      return 85;
   }

   public int computeSize() {
      return 85;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 85) {
         return ValidationResult.error("Buffer too small: expected at least 85 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         return ValidationResult.OK;
      }
   }

   public ParticleAttractor clone() {
      ParticleAttractor copy = new ParticleAttractor();
      copy.position = this.position;
      copy.radialAxis = this.radialAxis;
      copy.trailPositionMultiplier = this.trailPositionMultiplier;
      copy.radius = this.radius;
      copy.radialAcceleration = this.radialAcceleration;
      copy.radialTangentAcceleration = this.radialTangentAcceleration;
      copy.linearAcceleration = this.linearAcceleration;
      copy.radialImpulse = this.radialImpulse;
      copy.radialTangentImpulse = this.radialTangentImpulse;
      copy.linearImpulse = this.linearImpulse;
      copy.dampingMultiplier = this.dampingMultiplier;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ParticleAttractor other)
            ? false
            : Objects.equals(this.position, other.position)
               && Objects.equals(this.radialAxis, other.radialAxis)
               && this.trailPositionMultiplier == other.trailPositionMultiplier
               && this.radius == other.radius
               && this.radialAcceleration == other.radialAcceleration
               && this.radialTangentAcceleration == other.radialTangentAcceleration
               && Objects.equals(this.linearAcceleration, other.linearAcceleration)
               && this.radialImpulse == other.radialImpulse
               && this.radialTangentImpulse == other.radialTangentImpulse
               && Objects.equals(this.linearImpulse, other.linearImpulse)
               && Objects.equals(this.dampingMultiplier, other.dampingMultiplier);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.position,
         this.radialAxis,
         this.trailPositionMultiplier,
         this.radius,
         this.radialAcceleration,
         this.radialTangentAcceleration,
         this.linearAcceleration,
         this.radialImpulse,
         this.radialTangentImpulse,
         this.linearImpulse,
         this.dampingMultiplier
      );
   }
}
