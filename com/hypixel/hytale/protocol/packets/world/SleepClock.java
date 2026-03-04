package com.hypixel.hytale.protocol.packets.world;

import com.hypixel.hytale.protocol.InstantData;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SleepClock {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 33;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 33;
   public static final int MAX_SIZE = 33;
   @Nullable
   public InstantData startGametime;
   @Nullable
   public InstantData targetGametime;
   public float progress;
   public float durationSeconds;

   public SleepClock() {
   }

   public SleepClock(@Nullable InstantData startGametime, @Nullable InstantData targetGametime, float progress, float durationSeconds) {
      this.startGametime = startGametime;
      this.targetGametime = targetGametime;
      this.progress = progress;
      this.durationSeconds = durationSeconds;
   }

   public SleepClock(@Nonnull SleepClock other) {
      this.startGametime = other.startGametime;
      this.targetGametime = other.targetGametime;
      this.progress = other.progress;
      this.durationSeconds = other.durationSeconds;
   }

   @Nonnull
   public static SleepClock deserialize(@Nonnull ByteBuf buf, int offset) {
      SleepClock obj = new SleepClock();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.startGametime = InstantData.deserialize(buf, offset + 1);
      }

      if ((nullBits & 2) != 0) {
         obj.targetGametime = InstantData.deserialize(buf, offset + 13);
      }

      obj.progress = buf.getFloatLE(offset + 25);
      obj.durationSeconds = buf.getFloatLE(offset + 29);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 33;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.startGametime != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.targetGametime != null) {
         nullBits = (byte)(nullBits | 2);
      }

      buf.writeByte(nullBits);
      if (this.startGametime != null) {
         this.startGametime.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      if (this.targetGametime != null) {
         this.targetGametime.serialize(buf);
      } else {
         buf.writeZero(12);
      }

      buf.writeFloatLE(this.progress);
      buf.writeFloatLE(this.durationSeconds);
   }

   public int computeSize() {
      return 33;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 33 ? ValidationResult.error("Buffer too small: expected at least 33 bytes") : ValidationResult.OK;
   }

   public SleepClock clone() {
      SleepClock copy = new SleepClock();
      copy.startGametime = this.startGametime != null ? this.startGametime.clone() : null;
      copy.targetGametime = this.targetGametime != null ? this.targetGametime.clone() : null;
      copy.progress = this.progress;
      copy.durationSeconds = this.durationSeconds;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof SleepClock other)
            ? false
            : Objects.equals(this.startGametime, other.startGametime)
               && Objects.equals(this.targetGametime, other.targetGametime)
               && this.progress == other.progress
               && this.durationSeconds == other.durationSeconds;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.startGametime, this.targetGametime, this.progress, this.durationSeconds);
   }
}
