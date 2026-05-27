package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
import java.util.Objects;
import javax.annotation.Nonnull;

public class MovementStates {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 23;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 23;
   public static final int MAX_SIZE = 23;
   public boolean idle;
   public boolean horizontalIdle;
   public boolean jumping;
   public boolean flying;
   public boolean walking;
   public boolean running;
   public boolean sprinting;
   public boolean crouching;
   public boolean forcedCrouching;
   public boolean falling;
   public boolean fallingFar;
   public boolean climbing;
   public boolean inFluid;
   public boolean swimming;
   public boolean swimJumping;
   public boolean onGround;
   public boolean mantling;
   public boolean sliding;
   public boolean mounting;
   public boolean rolling;
   public boolean sitting;
   public boolean gliding;
   public boolean sleeping;

   public MovementStates() {
   }

   public MovementStates(
      boolean idle,
      boolean horizontalIdle,
      boolean jumping,
      boolean flying,
      boolean walking,
      boolean running,
      boolean sprinting,
      boolean crouching,
      boolean forcedCrouching,
      boolean falling,
      boolean fallingFar,
      boolean climbing,
      boolean inFluid,
      boolean swimming,
      boolean swimJumping,
      boolean onGround,
      boolean mantling,
      boolean sliding,
      boolean mounting,
      boolean rolling,
      boolean sitting,
      boolean gliding,
      boolean sleeping
   ) {
      this.idle = idle;
      this.horizontalIdle = horizontalIdle;
      this.jumping = jumping;
      this.flying = flying;
      this.walking = walking;
      this.running = running;
      this.sprinting = sprinting;
      this.crouching = crouching;
      this.forcedCrouching = forcedCrouching;
      this.falling = falling;
      this.fallingFar = fallingFar;
      this.climbing = climbing;
      this.inFluid = inFluid;
      this.swimming = swimming;
      this.swimJumping = swimJumping;
      this.onGround = onGround;
      this.mantling = mantling;
      this.sliding = sliding;
      this.mounting = mounting;
      this.rolling = rolling;
      this.sitting = sitting;
      this.gliding = gliding;
      this.sleeping = sleeping;
   }

   public MovementStates(@Nonnull MovementStates other) {
      this.idle = other.idle;
      this.horizontalIdle = other.horizontalIdle;
      this.jumping = other.jumping;
      this.flying = other.flying;
      this.walking = other.walking;
      this.running = other.running;
      this.sprinting = other.sprinting;
      this.crouching = other.crouching;
      this.forcedCrouching = other.forcedCrouching;
      this.falling = other.falling;
      this.fallingFar = other.fallingFar;
      this.climbing = other.climbing;
      this.inFluid = other.inFluid;
      this.swimming = other.swimming;
      this.swimJumping = other.swimJumping;
      this.onGround = other.onGround;
      this.mantling = other.mantling;
      this.sliding = other.sliding;
      this.mounting = other.mounting;
      this.rolling = other.rolling;
      this.sitting = other.sitting;
      this.gliding = other.gliding;
      this.sleeping = other.sleeping;
   }

   @Nonnull
   public static MovementStates deserialize(@Nonnull ByteBuf buf, int offset) {
      if (buf.readableBytes() - offset < 23) {
         throw ProtocolException.bufferTooSmall("MovementStates", 23, buf.readableBytes() - offset);
      } else {
         MovementStates obj = new MovementStates();
         obj.idle = buf.getByte(offset + 0) != 0;
         obj.horizontalIdle = buf.getByte(offset + 1) != 0;
         obj.jumping = buf.getByte(offset + 2) != 0;
         obj.flying = buf.getByte(offset + 3) != 0;
         obj.walking = buf.getByte(offset + 4) != 0;
         obj.running = buf.getByte(offset + 5) != 0;
         obj.sprinting = buf.getByte(offset + 6) != 0;
         obj.crouching = buf.getByte(offset + 7) != 0;
         obj.forcedCrouching = buf.getByte(offset + 8) != 0;
         obj.falling = buf.getByte(offset + 9) != 0;
         obj.fallingFar = buf.getByte(offset + 10) != 0;
         obj.climbing = buf.getByte(offset + 11) != 0;
         obj.inFluid = buf.getByte(offset + 12) != 0;
         obj.swimming = buf.getByte(offset + 13) != 0;
         obj.swimJumping = buf.getByte(offset + 14) != 0;
         obj.onGround = buf.getByte(offset + 15) != 0;
         obj.mantling = buf.getByte(offset + 16) != 0;
         obj.sliding = buf.getByte(offset + 17) != 0;
         obj.mounting = buf.getByte(offset + 18) != 0;
         obj.rolling = buf.getByte(offset + 19) != 0;
         obj.sitting = buf.getByte(offset + 20) != 0;
         obj.gliding = buf.getByte(offset + 21) != 0;
         obj.sleeping = buf.getByte(offset + 22) != 0;
         return obj;
      }
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 23;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 23L;
   }

