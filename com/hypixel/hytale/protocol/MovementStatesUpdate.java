package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class MovementStatesUpdate extends ComponentUpdate {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 23;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 23;
   public static final int MAX_SIZE = 23;
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
      if (buf.readableBytes() - offset < 23) {
         throw ProtocolException.bufferTooSmall("MovementStatesUpdate", 23, buf.readableBytes() - offset);
      } else {
         MovementStatesUpdate obj = new MovementStatesUpdate();
         obj.movementStates = MovementStates.deserialize(buf, offset + 0);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 23;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 23L;
   }

   public static MovementStates getMovementStates(MemorySegment mem) {
      return getMovementStates(mem, 0);
   }

   public static MovementStates getMovementStates(MemorySegment mem, int offset) {
      return MovementStates.toObject(mem, offset + 0);
   }

   public static MovementStatesUpdate toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static MovementStatesUpdate toObject(MemorySegment mem, int offset) {
      if (offset + 23 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("MovementStatesUpdate", offset + 23, (int)mem.byteSize());
      } else {
         return new MovementStatesUpdate(MovementStates.toObject(mem, offset + 0));
      }
   }

   @Override
   public int serialize(@Nonnull ByteBuf buf) {
      int startPos = buf.writerIndex();
      this.movementStates.serialize(buf);
      return buf.writerIndex() - startPos;
   }

   @Override
   public int serialize(@Nonnull MemorySegment mem, int offset) {
      this.movementStates.serialize(mem, offset + 0);
      return 23;
   }

   @Override
   public int computeSize() {
      return 23;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 23 ? ValidationResult.error("Buffer too small: expected at least 23 bytes") : ValidationResult.OK;
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
