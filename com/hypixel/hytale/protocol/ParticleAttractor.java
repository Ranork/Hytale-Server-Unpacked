package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ParticleAttractor {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 85;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 85;
   public static final int MAX_SIZE = 85;
   @Nullable
   public Vector3f position;
   @Nullable
   public Vector3f radialAxis;
   public float trailPositionMultiplier;
   public float radius;
   public float radialAcceleration;
   public float radialTangentAcceleration;
   @Nullable
   public Vector3f linearAcceleration;
   public float radialImpulse;
   public float radialTangentImpulse;
   @Nullable
   public Vector3f linearImpulse;
   @Nullable
   public Vector3f dampingMultiplier;

   public ParticleAttractor() {
   }

   public ParticleAttractor(
      @Nullable Vector3f position,
      @Nullable Vector3f radialAxis,
      float trailPositionMultiplier,
      float radius,
      float radialAcceleration,
      float radialTangentAcceleration,
      @Nullable Vector3f linearAcceleration,
      float radialImpulse,
      float radialTangentImpulse,
      @Nullable Vector3f linearImpulse,
      @Nullable Vector3f dampingMultiplier
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
      ParticleAttractor obj = new ParticleAttractor();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.position = Vector3f.deserialize(buf, offset + 1);
      }

      if ((nullBits & 2) != 0) {
         obj.radialAxis = Vector3f.deserialize(buf, offset + 13);
      }

      obj.trailPositionMultiplier = buf.getFloatLE(offset + 25);
      obj.radius = buf.getFloatLE(offset + 29);
      obj.radialAcceleration = buf.getFloatLE(offset + 33);
      obj.radialTangentAcceleration = buf.getFloatLE(offset + 37);
      if ((nullBits & 4) != 0) {
         obj.linearAcceleration = Vector3f.deserialize(buf, offset + 41);
      }

      obj.radialImpulse = buf.getFloatLE(offset + 53);
      obj.radialTangentImpulse = buf.getFloatLE(offset + 57);
      if ((nullBits & 8) != 0) {
         obj.linearImpulse = Vector3f.deserialize(buf, offset + 61);
      }

      if ((nullBits & 16) != 0) {
         obj.dampingMultiplier = Vector3f.deserialize(buf, offset + 73);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 85;
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
         this.position.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.radialAxis != null) {
         this.radialAxis.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeFloatLE(this.trailPositionMultiplier);
      buf.writeFloatLE(this.radius);
      buf.writeFloatLE(this.radialAcceleration);
      buf.writeFloatLE(this.radialTangentAcceleration);
      if (this.linearAcceleration != null) {
         this.linearAcceleration.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeFloatLE(this.radialImpulse);
      buf.writeFloatLE(this.radialTangentImpulse);
      if (this.linearImpulse != null) {
         this.linearImpulse.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.dampingMultiplier != null) {
         this.dampingMultiplier.serialize(buf);
      } else {
         buf.writeZero(12);
      }
   }

   public int computeSize() {
      return 85;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 85 ? ValidationResult.error("Buffer too small: expected at least 85 bytes") : ValidationResult.OK;
   }

   public ParticleAttractor clone() {
      ParticleAttractor copy = new ParticleAttractor();
      copy.position = this.position != null ? this.position.clone() : null;
      copy.radialAxis = this.radialAxis != null ? this.radialAxis.clone() : null;
      copy.trailPositionMultiplier = this.trailPositionMultiplier;
      copy.radius = this.radius;
      copy.radialAcceleration = this.radialAcceleration;
      copy.radialTangentAcceleration = this.radialTangentAcceleration;
      copy.linearAcceleration = this.linearAcceleration != null ? this.linearAcceleration.clone() : null;
      copy.radialImpulse = this.radialImpulse;
      copy.radialTangentImpulse = this.radialTangentImpulse;
      copy.linearImpulse = this.linearImpulse != null ? this.linearImpulse.clone() : null;
      copy.dampingMultiplier = this.dampingMultiplier != null ? this.dampingMultiplier.clone() : null;
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
