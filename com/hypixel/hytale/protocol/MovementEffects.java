package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 7) {
         throw ProtocolException.bufferTooSmall("MovementEffects", 7, buf.readableBytes() - offset);
      } else {
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
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 7;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 7L;
   }

   public static boolean getDisableForward(MemorySegment mem) {
      return getDisableForward(mem, 0);
   }

   public static boolean getDisableForward(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0));
   }

   public static boolean getDisableBackward(MemorySegment mem) {
      return getDisableBackward(mem, 0);
   }

   public static boolean getDisableBackward(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   public static boolean getDisableLeft(MemorySegment mem) {
      return getDisableLeft(mem, 0);
   }

   public static boolean getDisableLeft(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2));
   }

   public static boolean getDisableRight(MemorySegment mem) {
      return getDisableRight(mem, 0);
   }

   public static boolean getDisableRight(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 3));
   }

   public static boolean getDisableSprint(MemorySegment mem) {
      return getDisableSprint(mem, 0);
   }

   public static boolean getDisableSprint(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 4));
   }

   public static boolean getDisableJump(MemorySegment mem) {
      return getDisableJump(mem, 0);
   }

   public static boolean getDisableJump(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 5));
   }

   public static boolean getDisableCrouch(MemorySegment mem) {
      return getDisableCrouch(mem, 0);
   }

   public static boolean getDisableCrouch(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 6));
   }

   public static MovementEffects toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static MovementEffects toObject(MemorySegment mem, int offset) {
      if (offset + 7 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("MovementEffects", offset + 7, (int)mem.byteSize());
      } else {
         return new MovementEffects(
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 3)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 6))
         );
      }
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BOOL, offset + 0, this.disableForward);
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.disableBackward);
      mem.set(PacketIO.PROTO_BOOL, offset + 2, this.disableLeft);
      mem.set(PacketIO.PROTO_BOOL, offset + 3, this.disableRight);
      mem.set(PacketIO.PROTO_BOOL, offset + 4, this.disableSprint);
      mem.set(PacketIO.PROTO_BOOL, offset + 5, this.disableJump);
      mem.set(PacketIO.PROTO_BOOL, offset + 6, this.disableCrouch);
      return 7;
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
