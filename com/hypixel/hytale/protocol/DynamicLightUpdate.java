package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class DynamicLightUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 4;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 4;
   public static final int MAX_SIZE = 4;
   @Nonnull
   public ColorLight dynamicLight = new ColorLight();

   public DynamicLightUpdate() {
   }

   public DynamicLightUpdate(@Nonnull ColorLight dynamicLight) {
      this.dynamicLight = dynamicLight;
   }

   public DynamicLightUpdate(@Nonnull DynamicLightUpdate other) {
      this.dynamicLight = other.dynamicLight;
   }

   @Nonnull
   public static DynamicLightUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      DynamicLightUpdate obj = new DynamicLightUpdate();
      obj.dynamicLight = ColorLight.deserialize(buf, offset + 0);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 4;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      this.dynamicLight.serialize(buf);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      return 4;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 4 ? ValidationResult.error("Buffer too small: expected at least 4 bytes") : ValidationResult.OK;
   }

   public DynamicLightUpdate clone() {
      DynamicLightUpdate copy = new DynamicLightUpdate();
      copy.dynamicLight = this.dynamicLight.clone();
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof DynamicLightUpdate other ? Objects.equals(this.dynamicLight, other.dynamicLight) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.dynamicLight);
   }
}
