package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class MovementEffects {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 7;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 7;
   public static final int MAX_SIZE = 7;
   public boolean disableForward;
   public boolean disableBackward;
   public boolean disableLeft;
   public boolean disableRight;
   public boolean disableSprint;
   public boolean disableJump;
   public boolean disableCrouch;

   public MovementEffects() {
   }

   public MovementEffects(
      boolean disableForward,
      boolean disableBackward,
      boolean disableLeft,
      boolean disableRight,
      boolean disableSprint,
      boolean disableJump,
      boolean disableCrouch
   ) {
      this.disableForward = disableForward;
      this.disableBackward = disableBackward;
      this.disableLeft = disableLeft;
      this.disableRight = disableRight;
      this.disableSprint = disableSprint;
      this.disableJump = disableJump;
      this.disableCrouch = disableCrouch;
   }

   public MovementEffects(@Nonnull MovementEffects other) {
      this.disableForward = other.disableForward;
      this.disableBackward = other.disableBackward;
      this.disableLeft = other.disableLeft;
      this.disableRight = other.disableRight;
      this.disableSprint = other.disableSprint;
      this.disableJump = other.disableJump;
      this.disableCrouch = other.disableCrouch;
   }

   @Nonnull
   public static MovementEffects deserialize(@Nonnull ByteBuf buf, int offset) {
      MovementEffects obj = new MovementEffects();
      obj.disableForward = buf.getByte(offset + 0) != 0;
      obj.disableBackward = buf.getByte(offset + 1) != 0;
      obj.disableLeft = buf.getByte(offset + 2) != 0;
      obj.disableRight = buf.getByte(offset + 3) != 0;
      obj.disableSprint = buf.getByte(offset + 4) != 0;
      obj.disableJump = buf.getByte(offset + 5) != 0;
      obj.disableCrouch = buf.getByte(offset + 6) != 0;
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 7;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.disableForward ? 1 : 0);
      buf.writeByte(this.disableBackward ? 1 : 0);
      buf.writeByte(this.disableLeft ? 1 : 0);
      buf.writeByte(this.disableRight ? 1 : 0);
      buf.writeByte(this.disableSprint ? 1 : 0);
      buf.writeByte(this.disableJump ? 1 : 0);
      buf.writeByte(this.disableCrouch ? 1 : 0);
   }

   public int computeSize() {
      return 7;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 7 ? ValidationResult.error("Buffer too small: expected at least 7 bytes") : ValidationResult.OK;
   }

   public MovementEffects clone() {
      MovementEffects copy = new MovementEffects();
      copy.disableForward = this.disableForward;
      copy.disableBackward = this.disableBackward;
      copy.disableLeft = this.disableLeft;
      copy.disableRight = this.disableRight;
      copy.disableSprint = this.disableSprint;
      copy.disableJump = this.disableJump;
      copy.disableCrouch = this.disableCrouch;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof MovementEffects other)
            ? false
            : this.disableForward == other.disableForward
               && this.disableBackward == other.disableBackward
               && this.disableLeft == other.disableLeft
               && this.disableRight == other.disableRight
               && this.disableSprint == other.disableSprint
               && this.disableJump == other.disableJump
               && this.disableCrouch == other.disableCrouch;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.disableForward, this.disableBackward, this.disableLeft, this.disableRight, this.disableSprint, this.disableJump, this.disableCrouch
      );
   }
}
