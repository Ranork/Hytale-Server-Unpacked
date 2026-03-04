package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class RepulsionConfig {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 12;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 12;
   public float radius;
   public float minForce;
   public float maxForce;

   public RepulsionConfig() {
   }

   public RepulsionConfig(float radius, float minForce, float maxForce) {
      this.radius = radius;
      this.minForce = minForce;
      this.maxForce = maxForce;
   }

   public RepulsionConfig(@Nonnull RepulsionConfig other) {
      this.radius = other.radius;
      this.minForce = other.minForce;
      this.maxForce = other.maxForce;
   }

   @Nonnull
   public static RepulsionConfig deserialize(@Nonnull ByteBuf buf, int offset) {
      RepulsionConfig obj = new RepulsionConfig();
      obj.radius = buf.getFloatLE(offset + 0);
      obj.minForce = buf.getFloatLE(offset + 4);
      obj.maxForce = buf.getFloatLE(offset + 8);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 12;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.radius);
      buf.writeFloatLE(this.minForce);
      buf.writeFloatLE(this.maxForce);
   }

   public int computeSize() {
      return 12;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 12 ? ValidationResult.error("Buffer too small: expected at least 12 bytes") : ValidationResult.OK;
   }

   public RepulsionConfig clone() {
      RepulsionConfig copy = new RepulsionConfig();
      copy.radius = this.radius;
      copy.minForce = this.minForce;
      copy.maxForce = this.maxForce;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof RepulsionConfig other)
            ? false
            : this.radius == other.radius && this.minForce == other.minForce && this.maxForce == other.maxForce;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.radius, this.minForce, this.maxForce);
   }
}
