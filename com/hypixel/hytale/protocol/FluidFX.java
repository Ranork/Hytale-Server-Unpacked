package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import com.hypixel.hytale.protocol.io.VarInt;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FluidFX {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 61;
   public static final int VARIABLE_FIELD_COUNT = 2;
   public static final int VARIABLE_BLOCK_START = 69;
   public static final int MAX_SIZE = 32768087;
   @Nullable
   public String id;
   @Nonnull
   public ShaderType shader = ShaderType.None;
   @Nonnull
   public FluidFog fogMode = FluidFog.Color;
   @Nullable
   public Color fogColor;
   @Nullable
   public NearFar fogDistance;
   public float fogDepthStart;
   public float fogDepthFalloff;
   @Nullable
   public Color colorFilter;
   public float colorSaturation;
   public float distortionAmplitude;
   public float distortionFrequency;
   @Nullable
   public FluidParticle particle;
   @Nullable
   public FluidFXMovementSettings movementSettings;

   public FluidFX() {
   }

   public FluidFX(
      @Nullable String id,
      @Nonnull ShaderType shader,
      @Nonnull FluidFog fogMode,
      @Nullable Color fogColor,
      @Nullable NearFar fogDistance,
      float fogDepthStart,
      float fogDepthFalloff,
      @Nullable Color colorFilter,
      float colorSaturation,
      float distortionAmplitude,
      float distortionFrequency,
      @Nullable FluidParticle particle,
      @Nullable FluidFXMovementSettings movementSettings
   ) {
      this.id = id;
      this.shader = shader;
      this.fogMode = fogMode;
      this.fogColor = fogColor;
      this.fogDistance = fogDistance;
      this.fogDepthStart = fogDepthStart;
      this.fogDepthFalloff = fogDepthFalloff;
      this.colorFilter = colorFilter;
      this.colorSaturation = colorSaturation;
      this.distortionAmplitude = distortionAmplitude;
      this.distortionFrequency = distortionFrequency;
      this.particle = particle;
      this.movementSettings = movementSettings;
   }

   public FluidFX(@Nonnull FluidFX other) {
      this.id = other.id;
      this.shader = other.shader;
      this.fogMode = other.fogMode;
      this.fogColor = other.fogColor;
      this.fogDistance = other.fogDistance;
      this.fogDepthStart = other.fogDepthStart;
      this.fogDepthFalloff = other.fogDepthFalloff;
      this.colorFilter = other.colorFilter;
      this.colorSaturation = other.colorSaturation;
      this.distortionAmplitude = other.distortionAmplitude;
      this.distortionFrequency = other.distortionFrequency;
      this.particle = other.particle;
      this.movementSettings = other.movementSettings;
   }

   @Nonnull
   public static FluidFX deserialize(@Nonnull ByteBuf buf, int offset) {
      FluidFX obj = new FluidFX();
      byte nullBits = buf.getByte(offset);
      obj.shader = ShaderType.fromValue(buf.getByte(offset + 1));
      obj.fogMode = FluidFog.fromValue(buf.getByte(offset + 2));
      if ((nullBits & 1) != 0) {
         obj.fogColor = Color.deserialize(buf, offset + 3);
      }

      if ((nullBits & 2) != 0) {
         obj.fogDistance = NearFar.deserialize(buf, offset + 6);
      }

      obj.fogDepthStart = buf.getFloatLE(offset + 14);
      obj.fogDepthFalloff = buf.getFloatLE(offset + 18);
      if ((nullBits & 4) != 0) {
         obj.colorFilter = Color.deserialize(buf, offset + 22);
      }

      obj.colorSaturation = buf.getFloatLE(offset + 25);
      obj.distortionAmplitude = buf.getFloatLE(offset + 29);
      obj.distortionFrequency = buf.getFloatLE(offset + 33);
      if ((nullBits & 8) != 0) {
         obj.movementSettings = FluidFXMovementSettings.deserialize(buf, offset + 37);
      }

      if ((nullBits & 16) != 0) {
         int varPos0 = offset + 69 + buf.getIntLE(offset + 61);
         int idLen = VarInt.peek(buf, varPos0);
         if (idLen < 0) {
            throw ProtocolException.negativeLength("Id", idLen);
         }

         if (idLen > 4096000) {
            throw ProtocolException.stringTooLong("Id", idLen, 4096000);
         }

         obj.id = PacketIO.readVarString(buf, varPos0, PacketIO.UTF8);
      }

      if ((nullBits & 32) != 0) {
         int varPos1 = offset + 69 + buf.getIntLE(offset + 65);
         obj.particle = FluidParticle.deserialize(buf, varPos1);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      byte nullBits = buf.getByte(offset);
      int maxEnd = 69;
      if ((nullBits & 16) != 0) {
         int fieldOffset0 = buf.getIntLE(offset + 61);
         int pos0 = offset + 69 + fieldOffset0;
         int sl = VarInt.peek(buf, pos0);
         pos0 += VarInt.length(buf, pos0) + sl;
         if (pos0 - offset > maxEnd) {
            maxEnd = pos0 - offset;
         }
      }

      if ((nullBits & 32) != 0) {
         int fieldOffset1 = buf.getIntLE(offset + 65);
         int pos1 = offset + 69 + fieldOffset1;
         pos1 += FluidParticle.computeBytesConsumed(buf, pos1);
         if (pos1 - offset > maxEnd) {
            maxEnd = pos1 - offset;
         }
      }

      return maxEnd;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      byte nullBits = 0;
      if (this.fogColor != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.fogDistance != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.colorFilter != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.movementSettings != null) {
         nullBits = (byte)(nullBits | 8);
      }

      if (this.id != null) {
         nullBits = (byte)(nullBits | 16);
      }

      if (this.particle != null) {
         nullBits = (byte)(nullBits | 32);
      }

      buf.writeByte(nullBits);
      buf.writeByte(this.shader.getValue());
      buf.writeByte(this.fogMode.getValue());
      if (this.fogColor != null) {
         this.fogColor.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      if (this.fogDistance != null) {
         this.fogDistance.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      buf.writeFloatLE(this.fogDepthStart);
      buf.writeFloatLE(this.fogDepthFalloff);
      if (this.colorFilter != null) {
         this.colorFilter.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      buf.writeFloatLE(this.colorSaturation);
      buf.writeFloatLE(this.distortionAmplitude);
      buf.writeFloatLE(this.distortionFrequency);
      if (this.movementSettings != null) {
         this.movementSettings.serialize(buf);
      } else {
         buf.writeZero(24);
      }

      int idOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int particleOffsetSlot = buf.writerIndex();
      buf.writeIntLE(0);
      int varBlockStart = buf.writerIndex();
      if (this.id != null) {
         buf.setIntLE(idOffsetSlot, buf.writerIndex() - varBlockStart);
         PacketIO.writeVarString(buf, this.id, 4096000);
      } else {
         buf.setIntLE(idOffsetSlot, -1);
      }

      if (this.particle != null) {
         buf.setIntLE(particleOffsetSlot, buf.writerIndex() - varBlockStart);
         this.particle.serialize(buf);
      } else {
         buf.setIntLE(particleOffsetSlot, -1);
      }
   }

   public int computeSize() {
      int size = 69;
      if (this.id != null) {
         size += PacketIO.stringSize(this.id);
      }

      if (this.particle != null) {
         size += this.particle.computeSize();
      }

      return size;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 69) {
         return ValidationResult.error("Buffer too small: expected at least 69 bytes");
      } else {
         byte nullBits = buffer.getByte(offset);
         if ((nullBits & 16) != 0) {
            int idOffset = buffer.getIntLE(offset + 61);
            if (idOffset < 0) {
               return ValidationResult.error("Invalid offset for Id");
            }

            int pos = offset + 69 + idOffset;
            if (pos >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Id");
            }

            int idLen = VarInt.peek(buffer, pos);
            if (idLen < 0) {
               return ValidationResult.error("Invalid string length for Id");
            }

            if (idLen > 4096000) {
               return ValidationResult.error("Id exceeds max length 4096000");
            }

            pos += VarInt.length(buffer, pos);
            pos += idLen;
            if (pos > buffer.writerIndex()) {
               return ValidationResult.error("Buffer overflow reading Id");
            }
         }

         if ((nullBits & 32) != 0) {
            int particleOffset = buffer.getIntLE(offset + 65);
            if (particleOffset < 0) {
               return ValidationResult.error("Invalid offset for Particle");
            }

            int posx = offset + 69 + particleOffset;
            if (posx >= buffer.writerIndex()) {
               return ValidationResult.error("Offset out of bounds for Particle");
            }

            ValidationResult particleResult = FluidParticle.validateStructure(buffer, posx);
            if (!particleResult.isValid()) {
               return ValidationResult.error("Invalid Particle: " + particleResult.error());
            }

            posx += FluidParticle.computeBytesConsumed(buffer, posx);
         }

         return ValidationResult.OK;
      }
   }

   public FluidFX clone() {
      FluidFX copy = new FluidFX();
      copy.id = this.id;
      copy.shader = this.shader;
      copy.fogMode = this.fogMode;
      copy.fogColor = this.fogColor != null ? this.fogColor.clone() : null;
      copy.fogDistance = this.fogDistance != null ? this.fogDistance.clone() : null;
      copy.fogDepthStart = this.fogDepthStart;
      copy.fogDepthFalloff = this.fogDepthFalloff;
      copy.colorFilter = this.colorFilter != null ? this.colorFilter.clone() : null;
      copy.colorSaturation = this.colorSaturation;
      copy.distortionAmplitude = this.distortionAmplitude;
      copy.distortionFrequency = this.distortionFrequency;
      copy.particle = this.particle != null ? this.particle.clone() : null;
      copy.movementSettings = this.movementSettings != null ? this.movementSettings.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof FluidFX other)
            ? false
            : Objects.equals(this.id, other.id)
               && Objects.equals(this.shader, other.shader)
               && Objects.equals(this.fogMode, other.fogMode)
               && Objects.equals(this.fogColor, other.fogColor)
               && Objects.equals(this.fogDistance, other.fogDistance)
               && this.fogDepthStart == other.fogDepthStart
               && this.fogDepthFalloff == other.fogDepthFalloff
               && Objects.equals(this.colorFilter, other.colorFilter)
               && this.colorSaturation == other.colorSaturation
               && this.distortionAmplitude == other.distortionAmplitude
               && this.distortionFrequency == other.distortionFrequency
               && Objects.equals(this.particle, other.particle)
               && Objects.equals(this.movementSettings, other.movementSettings);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.id,
         this.shader,
         this.fogMode,
         this.fogColor,
         this.fogDistance,
         this.fogDepthStart,
         this.fogDepthFalloff,
         this.colorFilter,
         this.colorSaturation,
         this.distortionAmplitude,
         this.distortionFrequency,
         this.particle,
         this.movementSettings
      );
   }
}
