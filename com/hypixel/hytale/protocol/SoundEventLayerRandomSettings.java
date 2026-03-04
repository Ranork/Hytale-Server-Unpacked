package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class SoundEventLayerRandomSettings {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 20;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 20;
   public static final int MAX_SIZE = 20;
   public float minVolume;
   public float maxVolume;
   public float minPitch;
   public float maxPitch;
   public float maxStartOffset;

   public SoundEventLayerRandomSettings() {
   }

   public SoundEventLayerRandomSettings(float minVolume, float maxVolume, float minPitch, float maxPitch, float maxStartOffset) {
      this.minVolume = minVolume;
      this.maxVolume = maxVolume;
      this.minPitch = minPitch;
      this.maxPitch = maxPitch;
      this.maxStartOffset = maxStartOffset;
   }

   public SoundEventLayerRandomSettings(@Nonnull SoundEventLayerRandomSettings other) {
      this.minVolume = other.minVolume;
      this.maxVolume = other.maxVolume;
      this.minPitch = other.minPitch;
      this.maxPitch = other.maxPitch;
      this.maxStartOffset = other.maxStartOffset;
   }

   @Nonnull
   public static SoundEventLayerRandomSettings deserialize(@Nonnull ByteBuf buf, int offset) {
      SoundEventLayerRandomSettings obj = new SoundEventLayerRandomSettings();
      obj.minVolume = buf.getFloatLE(offset + 0);
      obj.maxVolume = buf.getFloatLE(offset + 4);
      obj.minPitch = buf.getFloatLE(offset + 8);
      obj.maxPitch = buf.getFloatLE(offset + 12);
      obj.maxStartOffset = buf.getFloatLE(offset + 16);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 20;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.minVolume);
      buf.writeFloatLE(this.maxVolume);
      buf.writeFloatLE(this.minPitch);
      buf.writeFloatLE(this.maxPitch);
      buf.writeFloatLE(this.maxStartOffset);
   }

   public int computeSize() {
      return 20;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 20 ? ValidationResult.error("Buffer too small: expected at least 20 bytes") : ValidationResult.OK;
   }

   public SoundEventLayerRandomSettings clone() {
      SoundEventLayerRandomSettings copy = new SoundEventLayerRandomSettings();
      copy.minVolume = this.minVolume;
      copy.maxVolume = this.maxVolume;
      copy.minPitch = this.minPitch;
      copy.maxPitch = this.maxPitch;
      copy.maxStartOffset = this.maxStartOffset;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SoundEventLayerRandomSettings other)
            ? false
            : this.minVolume == other.minVolume
               && this.maxVolume == other.maxVolume
               && this.minPitch == other.minPitch
               && this.maxPitch == other.maxPitch
               && this.maxStartOffset == other.maxStartOffset;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.minVolume, this.maxVolume, this.minPitch, this.maxPitch, this.maxStartOffset);
   }
}
