package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class WiggleWeights {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 40;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 40;
   public static final int MAX_SIZE = 40;
   public float x;
   public float xDeceleration;
   public float y;
   public float yDeceleration;
   public float z;
   public float zDeceleration;
   public float roll;
   public float rollDeceleration;
   public float pitch;
   public float pitchDeceleration;

   public WiggleWeights() {
   }

   public WiggleWeights(
      float x,
      float xDeceleration,
      float y,
      float yDeceleration,
      float z,
      float zDeceleration,
      float roll,
      float rollDeceleration,
      float pitch,
      float pitchDeceleration
   ) {
      this.x = x;
      this.xDeceleration = xDeceleration;
      this.y = y;
      this.yDeceleration = yDeceleration;
      this.z = z;
      this.zDeceleration = zDeceleration;
      this.roll = roll;
      this.rollDeceleration = rollDeceleration;
      this.pitch = pitch;
      this.pitchDeceleration = pitchDeceleration;
   }

   public WiggleWeights(@Nonnull WiggleWeights other) {
      this.x = other.x;
      this.xDeceleration = other.xDeceleration;
      this.y = other.y;
      this.yDeceleration = other.yDeceleration;
      this.z = other.z;
      this.zDeceleration = other.zDeceleration;
      this.roll = other.roll;
      this.rollDeceleration = other.rollDeceleration;
      this.pitch = other.pitch;
      this.pitchDeceleration = other.pitchDeceleration;
   }

   @Nonnull
   public static WiggleWeights deserialize(@Nonnull ByteBuf buf, int offset) {
      WiggleWeights obj = new WiggleWeights();
      obj.x = buf.getFloatLE(offset + 0);
      obj.xDeceleration = buf.getFloatLE(offset + 4);
      obj.y = buf.getFloatLE(offset + 8);
      obj.yDeceleration = buf.getFloatLE(offset + 12);
      obj.z = buf.getFloatLE(offset + 16);
      obj.zDeceleration = buf.getFloatLE(offset + 20);
      obj.roll = buf.getFloatLE(offset + 24);
      obj.rollDeceleration = buf.getFloatLE(offset + 28);
      obj.pitch = buf.getFloatLE(offset + 32);
      obj.pitchDeceleration = buf.getFloatLE(offset + 36);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 40;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.x);
      buf.writeFloatLE(this.xDeceleration);
      buf.writeFloatLE(this.y);
      buf.writeFloatLE(this.yDeceleration);
      buf.writeFloatLE(this.z);
      buf.writeFloatLE(this.zDeceleration);
      buf.writeFloatLE(this.roll);
      buf.writeFloatLE(this.rollDeceleration);
      buf.writeFloatLE(this.pitch);
      buf.writeFloatLE(this.pitchDeceleration);
   }

   public int computeSize() {
      return 40;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 40 ? ValidationResult.error("Buffer too small: expected at least 40 bytes") : ValidationResult.OK;
   }

   public WiggleWeights clone() {
      WiggleWeights copy = new WiggleWeights();
      copy.x = this.x;
      copy.xDeceleration = this.xDeceleration;
      copy.y = this.y;
      copy.yDeceleration = this.yDeceleration;
      copy.z = this.z;
      copy.zDeceleration = this.zDeceleration;
      copy.roll = this.roll;
      copy.rollDeceleration = this.rollDeceleration;
      copy.pitch = this.pitch;
      copy.pitchDeceleration = this.pitchDeceleration;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof WiggleWeights other)
            ? false
            : this.x == other.x
               && this.xDeceleration == other.xDeceleration
               && this.y == other.y
               && this.yDeceleration == other.yDeceleration
               && this.z == other.z
               && this.zDeceleration == other.zDeceleration
               && this.roll == other.roll
               && this.rollDeceleration == other.rollDeceleration
               && this.pitch == other.pitch
               && this.pitchDeceleration == other.pitchDeceleration;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.x,
         this.xDeceleration,
         this.y,
         this.yDeceleration,
         this.z,
         this.zDeceleration,
         this.roll,
         this.rollDeceleration,
         this.pitch,
         this.pitchDeceleration
      );
   }
}