   public static boolean getIdle(MemorySegment mem) {
      return getIdle(mem, 0);
   }

   public static boolean getIdle(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0));
   }

   public static boolean getHorizontalIdle(MemorySegment mem) {
      return getHorizontalIdle(mem, 0);
   }

   public static boolean getHorizontalIdle(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1));
   }

   public static boolean getJumping(MemorySegment mem) {
      return getJumping(mem, 0);
   }

   public static boolean getJumping(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2));
   }

   public static boolean getFlying(MemorySegment mem) {
      return getFlying(mem, 0);
   }

   public static boolean getFlying(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 3));
   }

   public static boolean getWalking(MemorySegment mem) {
      return getWalking(mem, 0);
   }

   public static boolean getWalking(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 4));
   }

   public static boolean getRunning(MemorySegment mem) {
      return getRunning(mem, 0);
   }

   public static boolean getRunning(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 5));
   }

   public static boolean getSprinting(MemorySegment mem) {
      return getSprinting(mem, 0);
   }

   public static boolean getSprinting(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 6));
   }

   public static boolean getCrouching(MemorySegment mem) {
      return getCrouching(mem, 0);
   }

   public static boolean getCrouching(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 7));
   }

   public static boolean getForcedCrouching(MemorySegment mem) {
      return getForcedCrouching(mem, 0);
   }

   public static boolean getForcedCrouching(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 8));
   }

   public static boolean getFalling(MemorySegment mem) {
      return getFalling(mem, 0);
   }

   public static boolean getFalling(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 9));
   }

   public static boolean getFallingFar(MemorySegment mem) {
      return getFallingFar(mem, 0);
   }

   public static boolean getFallingFar(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 10));
   }

   public static boolean getClimbing(MemorySegment mem) {
      return getClimbing(mem, 0);
   }

   public static boolean getClimbing(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 11));
   }

   public static boolean getInFluid(MemorySegment mem) {
      return getInFluid(mem, 0);
   }

   public static boolean getInFluid(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 12));
   }

   public static boolean getSwimming(MemorySegment mem) {
      return getSwimming(mem, 0);
   }

   public static boolean getSwimming(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 13));
   }

   public static boolean getSwimJumping(MemorySegment mem) {
      return getSwimJumping(mem, 0);
   }

   public static boolean getSwimJumping(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 14));
   }

   public static boolean getOnGround(MemorySegment mem) {
      return getOnGround(mem, 0);
   }

   public static boolean getOnGround(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 15));
   }

   public static boolean getMantling(MemorySegment mem) {
      return getMantling(mem, 0);
   }

   public static boolean getMantling(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 16));
   }

   public static boolean getSliding(MemorySegment mem) {
      return getSliding(mem, 0);
   }

   public static boolean getSliding(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 17));
   }

   public static boolean getMounting(MemorySegment mem) {
      return getMounting(mem, 0);
   }

   public static boolean getMounting(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 18));
   }

   public static boolean getRolling(MemorySegment mem) {
      return getRolling(mem, 0);
   }

   public static boolean getRolling(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 19));
   }

   public static boolean getSitting(MemorySegment mem) {
      return getSitting(mem, 0);
   }

   public static boolean getSitting(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 20));
   }

   public static boolean getGliding(MemorySegment mem) {
      return getGliding(mem, 0);
   }

   public static boolean getGliding(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 21));
   }

   public static boolean getSleeping(MemorySegment mem) {
      return getSleeping(mem, 0);
   }

   public static boolean getSleeping(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 22));
   }

   public static MovementStates toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static MovementStates toObject(MemorySegment mem, int offset) {
      if (offset + 23 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("MovementStates", offset + 23, (int)mem.byteSize());
      } else {
         return new MovementStates(
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 0)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 2)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 3)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 4)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 5)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 6)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 7)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 8)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 9)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 10)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 11)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 12)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 13)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 14)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 15)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 16)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 17)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 18)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 19)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 20)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 21)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 22))
         );
      }
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.idle ? 1 : 0);
      buf.writeByte(this.horizontalIdle ? 1 : 0);
      buf.writeByte(this.jumping ? 1 : 0);
      buf.writeByte(this.flying ? 1 : 0);
      buf.writeByte(this.walking ? 1 : 0);
      buf.writeByte(this.running ? 1 : 0);
      buf.writeByte(this.sprinting ? 1 : 0);
      buf.writeByte(this.crouching ? 1 : 0);
      buf.writeByte(this.forcedCrouching ? 1 : 0);
      buf.writeByte(this.falling ? 1 : 0);
      buf.writeByte(this.fallingFar ? 1 : 0);
      buf.writeByte(this.climbing ? 1 : 0);
      buf.writeByte(this.inFluid ? 1 : 0);
      buf.writeByte(this.swimming ? 1 : 0);
      buf.writeByte(this.swimJumping ? 1 : 0);
      buf.writeByte(this.onGround ? 1 : 0);
      buf.writeByte(this.mantling ? 1 : 0);
      buf.writeByte(this.sliding ? 1 : 0);
      buf.writeByte(this.mounting ? 1 : 0);
      buf.writeByte(this.rolling ? 1 : 0);
      buf.writeByte(this.sitting ? 1 : 0);
      buf.writeByte(this.gliding ? 1 : 0);
      buf.writeByte(this.sleeping ? 1 : 0);
   }

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BOOL, offset + 0, this.idle);
      mem.set(PacketIO.PROTO_BOOL, offset + 1, this.horizontalIdle);
      mem.set(PacketIO.PROTO_BOOL, offset + 2, this.jumping);
      mem.set(PacketIO.PROTO_BOOL, offset + 3, this.flying);
      mem.set(PacketIO.PROTO_BOOL, offset + 4, this.walking);
      mem.set(PacketIO.PROTO_BOOL, offset + 5, this.running);
      mem.set(PacketIO.PROTO_BOOL, offset + 6, this.sprinting);
      mem.set(PacketIO.PROTO_BOOL, offset + 7, this.crouching);
      mem.set(PacketIO.PROTO_BOOL, offset + 8, this.forcedCrouching);
      mem.set(PacketIO.PROTO_BOOL, offset + 9, this.falling);
      mem.set(PacketIO.PROTO_BOOL, offset + 10, this.fallingFar);
      mem.set(PacketIO.PROTO_BOOL, offset + 11, this.climbing);
      mem.set(PacketIO.PROTO_BOOL, offset + 12, this.inFluid);
      mem.set(PacketIO.PROTO_BOOL, offset + 13, this.swimming);
      mem.set(PacketIO.PROTO_BOOL, offset + 14, this.swimJumping);
      mem.set(PacketIO.PROTO_BOOL, offset + 15, this.onGround);
      mem.set(PacketIO.PROTO_BOOL, offset + 16, this.mantling);
      mem.set(PacketIO.PROTO_BOOL, offset + 17, this.sliding);
      mem.set(PacketIO.PROTO_BOOL, offset + 18, this.mounting);
      mem.set(PacketIO.PROTO_BOOL, offset + 19, this.rolling);
      mem.set(PacketIO.PROTO_BOOL, offset + 20, this.sitting);
      mem.set(PacketIO.PROTO_BOOL, offset + 21, this.gliding);
      mem.set(PacketIO.PROTO_BOOL, offset + 22, this.sleeping);
      return 23;
   }

   public int computeSize() {
      return 23;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 23 ? ValidationResult.error("Buffer too small: expected at least 23 bytes") : ValidationResult.OK;
   }

   public MovementStates clone() {
      MovementStates copy = new MovementStates();
      copy.idle = this.idle;
      copy.horizontalIdle = this.horizontalIdle;
      copy.jumping = this.jumping;
      copy.flying = this.flying;
      copy.walking = this.walking;
      copy.running = this.running;
      copy.sprinting = this.sprinting;
      copy.crouching = this.crouching;
      copy.forcedCrouching = this.forcedCrouching;
      copy.falling = this.falling;
      copy.fallingFar = this.fallingFar;
      copy.climbing = this.climbing;
      copy.inFluid = this.inFluid;
      copy.swimming = this.swimming;
      copy.swimJumping = this.swimJumping;
      copy.onGround = this.onGround;
      copy.mantling = this.mantling;
      copy.sliding = this.sliding;
      copy.mounting = this.mounting;
      copy.rolling = this.rolling;
      copy.sitting = this.sitting;
      copy.gliding = this.gliding;
      copy.sleeping = this.sleeping;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof MovementStates other)
            ? false
            : this.idle == other.idle
               && this.horizontalIdle == other.horizontalIdle
               && this.jumping == other.jumping
               && this.flying == other.flying
               && this.walking == other.walking
               && this.running == other.running
               && this.sprinting == other.sprinting
               && this.crouching == other.crouching
               && this.forcedCrouching == other.forcedCrouching
               && this.falling == other.falling
               && this.fallingFar == other.fallingFar
               && this.climbing == other.climbing
               && this.inFluid == other.inFluid
               && this.swimming == other.swimming
               && this.swimJumping == other.swimJumping
               && this.onGround == other.onGround
               && this.mantling == other.mantling
               && this.sliding == other.sliding
               && this.mounting == other.mounting
               && this.rolling == other.rolling
               && this.sitting == other.sitting
               && this.gliding == other.gliding
               && this.sleeping == other.sleeping;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.idle,
         this.horizontalIdle,
         this.jumping,
         this.flying,
         this.walking,
         this.running,
         this.sprinting,
         this.crouching,
         this.forcedCrouching,
         this.falling,
         this.fallingFar,
         this.climbing,
         this.inFluid,
         this.swimming,
         this.swimJumping,
         this.onGround,
         this.mantling,
         this.sliding,
         this.mounting,
         this.rolling,
         this.sitting,
         this.gliding,
         this.sleeping
      );
   }
}
