package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class Vector3f {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 12;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 12;
   public static final int MAX_SIZE = 12;
   public float x;
   public float y;
   public float z;

   public Vector3f() {
   }

   public Vector3f(float x, float y, float z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public Vector3f(@Nonnull Vector3f other) {
      this.x = other.x;
      this.y = other.y;
      this.z = other.z;
   }

   @Nonnull
   public static Vector3f deserialize(@Nonnull ByteBuf buf, int offset) {
      Vector3f obj = new Vector3f();
      obj.x = buf.getFloatLE(offset + 0);
      obj.y = buf.getFloatLE(offset + 4);
      obj.z = buf.getFloatLE(offset + 8);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 12;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.x);
      buf.writeFloatLE(this.y);
      buf.writeFloatLE(this.z);
   }

   public int computeSize() {
      return 12;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 12 ? ValidationResult.error("Buffer too small: expected at least 12 bytes") : ValidationResult.OK;
   }

   public Vector3f clone() {
      Vector3f copy = new Vector3f();
      copy.x = this.x;
      copy.y = this.y;
      copy.z = this.z;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof Vector3f other) ? false : this.x == other.x && this.y == other.y && this.z == other.z;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.x, this.y, this.z);
   }
}
