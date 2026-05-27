package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.PacketIO;
import com.hypixel.hytale.protocol.io.ProtocolException;
import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.lang.foreign.MemorySegment;
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
      if (buf.readableBytes() - offset < 122) {
         throw ProtocolException.bufferTooSmall("PhysicsConfig", 122, buf.readableBytes() - offset);
      } else {
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
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 122;
   }

   public static boolean isBufferTooSmall(MemorySegment mem) {
      return mem.byteSize() < 122L;
   }

   public static PhysicsType getType(MemorySegment mem) {
      return getType(mem, 0);
   }

   public static PhysicsType getType(MemorySegment mem, int offset) {
      return PhysicsType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0)));
   }

   public static double getDensity(MemorySegment mem) {
      return getDensity(mem, 0);
   }

   public static double getDensity(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 1));
   }

   public static double getGravity(MemorySegment mem) {
      return getGravity(mem, 0);
   }

   public static double getGravity(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 9));
   }

   public static double getBounciness(MemorySegment mem) {
      return getBounciness(mem, 0);
   }

   public static double getBounciness(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 17));
   }

   public static int getBounceCount(MemorySegment mem) {
      return getBounceCount(mem, 0);
   }

   public static int getBounceCount(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_INT, (long)(offset + 25));
   }

   public static double getBounceLimit(MemorySegment mem) {
      return getBounceLimit(mem, 0);
   }

   public static double getBounceLimit(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 29));
   }

   public static boolean getSticksVertically(MemorySegment mem) {
      return getSticksVertically(mem, 0);
   }

   public static boolean getSticksVertically(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 37));
   }

   public static boolean getComputeYaw(MemorySegment mem) {
      return getComputeYaw(mem, 0);
   }

   public static boolean getComputeYaw(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 38));
   }

   public static boolean getComputePitch(MemorySegment mem) {
      return getComputePitch(mem, 0);
   }

   public static boolean getComputePitch(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 39));
   }

   public static RotationMode getRotationMode(MemorySegment mem) {
      return getRotationMode(mem, 0);
   }

   public static RotationMode getRotationMode(MemorySegment mem, int offset) {
      return RotationMode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 40)));
   }

   public static double getMoveOutOfSolidSpeed(MemorySegment mem) {
      return getMoveOutOfSolidSpeed(mem, 0);
   }

   public static double getMoveOutOfSolidSpeed(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 41));
   }

   public static double getTerminalVelocityAir(MemorySegment mem) {
      return getTerminalVelocityAir(mem, 0);
   }

   public static double getTerminalVelocityAir(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 49));
   }

   public static double getDensityAir(MemorySegment mem) {
      return getDensityAir(mem, 0);
   }

   public static double getDensityAir(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 57));
   }

   public static double getTerminalVelocityWater(MemorySegment mem) {
      return getTerminalVelocityWater(mem, 0);
   }

   public static double getTerminalVelocityWater(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 65));
   }

   public static double getDensityWater(MemorySegment mem) {
      return getDensityWater(mem, 0);
   }

   public static double getDensityWater(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 73));
   }

   public static double getHitWaterImpulseLoss(MemorySegment mem) {
      return getHitWaterImpulseLoss(mem, 0);
   }

   public static double getHitWaterImpulseLoss(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 81));
   }

   public static double getRotationForce(MemorySegment mem) {
      return getRotationForce(mem, 0);
   }

   public static double getRotationForce(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 89));
   }

   public static float getSpeedRotationFactor(MemorySegment mem) {
      return getSpeedRotationFactor(mem, 0);
   }

   public static float getSpeedRotationFactor(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 97));
   }

   public static double getSwimmingDampingFactor(MemorySegment mem) {
      return getSwimmingDampingFactor(mem, 0);
   }

   public static double getSwimmingDampingFactor(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 101));
   }

   public static boolean getAllowRolling(MemorySegment mem) {
      return getAllowRolling(mem, 0);
   }

   public static boolean getAllowRolling(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_BOOL, (long)(offset + 109));
   }

   public static double getRollingFrictionFactor(MemorySegment mem) {
      return getRollingFrictionFactor(mem, 0);
   }

   public static double getRollingFrictionFactor(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 110));
   }

   public static float getRollingSpeed(MemorySegment mem) {
      return getRollingSpeed(mem, 0);
   }

   public static float getRollingSpeed(MemorySegment mem, int offset) {
      return mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 118));
   }

   public static PhysicsConfig toObject(MemorySegment mem) {
      return toObject(mem, 0);
   }

   public static PhysicsConfig toObject(MemorySegment mem, int offset) {
      if (offset + 122 > mem.byteSize()) {
         throw ProtocolException.bufferTooSmall("PhysicsConfig", offset + 122, (int)mem.byteSize());
      } else {
         return new PhysicsConfig(
            PhysicsType.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 0))),
            mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 1)),
            mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 9)),
            mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 17)),
            mem.get(PacketIO.PROTO_INT, (long)(offset + 25)),
            mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 29)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 37)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 38)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 39)),
            RotationMode.fromValue(mem.get(PacketIO.PROTO_BYTE, (long)(offset + 40))),
            mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 41)),
            mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 49)),
            mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 57)),
            mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 65)),
            mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 73)),
            mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 81)),
            mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 89)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 97)),
            mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 101)),
            mem.get(PacketIO.PROTO_BOOL, (long)(offset + 109)),
            mem.get(PacketIO.PROTO_DOUBLE, (long)(offset + 110)),
            mem.get(PacketIO.PROTO_FLOAT, (long)(offset + 118))
         );
      }
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

   public int serialize(@Nonnull MemorySegment mem, int offset) {
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 0), (byte)this.type.getValue());
      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 1), this.density);
      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 9), this.gravity);
      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 17), this.bounciness);
      mem.set(PacketIO.PROTO_INT, (long)(offset + 25), this.bounceCount);
      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 29), this.bounceLimit);
      mem.set(PacketIO.PROTO_BOOL, offset + 37, this.sticksVertically);
      mem.set(PacketIO.PROTO_BOOL, offset + 38, this.computeYaw);
      mem.set(PacketIO.PROTO_BOOL, offset + 39, this.computePitch);
      mem.set(PacketIO.PROTO_BYTE, (long)(offset + 40), (byte)this.rotationMode.getValue());
      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 41), this.moveOutOfSolidSpeed);
      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 49), this.terminalVelocityAir);
      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 57), this.densityAir);
      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 65), this.terminalVelocityWater);
      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 73), this.densityWater);
      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 81), this.hitWaterImpulseLoss);
      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 89), this.rotationForce);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 97), this.speedRotationFactor);
      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 101), this.swimmingDampingFactor);
      mem.set(PacketIO.PROTO_BOOL, offset + 109, this.allowRolling);
      mem.set(PacketIO.PROTO_DOUBLE, (long)(offset + 110), this.rollingFrictionFactor);
      mem.set(PacketIO.PROTO_FLOAT, (long)(offset + 118), this.rollingSpeed);
      return 122;
   }

   public int computeSize() {
      return 122;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      if (buffer.readableBytes() - offset < 122) {
         return ValidationResult.error("Buffer too small: expected at least 122 bytes");
      } else {
         int v = buffer.getByte(offset + 0) & 255;
         if (v >= 1) {
            return ValidationResult.error("Invalid PhysicsType value for Type");
         } else {
            v = buffer.getByte(offset + 40) & 255;
            return v >= 4 ? ValidationResult.error("Invalid RotationMode value for RotationMode") : ValidationResult.OK;
         }
      }
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
