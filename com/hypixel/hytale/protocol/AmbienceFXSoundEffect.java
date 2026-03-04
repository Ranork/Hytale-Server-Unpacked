package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class AmbienceFXSoundEffect {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 9;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 9;
   public static final int MAX_SIZE = 9;
   public int reverbEffectIndex;
   public int equalizerEffectIndex;
   public boolean isInstant;

   public AmbienceFXSoundEffect() {
   }

   public AmbienceFXSoundEffect(int reverbEffectIndex, int equalizerEffectIndex, boolean isInstant) {
      this.reverbEffectIndex = reverbEffectIndex;
      this.equalizerEffectIndex = equalizerEffectIndex;
      this.isInstant = isInstant;
   }

   public AmbienceFXSoundEffect(@Nonnull AmbienceFXSoundEffect other) {
      this.reverbEffectIndex = other.reverbEffectIndex;
      this.equalizerEffectIndex = other.equalizerEffectIndex;
      this.isInstant = other.isInstant;
   }

   @Nonnull
   public static AmbienceFXSoundEffect deserialize(@Nonnull ByteBuf buf, int offset) {
      AmbienceFXSoundEffect obj = new AmbienceFXSoundEffect();
      obj.reverbEffectIndex = buf.getIntLE(offset + 0);
      obj.equalizerEffectIndex = buf.getIntLE(offset + 4);
      obj.isInstant = buf.getByte(offset + 8) != 0;
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 9;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.reverbEffectIndex);
      buf.writeIntLE(this.equalizerEffectIndex);
      buf.writeByte(this.isInstant ? 1 : 0);
   }

   public int computeSize() {
      return 9;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 9 ? ValidationResult.error("Buffer too small: expected at least 9 bytes") : ValidationResult.OK;
   }

   public AmbienceFXSoundEffect clone() {
      AmbienceFXSoundEffect copy = new AmbienceFXSoundEffect();
      copy.reverbEffectIndex = this.reverbEffectIndex;
      copy.equalizerEffectIndex = this.equalizerEffectIndex;
      copy.isInstant = this.isInstant;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AmbienceFXSoundEffect other)
            ? false
            : this.reverbEffectIndex == other.reverbEffectIndex && this.equalizerEffectIndex == other.equalizerEffectIndex && this.isInstant == other.isInstant;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.reverbEffectIndex, this.equalizerEffectIndex, this.isInstant);
   }
}
