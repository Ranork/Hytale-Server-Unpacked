package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class ChargingDelay {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 20;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 20;
   public static final int MAX_SIZE = 20;
   public float minDelay;
   public float maxDelay;
   public float maxTotalDelay;
   public float minHealth;
   public float maxHealth;

   public ChargingDelay() {
   }

   public ChargingDelay(float minDelay, float maxDelay, float maxTotalDelay, float minHealth, float maxHealth) {
      this.minDelay = minDelay;
      this.maxDelay = maxDelay;
      this.maxTotalDelay = maxTotalDelay;
      this.minHealth = minHealth;
      this.maxHealth = maxHealth;
   }

   public ChargingDelay(@Nonnull ChargingDelay other) {
      this.minDelay = other.minDelay;
      this.maxDelay = other.maxDelay;
      this.maxTotalDelay = other.maxTotalDelay;
      this.minHealth = other.minHealth;
      this.maxHealth = other.maxHealth;
   }

   @Nonnull
   public static ChargingDelay deserialize(@Nonnull ByteBuf buf, int offset) {
      ChargingDelay obj = new ChargingDelay();
      obj.minDelay = buf.getFloatLE(offset + 0);
      obj.maxDelay = buf.getFloatLE(offset + 4);
      obj.maxTotalDelay = buf.getFloatLE(offset + 8);
      obj.minHealth = buf.getFloatLE(offset + 12);
      obj.maxHealth = buf.getFloatLE(offset + 16);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 20;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.minDelay);
      buf.writeFloatLE(this.maxDelay);
      buf.writeFloatLE(this.maxTotalDelay);
      buf.writeFloatLE(this.minHealth);
      buf.writeFloatLE(this.maxHealth);
   }

   public int computeSize() {
      return 20;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 20 ? ValidationResult.error("Buffer too small: expected at least 20 bytes") : ValidationResult.OK;
   }

   public ChargingDelay clone() {
      ChargingDelay copy = new ChargingDelay();
      copy.minDelay = this.minDelay;
      copy.maxDelay = this.maxDelay;
      copy.maxTotalDelay = this.maxTotalDelay;
      copy.minHealth = this.minHealth;
      copy.maxHealth = this.maxHealth;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof ChargingDelay other)
            ? false
            : this.minDelay == other.minDelay
               && this.maxDelay == other.maxDelay
               && this.maxTotalDelay == other.maxTotalDelay
               && this.minHealth == other.minHealth
               && this.maxHealth == other.maxHealth;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.minDelay, this.maxDelay, this.maxTotalDelay, this.minHealth, this.maxHealth);
   }
}
