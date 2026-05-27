package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class BlockMovementSettings {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 42;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 42;
   public static final int MAX_SIZE = 42;
   public boolean isClimbable;
   public float climbUpSpeedMultiplier;
   public float climbDownSpeedMultiplier;
   public float climbLateralSpeedMultiplier;
   public boolean isBouncy;
   public float bounceVelocity;
   public float drag;
   public float friction;
   public float terminalVelocityModifier;
   public float horizontalSpeedMultiplier;
   public float acceleration;
   public float jumpForceMultiplier;

   public BlockMovementSettings() {
   }

   public BlockMovementSettings(
      boolean isClimbable,
      float climbUpSpeedMultiplier,
      float climbDownSpeedMultiplier,
      float climbLateralSpeedMultiplier,
      boolean isBouncy,
      float bounceVelocity,
      float drag,
      float friction,
      float terminalVelocityModifier,
      float horizontalSpeedMultiplier,
      float acceleration,
      float jumpForceMultiplier
   ) {
      this.isClimbable = isClimbable;
      this.climbUpSpeedMultiplier = climbUpSpeedMultiplier;
      this.climbDownSpeedMultiplier = climbDownSpeedMultiplier;
      this.climbLateralSpeedMultiplier = climbLateralSpeedMultiplier;
      this.isBouncy = isBouncy;
      this.bounceVelocity = bounceVelocity;
      this.drag = drag;
      this.friction = friction;
      this.terminalVelocityModifier = terminalVelocityModifier;
      this.horizontalSpeedMultiplier = horizontalSpeedMultiplier;
      this.acceleration = acceleration;
      this.jumpForceMultiplier = jumpForceMultiplier;
   }

   public BlockMovementSettings(@Nonnull BlockMovementSettings other) {
      this.isClimbable = other.isClimbable;
      this.climbUpSpeedMultiplier = other.climbUpSpeedMultiplier;
      this.climbDownSpeedMultiplier = other.climbDownSpeedMultiplier;
      this.climbLateralSpeedMultiplier = other.climbLateralSpeedMultiplier;
      this.isBouncy = other.isBouncy;
      this.bounceVelocity = other.bounceVelocity;
      this.drag = other.drag;
      this.friction = other.friction;
      this.terminalVelocityModifier = other.terminalVelocityModifier;
      this.horizontalSpeedMultiplier = other.horizontalSpeedMultiplier;
      this.acceleration = other.acceleration;
      this.jumpForceMultiplier = other.jumpForceMultiplier;
   }

   @Nonnull
   public static BlockMovementSettings deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 42) {
         throw ProtocolException.bufferTooSmall("BlockMovementSettings", 42, buf.readableBytes() - offset);
      } else {
         BlockMovementSettings obj = new BlockMovementSettings();
         obj.isClimbable = buf.getByte(offset + 0) != 0;
         obj.climbUpSpeedMultiplier = buf.getFloatLE(offset + 1);
         obj.climbDownSpeedMultiplier = buf.getFloatLE(offset + 5);
         obj.climbLateralSpeedMultiplier = buf.getFloatLE(offset + 9);
         obj.isBouncy = buf.getByte(offset + 13) != 0;
         obj.bounceVelocity = buf.getFloatLE(offset + 14);
         obj.drag = buf.getFloatLE(offset + 18);
         obj.friction = buf.getFloatLE(offset + 22);
         obj.terminalVelocityModifier = buf.getFloatLE(offset + 26);
         obj.horizontalSpeedMultiplier = buf.getFloatLE(offset + 30);
         obj.acceleration = buf.getFloatLE(offset + 34);
         obj.jumpForceMultiplier = buf.getFloatLE(offset + 38);
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 42;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 42L;
   }

   public static boolean getIsClimbable(MemorySegment mem) {
      return getIsClimbable(mem, 0);
   }

   public static boolean getIsClimbable(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0));
   }

   public static float getClimbUpSpeedMultiplier(MemorySegment mem) {
      return getClimbUpSpeedMultiplier(mem, 0);
   }

   public static float getClimbUpSpeedMultiplier(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1));
   }

   public static float getClimbDownSpeedMultiplier(MemorySegment mem) {
      return getClimbDownSpeedMultiplier(mem, 0);
   }

   public static float getClimbDownSpeedMultiplier(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5));
   }

   public static float getClimbLateralSpeedMultiplier(MemorySegment mem) {
      return getClimbLateralSpeedMultiplier(mem, 0);
   }

   public static float getClimbLateralSpeedMultiplier(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9));
   }

   public static boolean getIsBouncy(MemorySegment mem) {
      return getIsBouncy(mem, 0);
   }

   public static boolean getIsBouncy(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 13));
   }

   public static float getBounceVelocity(MemorySegment mem) {
      return getBounceVelocity(mem, 0);
   }

   public static float getBounceVelocity(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 14));
   }

   public static float getDrag(MemorySegment mem) {
      return getDrag(mem, 0);
   }

   public static float getDrag(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 18));
   }

   public static float getFriction(MemorySegment mem) {
      return getFriction(mem, 0);
   }

   public static float getFriction(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 22));
   }

   public static float getTerminalVelocityModifier(MemorySegment mem) {
      return getTerminalVelocityModifier(mem, 0);
   }

   public static float getTerminalVelocityModifier(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 26));
   }

   public static float getHorizontalSpeedMultiplier(MemorySegment mem) {
      return getHorizontalSpeedMultiplier(mem, 0);
   }

   public static float getHorizontalSpeedMultiplier(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 30));
   }

   public static float getAcceleration(MemorySegment mem) {
      return getAcceleration(mem, 0);
   }

   public static float getAcceleration(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 34));
   }

   public static float getJumpForceMultiplier(MemorySegment mem) {
      return getJumpForceMultiplier(mem, 0);
   }

   public static float getJumpForceMultiplier(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 38));
   }

   public static BlockMovementSettings toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static BlockMovementSettings toObject(MemorySegment mem, int offset) {
      if (offset + 42 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("BlockMovementSettings", offset + 42, (int)mem.byteSize());
      } else {
         return new BlockMovementSettings(
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 9)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 13)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 14)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 18)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 22)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 26)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 30)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 34)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 38))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.isClimbable ? 1 : 0);
      buf.writeFloatLE(this.climbUpSpeedMultiplier);
      buf.writeFloatLE(this.climbDownSpeedMultiplier);
      buf.writeFloatLE(this.climbLateralSpeedMultiplier);
      buf.writeByte(this.isBouncy ? 1 : 0);
      buf.writeFloatLE(this.bounceVelocity);
      buf.writeFloatLE(this.drag);
      buf.writeFloatLE(this.friction);
      buf.writeFloatLE(this.terminalVelocityModifier);
      buf.writeFloatLE(this.horizontalSpeedMultiplier);
      buf.writeFloatLE(this.acceleration);
      buf.writeFloatLE(this.jumpForceMultiplier);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BOOL, offset + 0, this.isClimbable);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 1), this.climbUpSpeedMultiplier);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 5), this.climbDownSpeedMultiplier);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 9), this.climbLateralSpeedMultiplier);
      mem.set(PacketIO.PROTO_BOOL, offset + 13, this.isBouncy);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 14), this.bounceVelocity);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 18), this.drag);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 22), this.friction);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 26), this.terminalVelocityModifier);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 30), this.horizontalSpeedMultiplier);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 34), this.acceleration);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 38), this.jumpForceMultiplier);
      return 42;
   }

   public int computeSize() {
      return 42;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 42 ? ValidationResult.error("Buffer too small: expected at least 42 bytes") : ValidationResult.OK;
   }

   public BlockMovementSettings clone() {
      BlockMovementSettings copy = new BlockMovementSettings();
      copy.isClimbable = this.isClimbable;
      copy.climbUpSpeedMultiplier = this.climbUpSpeedMultiplier;
      copy.climbDownSpeedMultiplier = this.climbDownSpeedMultiplier;
      copy.climbLateralSpeedMultiplier = this.climbLateralSpeedMultiplier;
      copy.isBouncy = this.isBouncy;
      copy.bounceVelocity = this.bounceVelocity;
      copy.drag = this.drag;
      copy.friction = this.friction;
      copy.terminalVelocityModifier = this.terminalVelocityModifier;
      copy.horizontalSpeedMultiplier = this.horizontalSpeedMultiplier;
      copy.acceleration = this.acceleration;
      copy.jumpForceMultiplier = this.jumpForceMultiplier;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof BlockMovementSettings other)
            ? false
            : this.isClimbable == other.isClimbable
               && this.climbUpSpeedMultiplier == other.climbUpSpeedMultiplier
               && this.climbDownSpeedMultiplier == other.climbDownSpeedMultiplier
               && this.climbLateralSpeedMultiplier == other.climbLateralSpeedMultiplier
               && this.isBouncy == other.isBouncy
               && this.bounceVelocity == other.bounceVelocity
               && this.drag == other.drag
               && this.friction == other.friction
               && this.terminalVelocityModifier == other.terminalVelocityModifier
               && this.horizontalSpeedMultiplier == other.horizontalSpeedMultiplier
               && this.acceleration == other.acceleration
               && this.jumpForceMultiplier == other.jumpForceMultiplier;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.isClimbable,
         this.climbUpSpeedMultiplier,
         this.climbDownSpeedMultiplier,
         this.climbLateralSpeedMultiplier,
         this.isBouncy,
         this.bounceVelocity,
         this.drag,
         this.friction,
         this.terminalVelocityModifier,
         this.horizontalSpeedMultiplier,
         this.acceleration,
         this.jumpForceMultiplier
      );
   }
}
