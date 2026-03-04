package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NoiseConfig {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 23;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 23;
   public static final int MAX_SIZE = 23;
   public int seed;
   @Nonnull
   public NoiseType type = NoiseType.Sin;
   public float frequency;
   public float amplitude;
   @Nullable
   public ClampConfig clamp;

   public NoiseConfig() {
   }

   public NoiseConfig(int seed, @Nonnull NoiseType type, float frequency, float amplitude, @Nullable ClampConfig clamp) {
      this.seed = seed;
      this.type = type;
      this.frequency = frequency;
      this.amplitude = amplitude;
      this.clamp = clamp;
   }

   public NoiseConfig(@Nonnull NoiseConfig other) {
      this.seed = other.seed;
      this.type = other.type;
      this.frequency = other.frequency;
      this.amplitude = other.amplitude;
      this.clamp = other.clamp;
   }

   @Nonnull
   public static NoiseConfig deserialize(@Nonnull ByteBuf buf, int offset) {
      NoiseConfig obj = new NoiseConfig();
      byte nullBits = buf.getByte(offset);
      obj.seed = buf.getIntLE(offset + 1);
      obj.type = NoiseType.fromValue(buf.getByte(offset + 5));
      obj.frequency = buf.getFloatLE(offset + 6);
      obj.amplitude = buf.getFloatLE(offset + 10);
      if ((nullBits & 1) != 0) {
         obj.clamp = ClampConfig.deserialize(buf, offset + 14);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 23;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.clamp != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.seed);
      buf.writeByte(this.type.getValue());
      buf.writeFloatLE(this.frequency);
      buf.writeFloatLE(this.amplitude);
      if (this.clamp != null) {
         this.clamp.serialize(buf);
      } else {
         buf.writeZero(9);
      }
   }

   public int computeSize() {
      return 23;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 23 ? ValidationResult.error("Buffer too small: expected at least 23 bytes") : ValidationResult.OK;
   }

   public NoiseConfig clone() {
      NoiseConfig copy = new NoiseConfig();
      copy.seed = this.seed;
      copy.type = this.type;
      copy.frequency = this.frequency;
      copy.amplitude = this.amplitude;
      copy.clamp = this.clamp != null ? this.clamp.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof NoiseConfig other)
            ? false
            : this.seed == other.seed
               && Objects.equals(this.type, other.type)
               && this.frequency == other.frequency
               && this.amplitude == other.amplitude
               && Objects.equals(this.clamp, other.clamp);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.seed, this.type, this.frequency, this.amplitude, this.clamp);
   }
}
