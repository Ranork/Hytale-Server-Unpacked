package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class MovementStatesUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 22;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 22;
   public static final int MAX_SIZE = 22;
   @Nonnull
   public MovementStates movementStates = new MovementStates();

   public MovementStatesUpdate() {
   }

   public MovementStatesUpdate(@Nonnull MovementStates movementStates) {
      this.movementStates = movementStates;
   }

   public MovementStatesUpdate(@Nonnull MovementStatesUpdate other) {
      this.movementStates = other.movementStates;
   }

   @Nonnull
   public static MovementStatesUpdate deserialize(@Nonnull ByteBuf buf, int offset) {
      MovementStatesUpdate obj = new MovementStatesUpdate();
      obj.movementStates = MovementStates.deserialize(buf, offset + 0);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 22;
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      this.movementStates.serialize(buf);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int computeSize() {
      return 22;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 22 ? ValidationResult.error("Buffer too small: expected at least 22 bytes") : ValidationResult.OK;
   }

   public MovementStatesUpdate clone() {
      MovementStatesUpdate copy = new MovementStatesUpdate();
      copy.movementStates = this.movementStates.clone();
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return obj instanceof MovementStatesUpdate other ? Objects.equals(this.movementStates, other.movementStates) : false;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(this.movementStates);
   }
}
