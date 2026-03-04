package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class AmbienceFXBlockSoundSet {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 13;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 13;
   public static final int MAX_SIZE = 13;
   public int blockSoundSetIndex;
   @Nullable
   public Rangef percent;

   public AmbienceFXBlockSoundSet() {
   }

   public AmbienceFXBlockSoundSet(int blockSoundSetIndex, @Nullable Rangef percent) {
      this.blockSoundSetIndex = blockSoundSetIndex;
      this.percent = percent;
   }

   public AmbienceFXBlockSoundSet(@Nonnull AmbienceFXBlockSoundSet other) {
      this.blockSoundSetIndex = other.blockSoundSetIndex;
      this.percent = other.percent;
   }

   @Nonnull
   public static AmbienceFXBlockSoundSet deserialize(@Nonnull ByteBuf buf, int offset) {
      AmbienceFXBlockSoundSet obj = new AmbienceFXBlockSoundSet();
      byte nullBits = buf.getByte(offset);
      obj.blockSoundSetIndex = buf.getIntLE(offset + 1);
      if ((nullBits & 1) != 0) {
         obj.percent = Rangef.deserialize(buf, offset + 5);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 13;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.percent != null) {
         nullBits = (byte)(nullBits | 1);
      }

      buf.writeByte(nullBits);
      buf.writeIntLE(this.blockSoundSetIndex);
      if (this.percent != null) {
         this.percent.serialize(buf);
      } else {
         buf.writeZero(8);
      }
   }

   public int computeSize() {
      return 13;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 13 ? ValidationResult.error("Buffer too small: expected at least 13 bytes") : ValidationResult.OK;
   }

   public AmbienceFXBlockSoundSet clone() {
      AmbienceFXBlockSoundSet copy = new AmbienceFXBlockSoundSet();
      copy.blockSoundSetIndex = this.blockSoundSetIndex;
      copy.percent = this.percent != null ? this.percent.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof AmbienceFXBlockSoundSet other)
            ? false
            : this.blockSoundSetIndex == other.blockSoundSetIndex && Objects.equals(this.percent, other.percent);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.blockSoundSetIndex, this.percent);
   }
}
