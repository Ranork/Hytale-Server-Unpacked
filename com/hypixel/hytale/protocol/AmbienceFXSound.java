package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AmbienceFXSound {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 27;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 27;
   public static final int MAX_SIZE = 27;
   public int soundEventIndex;
   @Nonnull
   public AmbienceFXSoundPlay3D play3D = AmbienceFXSoundPlay3D.Random;
   public int blockSoundSetIndex;
   @Nonnull
   public AmbienceFXAltitude altitude = AmbienceFXAltitude.Normal;
   @Nullable
   public Rangef frequency;
   @Nullable
   public Range radius;

   public AmbienceFXSound() {
   }

   public AmbienceFXSound(
      int soundEventIndex,
      @Nonnull AmbienceFXSoundPlay3D play3D,
      int blockSoundSetIndex,
      @Nonnull AmbienceFXAltitude altitude,
      @Nullable Rangef frequency,
      @Nullable Range radius
   ) {
      this.soundEventIndex = soundEventIndex;
      this.play3D = play3D;
      this.blockSoundSetIndex = blockSoundSetIndex;
      this.altitude = altitude;
      this.frequency = frequency;
      this.radius = radius;
   }

   public AmbienceFXSound(@Nonnull AmbienceFXSound other) {
      this.soundEventIndex = other.soundEventIndex;
      this.play3D = other.play3D;
      this.blockSoundSetIndex = other.blockSoundSetIndex;
      this.altitude = other.altitude;
      this.frequency = other.frequency;
      this.radius = other.radius;
   }

   @Nonnull
   public static AmbienceFXSound deserialize(@Nonnull ByteBuf buf, int offset) {
      AmbienceFXSound obj = new AmbienceFXSound();
      byte nullBits = buf.getByte(offset);
      obj.soundEventIndex = buf.getIntLE(offset + 1);
      obj.play3D = AmbienceFXSoundPlay3D.fromValue(buf.getByte(offset + 5));
      obj.blockSoundSetIndex = buf.getIntLE(offset + 6);
      obj.altitude = AmbienceFXAltitude.fromValue(buf.getByte(offset + 10));
      if ((nullBits & 1) != 0) {
         obj.frequency = Rangef.deserialize(buf, offset + 11);
      }

      if ((nullBits & 2) != 0) {
         obj.radius = Range.deserialize(buf, offset + 19);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 27;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.frequency != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.radius != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.soundEventIndex);
      buf.writeByte(this.play3D.getValue());
      buf.writeIntLE(this.blockSoundSetIndex);
      buf.writeByte(this.altitude.getValue());
      if (this.frequency != null) {
         this.frequency.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.radius != null) {
         this.radius.serialize(buf);
      } else {
         buf.writeZero(8);
      }
   }

   public int computeSize() {
      return 27;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 27 ? ValidationResult.error("Buffer too small: expected at least 27 bytes") : ValidationResult.OK;
   }

   public AmbienceFXSound clone() {
      AmbienceFXSound copy = new AmbienceFXSound();
      copy.soundEventIndex = this.soundEventIndex;
      copy.play3D = this.play3D;
      copy.blockSoundSetIndex = this.blockSoundSetIndex;
      copy.altitude = this.altitude;
      copy.frequency = this.frequency != null ? this.frequency.clone() : null;
      copy.radius = this.radius != null ? this.radius.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AmbienceFXSound other)
            ? false
            : this.soundEventIndex == other.soundEventIndex
               && Objects.equals(this.play3D, other.play3D)
               && this.blockSoundSetIndex == other.blockSoundSetIndex
               && Objects.equals(this.altitude, other.altitude)
               && Objects.equals(this.frequency, other.frequency)
               && Objects.equals(this.radius, other.radius);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.soundEventIndex, this.play3D, this.blockSoundSetIndex, this.altitude, this.frequency, this.radius);
   }
}
