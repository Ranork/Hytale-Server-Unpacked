package com.hypixel.hytale.server.core.modules.projectile.config;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.ComponentAccessor;
import com.hypixel.hytale.component.Holder;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.vector.Vector3d;
import com.hypixel.hytale.protocol.PhysicsType;
import com.hypixel.hytale.protocol.RotationMode;
import com.hypixel.hytale.server.core.entity.UUIDComponent;
import com.hypixel.hytale.server.core.modules.entity.component.BoundingBox;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StandardPhysicsConfig implements PhysicsConfig {
   @Nonnull
   public static final BuilderCodec<StandardPhysicsConfig> CODEC = BuilderCodec.builder(StandardPhysicsConfig.class, StandardPhysicsConfig::new)
      .appendInherited(new KeyedCodec<>("Density", Codec.DOUBLE), (o, i) -> o.density = i, o -> o.density, (o, p) -> o.density = p.density)
      .add()
      .appendInherited(new KeyedCodec<>("Gravity", Codec.DOUBLE), (o, i) -> o.gravity = i, o -> o.gravity, (o, p) -> o.gravity = p.gravity)
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("Bounciness", Codec.DOUBLE), (o, i) -> o.bounciness = i, o -> o.bounciness, (o, p) -> o.bounciness = p.bounciness
      )
      .addValidator(Validators.range(0.0, 1.0))
      .add()
      .appendInherited(new KeyedCodec<>("BounceLimit", Codec.DOUBLE), (o, i) -> o.bounceLimit = i, o -> o.bounceLimit, (o, p) -> o.bounceLimit = p.bounceLimit)
      .add()
      .appendInherited(new KeyedCodec<>("BounceCount", Codec.INTEGER), (o, i) -> o.bounceCount = i, o -> o.bounceCount, (o, p) -> o.bounceCount = p.bounceCount)
      .add()
      .appendInherited(
         new KeyedCodec<>("SticksVertically", Codec.BOOLEAN),
         (o, i) -> o.sticksVertically = i,
         o -> o.sticksVertically,
         (o, p) -> o.sticksVertically = p.sticksVertically
      )
      .add()
      .appendInherited(new KeyedCodec<>("ComputeYaw", Codec.BOOLEAN), (o, i) -> o.computeYaw = i, o -> o.computeYaw, (o, p) -> o.computeYaw = p.computeYaw)
      .add()
      .appendInherited(
         new KeyedCodec<>("ComputePitch", Codec.BOOLEAN), (o, i) -> o.computePitch = i, o -> o.computePitch, (o, p) -> o.computePitch = p.computePitch
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("RotationMode", new EnumCodec<>(RotationMode.class)),
         (o, i) -> o.rotationMode = i,
         o -> o.rotationMode,
         (o, p) -> o.rotationMode = p.rotationMode
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("MoveOutOfSolidSpeed", Codec.DOUBLE),
         (o, i) -> o.moveOutOfSolidSpeed = i,
         o -> o.moveOutOfSolidSpeed,
         (o, p) -> o.moveOutOfSolidSpeed = p.moveOutOfSolidSpeed
      )
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("TerminalVelocityAir", Codec.DOUBLE),
         (o, i) -> o.terminalVelocityAir = i,
         o -> o.terminalVelocityAir,
         (o, p) -> o.terminalVelocityAir = p.terminalVelocityAir
      )
      .addValidator(Validators.greaterThan(0.0))
      .add()
      .appendInherited(new KeyedCodec<>("DensityAir", Codec.DOUBLE), (o, i) -> o.densityAir = i, o -> o.densityAir, (o, p) -> o.densityAir = p.densityAir)
      .add()
      .<Double>appendInherited(
         new KeyedCodec<>("TerminalVelocityWater", Codec.DOUBLE),
         (o, i) -> o.terminalVelocityWater = i,
         o -> o.terminalVelocityWater,
         (o, p) -> o.terminalVelocityWater = p.terminalVelocityWater
      )
      .addValidator(Validators.greaterThan(0.0))
      .add()
      .appendInherited(
         new KeyedCodec<>("DensityWater", Codec.DOUBLE), (o, i) -> o.densityWater = i, o -> o.densityWater, (o, p) -> o.densityWater = p.densityWater
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("HitWaterImpulseLoss", Codec.DOUBLE),
         (o, i) -> o.hitWaterImpulseLoss = i,
         o -> o.hitWaterImpulseLoss,
         (o, p) -> o.hitWaterImpulseLoss = p.hitWaterImpulseLoss
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("RotationForce", Codec.DOUBLE), (o, i) -> o.rotationForce = i, o -> o.rotationForce, (o, p) -> o.rotationForce = p.rotationForce
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("SpeedRotationFactor", Codec.FLOAT),
         (o, i) -> o.speedRotationFactor = i,
         o -> o.speedRotationFactor,
         (o, p) -> o.speedRotationFactor = p.speedRotationFactor
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("SwimmingDampingFactor", Codec.DOUBLE),
         (o, i) -> o.swimmingDampingFactor = i,
         o -> o.swimmingDampingFactor,
         (o, p) -> o.swimmingDampingFactor = p.swimmingDampingFactor
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AllowRolling", Codec.BOOLEAN), (o, i) -> o.allowRolling = i, o -> o.allowRolling, (o, p) -> o.allowRolling = p.allowRolling
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("RollingFrictionFactor", Codec.DOUBLE),
         (o, i) -> o.rollingFrictionFactor = i,
         o -> o.rollingFrictionFactor,
         (o, p) -> o.rollingFrictionFactor = p.rollingFrictionFactor
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("RollingSpeed", Codec.FLOAT), (o, i) -> o.rollingSpeed = i, o -> o.rollingSpeed, (o, p) -> o.rollingSpeed = p.rollingSpeed
      )
      .add()
      .build();
   public static final StandardPhysicsConfig DEFAULT = new StandardPhysicsConfig();
   protected static final double HIT_WATER_IMPULSE_LOSS = 0.2;
   protected static final double ROTATION_FORCE = 3.0;
   protected static final float SPEED_ROTATION_FACTOR = 2.0F;
   protected static final double SWIMMING_DAMPING_FACTOR = 1.0;
   protected double density = 700.0;
   protected double gravity;
   protected double bounciness;
   protected int bounceCount = -1;
   protected double bounceLimit = 0.4;
   protected boolean sticksVertically;
   protected boolean computeYaw = true;
   protected boolean computePitch = true;
   @Nonnull
   protected RotationMode rotationMode = RotationMode.VelocityDamped;
   protected boolean allowRolling = false;
   protected double rollingFrictionFactor = 0.99;
   protected float rollingSpeed = 0.1F;
   protected double moveOutOfSolidSpeed;
   protected double terminalVelocityAir = 1.0;
   protected double densityAir = 1.2;
   protected double terminalVelocityWater = 1.0;
   protected double densityWater = 998.0;
   protected double hitWaterImpulseLoss = 0.2;
   protected double rotationForce = 3.0;
   protected float speedRotationFactor = 2.0F;
   protected double swimmingDampingFactor = 1.0;

   @Override
   public double getGravity() {
      return this.gravity;
   }

   @Override
   public void apply(
      @Nonnull Holder<EntityStore> holder,
      @Nullable Ref<EntityStore> creatorRef,
      @Nonnull Vector3d velocity,
      @Nonnull ComponentAccessor<EntityStore> componentAccessor,
      boolean predicted
   ) {
      UUID creatorUUID;
      if (creatorRef != null) {
         UUIDComponent uuidComponent = componentAccessor.getComponent(creatorRef, UUIDComponent.getComponentType());

         assert uuidComponent != null;

         creatorUUID = uuidComponent.getUuid();
      } else {
         creatorUUID = null;
      }

      BoundingBox boundingBoxComponent = holder.getComponent(BoundingBox.getComponentType());

      assert boundingBoxComponent != null;

      holder.addComponent(StandardPhysicsProvider.getComponentType(), new StandardPhysicsProvider(boundingBoxComponent, creatorUUID, this, velocity, predicted));
   }

   @Nonnull
   public com.hypixel.hytale.protocol.PhysicsConfig toPacket() {
      com.hypixel.hytale.protocol.PhysicsConfig packet = new com.hypixel.hytale.protocol.PhysicsConfig();
      packet.type = PhysicsType.Standard;
      packet.density = this.density;
      packet.gravity = this.gravity;
      packet.bounciness = this.bounciness;
      packet.bounceCount = this.bounceCount;
      packet.bounceLimit = this.bounceLimit;
      packet.sticksVertically = this.sticksVertically;
      packet.computeYaw = this.computeYaw;
      packet.computePitch = this.computePitch;
      packet.rotationMode = this.rotationMode;
      packet.moveOutOfSolidSpeed = this.moveOutOfSolidSpeed;
      packet.terminalVelocityAir = this.terminalVelocityAir;
      packet.densityAir = this.densityAir;
      packet.terminalVelocityWater = this.terminalVelocityWater;
      packet.densityWater = this.densityWater;
      packet.hitWaterImpulseLoss = this.hitWaterImpulseLoss;
      packet.rotationForce = this.rotationForce;
      packet.speedRotationFactor = this.speedRotationFactor;
      packet.swimmingDampingFactor = this.swimmingDampingFactor;
      packet.allowRolling = this.allowRolling;
      packet.rollingFrictionFactor = this.rollingFrictionFactor;
      packet.rollingSpeed = this.rollingSpeed;
      return packet;
   }

   public double getBounciness() {
      return this.bounciness;
   }

   public int getBounceCount() {
      return this.bounceCount;
   }

   public double getBounceLimit() {
      return this.bounceLimit;
   }

   public boolean isSticksVertically() {
      return this.sticksVertically;
   }

   public boolean isAllowRolling() {
      return this.allowRolling;
   }

   public double getRollingFrictionFactor() {
      return this.rollingFrictionFactor;
   }

   public double getSwimmingDampingFactor() {
      return this.swimmingDampingFactor;
   }

   public double getHitWaterImpulseLoss() {
      return this.hitWaterImpulseLoss;
   }
}
