package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class InstantData {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 12;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 12;
   public long seconds;
   public int nanos;

   public InstantData() {
   }

   public InstantData(long seconds, int nanos) {
      this.seconds = seconds;
      this.nanos = nanos;
   }

   public InstantData(@Nonnull InstantData other) {
      this.seconds = other.seconds;
      this.nanos = other.nanos;
   }

   @Nonnull
   public static InstantData deserialize(@Nonnull ByteBuf buf, int offset) {
      InstantData obj = new InstantData();
      obj.seconds = buf.getLongLE(offset + 0);
      obj.nanos = buf.getIntLE(offset + 8);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 12;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeLongLE(this.seconds);
      buf.writeIntLE(this.nanos);
   }

   public int computeSize() {
      return 12;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 12 ? ValidationResult.error("Buffer too small: expected at least 12 bytes") : ValidationResult.OK;
   }

   public InstantData clone() {
      InstantData copy = new InstantData();
      copy.seconds = this.seconds;
      copy.nanos = this.nanos;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof InstantData other) ? false : this.seconds == other.seconds && this.nanos == other.nanos;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.seconds, this.nanos);
   }
}
