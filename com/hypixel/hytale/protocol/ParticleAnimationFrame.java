package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ParticleAnimationFrame {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 58;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 58;
   public static final int MAX_SIZE = 58;
   @Nullable
   public Range frameIndex;
   @Nullable
   public RangeVector2f scale;
   @Nullable
   public RangeVector3f rotation;
   @Nullable
   public Color color;
   public float opacity;

   public ParticleAnimationFrame() {
   }

   public ParticleAnimationFrame(
      @Nullable Range frameIndex, @Nullable RangeVector2f scale, @Nullable RangeVector3f rotation, @Nullable Color color, float opacity
   ) {
      this.frameIndex = frameIndex;
      this.scale = scale;
      this.rotation = rotation;
      this.color = color;
      this.opacity = opacity;
   }

   public ParticleAnimationFrame(@Nonnull ParticleAnimationFrame other) {
      this.frameIndex = other.frameIndex;
      this.scale = other.scale;
      this.rotation = other.rotation;
      this.color = other.color;
      this.opacity = other.opacity;
   }

   @Nonnull
   public static ParticleAnimationFrame deserialize(@Nonnull ByteBuf buf, int offset) {
      ParticleAnimationFrame obj = new ParticleAnimationFrame();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.frameIndex = Range.deserialize(buf, offset + 1);
      }

      if ((nullBits & 2) != 0) {
         obj.scale = RangeVector2f.deserialize(buf, offset + 9);
      }

      if ((nullBits & 4) != 0) {
         obj.rotation = RangeVector3f.deserialize(buf, offset + 26);
      }

      if ((nullBits & 8) != 0) {
         obj.color = Color.deserialize(buf, offset + 51);
      }

      obj.opacity = buf.getFloatLE(offset + 54);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 58;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.frameIndex != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.scale != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.rotation != null) {
         nullBits = (byte)(nullBits | 4);
      }

      if (this.color != null) {
         nullBits = (byte)(nullBits | 8);
      }

      buf.writeByte(nullBits);
      if (this.frameIndex != null) {
         this.frameIndex.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.scale != null) {
         this.scale.serialize(buf);
      } else {
         buf.writeZero(17);
      }

      if (this.rotation != null) {
         this.rotation.serialize(buf);
      } else {
         buf.writeZero(25);
      }

      if (this.color != null) {
         this.color.serialize(buf);
      } else {
         buf.writeZero(3);
      }

      buf.writeFloatLE(this.opacity);
   }

   public int computeSize() {
      return 58;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 58 ? ValidationResult.error("Buffer too small: expected at least 58 bytes") : ValidationResult.OK;
   }

   public ParticleAnimationFrame clone() {
      ParticleAnimationFrame copy = new ParticleAnimationFrame();
      copy.frameIndex = this.frameIndex != null ? this.frameIndex.clone() : null;
      copy.scale = this.scale != null ? this.scale.clone() : null;
      copy.rotation = this.rotation != null ? this.rotation.clone() : null;
      copy.color = this.color != null ? this.color.clone() : null;
      copy.opacity = this.opacity;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ParticleAnimationFrame other)
            ? false
            : Objects.equals(this.frameIndex, other.frameIndex)
               && Objects.equals(this.scale, other.scale)
               && Objects.equals(this.rotation, other.rotation)
               && Objects.equals(this.color, other.color)
               && this.opacity == other.opacity;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.frameIndex, this.scale, this.rotation, this.color, this.opacity);
   }
}
