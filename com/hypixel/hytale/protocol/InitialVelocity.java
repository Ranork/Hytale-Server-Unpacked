package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InitialVelocity {
   public static final int NULLABLE_BIT_FIELD_SIZE = 1;
   public static final int FIXED_BLOCK_SIZE = 25;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 25;
   public static final int MAX_SIZE = 25;
   @Nullable
   public Rangef yaw;
   @Nullable
   public Rangef pitch;
   @Nullable
   public Rangef speed;

   public InitialVelocity() {
   }

   public InitialVelocity(@Nullable Rangef yaw, @Nullable Rangef pitch, @Nullable Rangef speed) {
      this.yaw = yaw;
      this.pitch = pitch;
      this.speed = speed;
   }

   public InitialVelocity(@Nonnull InitialVelocity other) {
      this.yaw = other.yaw;
      this.pitch = other.pitch;
      this.speed = other.speed;
   }

   @Nonnull
   public static InitialVelocity deserialize(@Nonnull ByteBuf buf, int offset) {
      InitialVelocity obj = new InitialVelocity();
      byte nullBits = buf.getByte(offset);
      if ((nullBits & 1) != 0) {
         obj.yaw = Rangef.deserialize(buf, offset + 1);
      }

      if ((nullBits & 2) != 0) {
         obj.pitch = Rangef.deserialize(buf, offset + 9);
      }

      if ((nullBits & 4) != 0) {
         obj.speed = Rangef.deserialize(buf, offset + 17);
      }

      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 25;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      byte nullBits = 0;
      if (this.yaw != null) {
         nullBits = (byte)(nullBits | 1);
      }

      if (this.pitch != null) {
         nullBits = (byte)(nullBits | 2);
      }

      if (this.speed != null) {
         nullBits = (byte)(nullBits | 4);
      }

      buf.writeByte(nullBits);
      if (this.yaw != null) {
         this.yaw.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.pitch != null) {
         this.pitch.serialize(buf);
      } else {
         buf.writeZero(8);
      }

      if (this.speed != null) {
         this.speed.serialize(buf);
      } else {
         buf.writeZero(8);
      }
   }

   public int computeSize() {
      return 25;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 25 ? ValidationResult.error("Buffer too small: expected at least 25 bytes") : ValidationResult.OK;
   }

   public InitialVelocity clone() {
      InitialVelocity copy = new InitialVelocity();
      copy.yaw = this.yaw != null ? this.yaw.clone() : null;
      copy.pitch = this.pitch != null ? this.pitch.clone() : null;
      copy.speed = this.speed != null ? this.speed.clone() : null;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof InitialVelocity other)
            ? false
            : Objects.equals(this.yaw, other.yaw) && Objects.equals(this.pitch, other.pitch) && Objects.equals(this.speed, other.speed);
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.yaw, this.pitch, this.speed);
   }
}
