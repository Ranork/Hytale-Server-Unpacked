package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class ConditionalBlockSound {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 8;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 8;
   public static final int MAX_SIZE = 8;
   public int soundEventIndex;
   public int ambienceFXIndex;

   public ConditionalBlockSound() {
   }

   public ConditionalBlockSound(int soundEventIndex, int ambienceFXIndex) {
      this.soundEventIndex = soundEventIndex;
      this.ambienceFXIndex = ambienceFXIndex;
   }

   public ConditionalBlockSound(@Nonnull ConditionalBlockSound other) {
      this.soundEventIndex = other.soundEventIndex;
      this.ambienceFXIndex = other.ambienceFXIndex;
   }

   @Nonnull
   public static ConditionalBlockSound deserialize(@Nonnull ByteBuf buf, int offset) {
      ConditionalBlockSound obj = new ConditionalBlockSound();
      obj.soundEventIndex = buf.getIntLE(offset + 0);
      obj.ambienceFXIndex = buf.getIntLE(offset + 4);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 8;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeIntLE(this.soundEventIndex);
      buf.writeIntLE(this.ambienceFXIndex);
   }

   public int computeSize() {
      return 8;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 8 ? ValidationResult.error("Buffer too small: expected at least 8 bytes") : ValidationResult.OK;
   }

   public ConditionalBlockSound clone() {
      ConditionalBlockSound copy = new ConditionalBlockSound();
      copy.soundEventIndex = this.soundEventIndex;
      copy.ambienceFXIndex = this.ambienceFXIndex;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ConditionalBlockSound other)
            ? false
            : this.soundEventIndex == other.soundEventIndex && this.ambienceFXIndex == other.ambienceFXIndex;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.soundEventIndex, this.ambienceFXIndex);
   }
}
