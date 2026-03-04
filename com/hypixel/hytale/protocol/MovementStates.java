package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class MovementStates {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 22;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 22;
   public static final int MAX_SIZE = 22;
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
      obj.climbing = buf.getByte(offset + 10) != 0;
      obj.inFluid = buf.getByte(offset + 11) != 0;
      obj.swimming = buf.getByte(offset + 12) != 0;
      obj.swimJumping = buf.getByte(offset + 13) != 0;
      obj.onGround = buf.getByte(offset + 14) != 0;
      obj.mantling = buf.getByte(offset + 15) != 0;
      obj.sliding = buf.getByte(offset + 16) != 0;
      obj.mounting = buf.getByte(offset + 17) != 0;
      obj.rolling = buf.getByte(offset + 18) != 0;
      obj.sitting = buf.getByte(offset + 19) != 0;
      obj.gliding = buf.getByte(offset + 20) != 0;
      obj.sleeping = buf.getByte(offset + 21) != 0;
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 22;
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

   public int computeSize() {
      return 22;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 22 ? ValidationResult.error("Buffer too small: expected at least 22 bytes") : ValidationResult.OK;
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
