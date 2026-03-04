package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class PhysicsConfig {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 122;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 122;
   public static final int MAX_SIZE = 122;
   @Nonnull
   public PhysicsType type = PhysicsType.Standard;
   public double density;
   public double gravity;
   public double bounciness;
   public int bounceCount;
   public double bounceLimit;
   public boolean sticksVertically;
   public boolean computeYaw;
   public boolean computePitch;
   @Nonnull
   public RotationMode rotationMode = RotationMode.None;
   public double moveOutOfSolidSpeed;
   public double terminalVelocityAir;
   public double densityAir;
   public double terminalVelocityWater;
   public double densityWater;
   public double hitWaterImpulseLoss;
   public double rotationForce;
   public float speedRotationFactor;
   public double swimmingDampingFactor;
   public boolean allowRolling;
   public double rollingFrictionFactor;
   public float rollingSpeed;

   public PhysicsConfig() {
   }

   public PhysicsConfig(
      @Nonnull PhysicsType type,
      double density,
      double gravity,
      double bounciness,
      int bounceCount,
      double bounceLimit,
      boolean sticksVertically,
      boolean computeYaw,
      boolean computePitch,
      @Nonnull RotationMode rotationMode,
      double moveOutOfSolidSpeed,
      double terminalVelocityAir,
      double densityAir,
      double terminalVelocityWater,
      double densityWater,
      double hitWaterImpulseLoss,
      double rotationForce,
      float speedRotationFactor,
      double swimmingDampingFactor,
      boolean allowRolling,
      double rollingFrictionFactor,
      float rollingSpeed
   ) {
      this.type = type;
      this.density = density;
      this.gravity = gravity;
      this.bounciness = bounciness;
      this.bounceCount = bounceCount;
      this.bounceLimit = bounceLimit;
      this.sticksVertically = sticksVertically;
      this.computeYaw = computeYaw;
      this.computePitch = computePitch;
      this.rotationMode = rotationMode;
      this.moveOutOfSolidSpeed = moveOutOfSolidSpeed;
      this.terminalVelocityAir = terminalVelocityAir;
      this.densityAir = densityAir;
      this.terminalVelocityWater = terminalVelocityWater;
      this.densityWater = densityWater;
      this.hitWaterImpulseLoss = hitWaterImpulseLoss;
      this.rotationForce = rotationForce;
      this.speedRotationFactor = speedRotationFactor;
      this.swimmingDampingFactor = swimmingDampingFactor;
      this.allowRolling = allowRolling;
      this.rollingFrictionFactor = rollingFrictionFactor;
      this.rollingSpeed = rollingSpeed;
   }

   public PhysicsConfig(@Nonnull PhysicsConfig other) {
      this.type = other.type;
      this.density = other.density;
      this.gravity = other.gravity;
      this.bounciness = other.bounciness;
      this.bounceCount = other.bounceCount;
      this.bounceLimit = other.bounceLimit;
      this.sticksVertically = other.sticksVertically;
      this.computeYaw = other.computeYaw;
      this.computePitch = other.computePitch;
      this.rotationMode = other.rotationMode;
      this.moveOutOfSolidSpeed = other.moveOutOfSolidSpeed;
      this.terminalVelocityAir = other.terminalVelocityAir;
      this.densityAir = other.densityAir;
      this.terminalVelocityWater = other.terminalVelocityWater;
      this.densityWater = other.densityWater;
      this.hitWaterImpulseLoss = other.hitWaterImpulseLoss;
      this.rotationForce = other.rotationForce;
      this.speedRotationFactor = other.speedRotationFactor;
      this.swimmingDampingFactor = other.swimmingDampingFactor;
      this.allowRolling = other.allowRolling;
      this.rollingFrictionFactor = other.rollingFrictionFactor;
      this.rollingSpeed = other.rollingSpeed;
   }

   @Nonnull
   public static PhysicsConfig deserialize(@Nonnull ByteBuf buf, int offset) {
      PhysicsConfig obj = new PhysicsConfig();
      obj.type = PhysicsType.fromValue(buf.getByte(offset + 0));
      obj.density = buf.getDoubleLE(offset + 1);
      obj.gravity = buf.getDoubleLE(offset + 9);
      obj.bounciness = buf.getDoubleLE(offset + 17);
      obj.bounceCount = buf.getIntLE(offset + 25);
      obj.bounceLimit = buf.getDoubleLE(offset + 29);
      obj.sticksVertically = buf.getByte(offset + 37) != 0;
      obj.computeYaw = buf.getByte(offset + 38) != 0;
      obj.computePitch = buf.getByte(offset + 39) != 0;
      obj.rotationMode = RotationMode.fromValue(buf.getByte(offset + 40));
      obj.moveOutOfSolidSpeed = buf.getDoubleLE(offset + 41);
      obj.terminalVelocityAir = buf.getDoubleLE(offset + 49);
      obj.densityAir = buf.getDoubleLE(offset + 57);
      obj.terminalVelocityWater = buf.getDoubleLE(offset + 65);
      obj.densityWater = buf.getDoubleLE(offset + 73);
      obj.hitWaterImpulseLoss = buf.getDoubleLE(offset + 81);
      obj.rotationForce = buf.getDoubleLE(offset + 89);
      obj.speedRotationFactor = buf.getFloatLE(offset + 97);
      obj.swimmingDampingFactor = buf.getDoubleLE(offset + 101);
      obj.allowRolling = buf.getByte(offset + 109) != 0;
      obj.rollingFrictionFactor = buf.getDoubleLE(offset + 110);
      obj.rollingSpeed = buf.getFloatLE(offset + 118);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 122;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeByte(this.type.getValue());
      buf.writeDoubleLE(this.density);
      buf.writeDoubleLE(this.gravity);
      buf.writeDoubleLE(this.bounciness);
      buf.writeIntLE(this.bounceCount);
      buf.writeDoubleLE(this.bounceLimit);
      buf.writeByte(this.sticksVertically ? 1 : 0);
      buf.writeByte(this.computeYaw ? 1 : 0);
      buf.writeByte(this.computePitch ? 1 : 0);
      buf.writeByte(this.rotationMode.getValue());
      buf.writeDoubleLE(this.moveOutOfSolidSpeed);
      buf.writeDoubleLE(this.terminalVelocityAir);
      buf.writeDoubleLE(this.densityAir);
      buf.writeDoubleLE(this.terminalVelocityWater);
      buf.writeDoubleLE(this.densityWater);
      buf.writeDoubleLE(this.hitWaterImpulseLoss);
      buf.writeDoubleLE(this.rotationForce);
      buf.writeFloatLE(this.speedRotationFactor);
      buf.writeDoubleLE(this.swimmingDampingFactor);
      buf.writeByte(this.allowRolling ? 1 : 0);
      buf.writeDoubleLE(this.rollingFrictionFactor);
      buf.writeFloatLE(this.rollingSpeed);
   }

   public int computeSize() {
      return 122;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 122 ? ValidationResult.error("Buffer too small: expected at least 122 bytes") : ValidationResult.OK;
   }

   public PhysicsConfig clone() {
      PhysicsConfig copy = new PhysicsConfig();
      copy.type = this.type;
      copy.density = this.density;
      copy.gravity = this.gravity;
      copy.bounciness = this.bounciness;
      copy.bounceCount = this.bounceCount;
      copy.bounceLimit = this.bounceLimit;
      copy.sticksVertically = this.sticksVertically;
      copy.computeYaw = this.computeYaw;
      copy.computePitch = this.computePitch;
      copy.rotationMode = this.rotationMode;
      copy.moveOutOfSolidSpeed = this.moveOutOfSolidSpeed;
      copy.terminalVelocityAir = this.terminalVelocityAir;
      copy.densityAir = this.densityAir;
      copy.terminalVelocityWater = this.terminalVelocityWater;
      copy.densityWater = this.densityWater;
      copy.hitWaterImpulseLoss = this.hitWaterImpulseLoss;
      copy.rotationForce = this.rotationForce;
      copy.speedRotationFactor = this.speedRotationFactor;
      copy.swimmingDampingFactor = this.swimmingDampingFactor;
      copy.allowRolling = this.allowRolling;
      copy.rollingFrictionFactor = this.rollingFrictionFactor;
      copy.rollingSpeed = this.rollingSpeed;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof PhysicsConfig other)
            ? false
            : Objects.equals(this.type, other.type)
               && this.density == other.density
               && this.gravity == other.gravity
               && this.bounciness == other.bounciness
               && this.bounceCount == other.bounceCount
               && this.bounceLimit == other.bounceLimit
               && this.sticksVertically == other.sticksVertically
               && this.computeYaw == other.computeYaw
               && this.computePitch == other.computePitch
               && Objects.equals(this.rotationMode, other.rotationMode)
               && this.moveOutOfSolidSpeed == other.moveOutOfSolidSpeed
               && this.terminalVelocityAir == other.terminalVelocityAir
               && this.densityAir == other.densityAir
               && this.terminalVelocityWater == other.terminalVelocityWater
               && this.densityWater == other.densityWater
               && this.hitWaterImpulseLoss == other.hitWaterImpulseLoss
               && this.rotationForce == other.rotationForce
               && this.speedRotationFactor == other.speedRotationFactor
               && this.swimmingDampingFactor == other.swimmingDampingFactor
               && this.allowRolling == other.allowRolling
               && this.rollingFrictionFactor == other.rollingFrictionFactor
               && this.rollingSpeed == other.rollingSpeed;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.type,
         this.density,
         this.gravity,
         this.bounciness,
         this.bounceCount,
         this.bounceLimit,
         this.sticksVertically,
         this.computeYaw,
         this.computePitch,
         this.rotationMode,
         this.moveOutOfSolidSpeed,
         this.terminalVelocityAir,
         this.densityAir,
         this.terminalVelocityWater,
         this.densityWater,
         this.hitWaterImpulseLoss,
         this.rotationForce,
         this.speedRotationFactor,
         this.swimmingDampingFactor,
         this.allowRolling,
         this.rollingFrictionFactor,
         this.rollingSpeed
      );
   }
}
