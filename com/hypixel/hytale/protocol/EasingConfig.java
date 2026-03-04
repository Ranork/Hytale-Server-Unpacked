package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class EasingConfig {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 5;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 5;
   public static final int MAX_SIZE = 5;
   public float time;
   @Nonnull
   public EasingType type = EasingType.Linear;

   public EasingConfig() {
   }

   public EasingConfig(float time, @Nonnull EasingType type) {
      this.time = time;
      this.type = type;
   }

   public EasingConfig(@Nonnull EasingConfig other) {
      this.time = other.time;
      this.type = other.type;
   }

   @Nonnull
   public static EasingConfig deserialize(@Nonnull ByteBuf buf, int offset) {
      EasingConfig obj = new EasingConfig();
      obj.time = buf.getFloatLE(offset + 0);
      obj.type = EasingType.fromValue(buf.getByte(offset + 4));
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 5;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.time);
      buf.writeByte(this.type.getValue());
   }

   public int computeSize() {
      return 5;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 5 ? ValidationResult.error("Buffer too small: expected at least 5 bytes") : ValidationResult.OK;
   }

   public EasingConfig clone() {
      EasingConfig copy = new EasingConfig();
      copy.time = this.time;
      copy.type = this.type;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof EasingConfig other) ? false : this.time == other.time && Objects.equals(this.type, other.type);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.time, this.type);
   }
}
