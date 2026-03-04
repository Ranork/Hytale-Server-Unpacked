package com.hypixel.hytale.server.core.entity.entities.player.movement;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.IndexedLookupTableAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.validation.ValidatorCache;
import com.hypixel.hytale.protocol.MovementSettings;
import com.hypixel.hytale.server.core.io.NetworkSerializable;
import javax.annotation.Nonnull;

public class MovementConfig implements JsonAssetWithMap<String, IndexedLookupTableAssetMap<String, MovementConfig>>, NetworkSerializable<MovementSettings> {
   public static final AssetBuilderCodec<String, MovementConfig> CODEC = AssetBuilderCodec.builder(
         MovementConfig.class,
         MovementConfig::new,
         Codec.STRING,
         (movementConfig, s) -> movementConfig.id = s,
         movementConfig -> movementConfig.id,
         (movementConfig, data) -> movementConfig.extraData = data,
         movementConfig -> movementConfig.extraData
      )
      .appendInherited(
         new KeyedCodec<>("VelocityResistance", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.velocityResistance = tasks,
         movementConfig -> movementConfig.velocityResistance,
         (movementConfig, parent) -> movementConfig.velocityResistance = parent.velocityResistance
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("JumpForce", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.jumpForce = tasks,
         movementConfig -> movementConfig.jumpForce,
         (movementConfig, parent) -> movementConfig.jumpForce = parent.jumpForce
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("SwimJumpForce", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.swimJumpForce = tasks,
         movementConfig -> movementConfig.swimJumpForce,
         (movementConfig, parent) -> movementConfig.swimJumpForce = parent.swimJumpForce
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("JumpBufferDuration", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.jumpBufferDuration = tasks,
         movementConfig -> movementConfig.jumpBufferDuration,
         (movementConfig, parent) -> movementConfig.jumpBufferDuration = parent.jumpBufferDuration
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("JumpBufferMaxYVelocity", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.jumpBufferMaxYVelocity = tasks,
         movementConfig -> movementConfig.jumpBufferMaxYVelocity,
         (movementConfig, parent) -> movementConfig.jumpBufferMaxYVelocity = parent.jumpBufferMaxYVelocity
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("Acceleration", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.acceleration = tasks,
         movementConfig -> movementConfig.acceleration,
         (movementConfig, parent) -> movementConfig.acceleration = parent.acceleration
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AirDragMin", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.airDragMin = tasks,
         movementConfig -> movementConfig.airDragMin,
         (movementConfig, parent) -> movementConfig.airDragMin = parent.airDragMin
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AirDragMax", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.airDragMax = tasks,
         movementConfig -> movementConfig.airDragMax,
         (movementConfig, parent) -> movementConfig.airDragMax = parent.airDragMax
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AirDragMinSpeed", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.airDragMinSpeed = tasks,
         movementConfig -> movementConfig.airDragMinSpeed,
         (movementConfig, parent) -> movementConfig.airDragMinSpeed = parent.airDragMinSpeed
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AirDragMaxSpeed", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.airDragMaxSpeed = tasks,
         movementConfig -> movementConfig.airDragMaxSpeed,
         (movementConfig, parent) -> movementConfig.airDragMaxSpeed = parent.airDragMaxSpeed
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AirFrictionMin", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.airFrictionMin = tasks,
         movementConfig -> movementConfig.airFrictionMin,
         (movementConfig, parent) -> movementConfig.airFrictionMin = parent.airFrictionMin
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AirFrictionMax", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.airFrictionMax = tasks,
         movementConfig -> movementConfig.airFrictionMax,
         (movementConfig, parent) -> movementConfig.airFrictionMax = parent.airFrictionMax
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AirFrictionMinSpeed", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.airFrictionMinSpeed = tasks,
         movementConfig -> movementConfig.airFrictionMinSpeed,
         (movementConfig, parent) -> movementConfig.airFrictionMinSpeed = parent.airFrictionMinSpeed
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AirFrictionMaxSpeed", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.airFrictionMaxSpeed = tasks,
         movementConfig -> movementConfig.airFrictionMaxSpeed,
         (movementConfig, parent) -> movementConfig.airFrictionMaxSpeed = parent.airFrictionMaxSpeed
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AirSpeedMultiplier", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.airSpeedMultiplier = tasks,
         movementConfig -> movementConfig.airSpeedMultiplier,
         (movementConfig, parent) -> movementConfig.airSpeedMultiplier = parent.airSpeedMultiplier
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AirControlMinSpeed", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.airControlMinSpeed = tasks,
         movementConfig -> movementConfig.airControlMinSpeed,
         (movementConfig, parent) -> movementConfig.airControlMinSpeed = parent.airControlMinSpeed
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AirControlMaxSpeed", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.airControlMaxSpeed = tasks,
         movementConfig -> movementConfig.airControlMaxSpeed,
         (movementConfig, parent) -> movementConfig.airControlMaxSpeed = parent.airControlMaxSpeed
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AirControlMinMultiplier", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.airControlMinMultiplier = tasks,
         movementConfig -> movementConfig.airControlMinMultiplier,
         (movementConfig, parent) -> movementConfig.airControlMinMultiplier = parent.airControlMinMultiplier
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AirControlMaxMultiplier", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.airControlMaxMultiplier = tasks,
         movementConfig -> movementConfig.airControlMaxMultiplier,
         (movementConfig, parent) -> movementConfig.airControlMaxMultiplier = parent.airControlMaxMultiplier
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ComboAirSpeedMultiplier", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.comboAirSpeedMultiplier = tasks,
         movementConfig -> movementConfig.comboAirSpeedMultiplier,
         (movementConfig, parent) -> movementConfig.comboAirSpeedMultiplier = parent.comboAirSpeedMultiplier
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("BaseSpeed", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.baseSpeed = tasks,
         movementConfig -> movementConfig.baseSpeed,
         (movementConfig, parent) -> movementConfig.baseSpeed = parent.baseSpeed
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ClimbSpeed", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.climbSpeed = tasks,
         movementConfig -> movementConfig.climbSpeed,
         (movementConfig, parent) -> movementConfig.climbSpeed = parent.climbSpeed
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ClimbSpeedLateral", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.climbSpeedLateral = tasks,
         movementConfig -> movementConfig.climbSpeedLateral,
         (movementConfig, parent) -> movementConfig.climbSpeedLateral = parent.climbSpeedLateral
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ClimbUpSprintSpeed", Codec.FLOAT),
         (movementConfig, aFloat) -> movementConfig.climbUpSprintSpeed = aFloat,
         movementConfig -> movementConfig.climbUpSprintSpeed,
         (movementConfig, parent) -> movementConfig.climbUpSprintSpeed = parent.climbUpSprintSpeed
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ClimbDownSprintSpeed", Codec.FLOAT),
         (movementConfig, aFloat) -> movementConfig.climbDownSprintSpeed = aFloat,
         movementConfig -> movementConfig.climbDownSprintSpeed,
         (movementConfig, parent) -> movementConfig.climbDownSprintSpeed = parent.climbDownSprintSpeed
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("HorizontalFlySpeed", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.horizontalFlySpeed = tasks,
         movementConfig -> movementConfig.horizontalFlySpeed,
         (movementConfig, parent) -> movementConfig.horizontalFlySpeed = parent.horizontalFlySpeed
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("VerticalFlySpeed", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.verticalFlySpeed = tasks,
         movementConfig -> movementConfig.verticalFlySpeed,
         (movementConfig, parent) -> movementConfig.verticalFlySpeed = parent.verticalFlySpeed
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("MaxSpeedMultiplier", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.maxSpeedMultiplier = tasks,
         movementConfig -> movementConfig.maxSpeedMultiplier,
         (movementConfig, parent) -> movementConfig.maxSpeedMultiplier = parent.maxSpeedMultiplier
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("MinSpeedMultiplier", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.minSpeedMultiplier = tasks,
         movementConfig -> movementConfig.minSpeedMultiplier,
         (movementConfig, parent) -> movementConfig.minSpeedMultiplier = parent.minSpeedMultiplier
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("WishDirectionGravityX", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.wishDirectionGravityX = tasks,
         movementConfig -> movementConfig.wishDirectionGravityX,
         (movementConfig, parent) -> movementConfig.wishDirectionGravityX = parent.wishDirectionGravityX
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("WishDirectionGravityY", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.wishDirectionGravityY = tasks,
         movementConfig -> movementConfig.wishDirectionGravityY,
         (movementConfig, parent) -> movementConfig.wishDirectionGravityY = parent.wishDirectionGravityY
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("WishDirectionWeightX", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.wishDirectionWeightX = tasks,
         movementConfig -> movementConfig.wishDirectionWeightX,
         (movementConfig, parent) -> movementConfig.wishDirectionWeightX = parent.wishDirectionWeightX
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("WishDirectionWeightY", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.wishDirectionWeightY = tasks,
         movementConfig -> movementConfig.wishDirectionWeightY,
         (movementConfig, parent) -> movementConfig.wishDirectionWeightY = parent.wishDirectionWeightY
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("CollisionExpulsionForce", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.collisionExpulsionForce = tasks,
         movementConfig -> movementConfig.collisionExpulsionForce,
         (movementConfig, parent) -> movementConfig.collisionExpulsionForce = parent.collisionExpulsionForce
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ForwardWalkSpeedMultiplier", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.forwardWalkSpeedMultiplier = tasks,
         movementConfig -> movementConfig.forwardWalkSpeedMultiplier,
         (movementConfig, parent) -> movementConfig.forwardWalkSpeedMultiplier = parent.forwardWalkSpeedMultiplier
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("BackwardWalkSpeedMultiplier", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.backwardWalkSpeedMultiplier = tasks,
         movementConfig -> movementConfig.backwardWalkSpeedMultiplier,
         (movementConfig, parent) -> movementConfig.backwardWalkSpeedMultiplier = parent.backwardWalkSpeedMultiplier
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("StrafeWalkSpeedMultiplier", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.strafeWalkSpeedMultiplier = tasks,
         movementConfig -> movementConfig.strafeWalkSpeedMultiplier,
         (movementConfig, parent) -> movementConfig.strafeWalkSpeedMultiplier = parent.strafeWalkSpeedMultiplier
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ForwardRunSpeedMultiplier", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.forwardRunSpeedMultiplier = tasks,
         movementConfig -> movementConfig.forwardRunSpeedMultiplier,
         (movementConfig, parent) -> movementConfig.forwardRunSpeedMultiplier = parent.forwardRunSpeedMultiplier
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("BackwardRunSpeedMultiplier", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.backwardRunSpeedMultiplier = tasks,
         movementConfig -> movementConfig.backwardRunSpeedMultiplier,
         (movementConfig, parent) -> movementConfig.backwardRunSpeedMultiplier = parent.backwardRunSpeedMultiplier
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("StrafeRunSpeedMultiplier", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.strafeRunSpeedMultiplier = tasks,
         movementConfig -> movementConfig.strafeRunSpeedMultiplier,
         (movementConfig, parent) -> movementConfig.strafeRunSpeedMultiplier = parent.strafeRunSpeedMultiplier
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ForwardCrouchSpeedMultiplier", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.forwardCrouchSpeedMultiplier = tasks,
         movementConfig -> movementConfig.forwardCrouchSpeedMultiplier,
         (movementConfig, parent) -> movementConfig.forwardCrouchSpeedMultiplier = parent.forwardCrouchSpeedMultiplier
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("BackwardCrouchSpeedMultiplier", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.backwardCrouchSpeedMultiplier = tasks,
         movementConfig -> movementConfig.backwardCrouchSpeedMultiplier,
         (movementConfig, parent) -> movementConfig.backwardCrouchSpeedMultiplier = parent.backwardCrouchSpeedMultiplier
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("StrafeCrouchSpeedMultiplier", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.strafeCrouchSpeedMultiplier = tasks,
         movementConfig -> movementConfig.strafeCrouchSpeedMultiplier,
         (movementConfig, parent) -> movementConfig.strafeCrouchSpeedMultiplier = parent.strafeCrouchSpeedMultiplier
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("ForwardSprintSpeedMultiplier", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.forwardSprintSpeedMultiplier = tasks,
         movementConfig -> movementConfig.forwardSprintSpeedMultiplier,
         (movementConfig, parent) -> movementConfig.forwardSprintSpeedMultiplier = parent.forwardSprintSpeedMultiplier
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("VariableJumpFallForce", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.variableJumpFallForce = tasks,
         movementConfig -> movementConfig.variableJumpFallForce,
         (movementConfig, parent) -> movementConfig.variableJumpFallForce = parent.variableJumpFallForce
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("FallEffectDuration", Codec.FLOAT),
         (movementConfig, aFloat) -> movementConfig.fallEffectDuration = aFloat,
         movementConfig -> movementConfig.fallEffectDuration,
         (movementConfig, parent) -> movementConfig.fallEffectDuration = parent.fallEffectDuration
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("FallJumpForce", Codec.FLOAT),
         (movementConfig, aFloat) -> movementConfig.fallJumpForce = aFloat,
         movementConfig -> movementConfig.fallJumpForce,
         (movementConfig, parent) -> movementConfig.fallJumpForce = parent.fallJumpForce
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("FallMomentumLoss", Codec.FLOAT),
         (movementConfig, aFloat) -> movementConfig.fallMomentumLoss = aFloat,
         movementConfig -> movementConfig.fallMomentumLoss,
         (movementConfig, parent) -> movementConfig.fallMomentumLoss = parent.fallMomentumLoss
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AutoJumpObstacleSpeedLoss", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.autoJumpObstacleSpeedLoss = tasks,
         movementConfig -> movementConfig.autoJumpObstacleSpeedLoss,
         (movementConfig, parent) -> movementConfig.autoJumpObstacleSpeedLoss = parent.autoJumpObstacleSpeedLoss
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AutoJumpObstacleSprintSpeedLoss", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.autoJumpObstacleSprintSpeedLoss = tasks,
         movementConfig -> movementConfig.autoJumpObstacleSprintSpeedLoss,
         (movementConfig, parent) -> movementConfig.autoJumpObstacleSprintSpeedLoss = parent.autoJumpObstacleSprintSpeedLoss
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AutoJumpObstacleEffectDuration", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.autoJumpObstacleEffectDuration = tasks,
         movementConfig -> movementConfig.autoJumpObstacleEffectDuration,
         (movementConfig, parent) -> movementConfig.autoJumpObstacleEffectDuration = parent.autoJumpObstacleEffectDuration
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AutoJumpObstacleSprintEffectDuration", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.autoJumpObstacleSprintEffectDuration = tasks,
         movementConfig -> movementConfig.autoJumpObstacleSprintEffectDuration,
         (movementConfig, parent) -> movementConfig.autoJumpObstacleSprintEffectDuration = parent.autoJumpObstacleSprintEffectDuration
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AutoJumpObstacleMaxAngle", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.autoJumpObstacleMaxAngle = tasks,
         movementConfig -> movementConfig.autoJumpObstacleMaxAngle,
         (movementConfig, parent) -> movementConfig.autoJumpObstacleMaxAngle = parent.autoJumpObstacleMaxAngle
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("AutoJumpDisableJumping", Codec.BOOLEAN),
         (movementConfig, tasks) -> movementConfig.autoJumpDisableJumping = tasks,
         movementConfig -> movementConfig.autoJumpDisableJumping,
         (movementConfig, parent) -> movementConfig.autoJumpDisableJumping = parent.autoJumpDisableJumping
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("MinSlideEntrySpeed", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.minSlideEntrySpeed = tasks,
         movementConfig -> movementConfig.minSlideEntrySpeed,
         (movementConfig, parent) -> movementConfig.minSlideEntrySpeed = parent.minSlideEntrySpeed
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("SlideExitSpeed", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.slideExitSpeed = tasks,
         movementConfig -> movementConfig.slideExitSpeed,
         (movementConfig, parent) -> movementConfig.slideExitSpeed = parent.slideExitSpeed
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("MinFallSpeedToEngageRoll", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.minFallSpeedToEngageRoll = tasks,
         movementConfig -> movementConfig.minFallSpeedToEngageRoll,
         (movementConfig, parent) -> movementConfig.minFallSpeedToEngageRoll = parent.minFallSpeedToEngageRoll
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("MaxFallSpeedToEngageRoll", Codec.FLOAT),
         (movementConfig, value) -> movementConfig.maxFallSpeedToEngageRoll = value,
         movementConfig -> movementConfig.maxFallSpeedToEngageRoll,
         (movementConfig, parent) -> movementConfig.maxFallSpeedToEngageRoll = parent.maxFallSpeedToEngageRoll
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("FallDamagePartialMitigationPercent", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.fallDamagePartialMitigationPercent = tasks,
         movementConfig -> movementConfig.fallDamagePartialMitigationPercent,
         (movementConfig, parent) -> movementConfig.fallDamagePartialMitigationPercent = parent.fallDamagePartialMitigationPercent
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("MaxFallSpeedRollFullMitigation", Codec.FLOAT),
         (movementConfig, value) -> movementConfig.maxFallSpeedRollFullMitigation = value,
         movementConfig -> movementConfig.maxFallSpeedRollFullMitigation,
         (movementConfig, parent) -> movementConfig.maxFallSpeedRollFullMitigation = parent.maxFallSpeedRollFullMitigation
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("RollStartSpeedModifier", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.rollStartSpeedModifier = tasks,
         movementConfig -> movementConfig.rollStartSpeedModifier,
         (movementConfig, parent) -> movementConfig.rollStartSpeedModifier = parent.rollStartSpeedModifier
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("RollExitSpeedModifier", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.rollExitSpeedModifier = tasks,
         movementConfig -> movementConfig.rollExitSpeedModifier,
         (movementConfig, parent) -> movementConfig.rollExitSpeedModifier = parent.rollExitSpeedModifier
      )
      .add()
      .appendInherited(
         new KeyedCodec<>("RollTimeToComplete", Codec.FLOAT),
         (movementConfig, tasks) -> movementConfig.rollTimeToComplete = tasks,
         movementConfig -> movementConfig.rollTimeToComplete,
         (movementConfig, parent) -> movementConfig.rollTimeToComplete = parent.rollTimeToComplete
      )
      .add()
      .build();
   public static final ValidatorCache<String> VALIDATOR_CACHE = new ValidatorCache<>(new AssetKeyValidator<>(MovementConfig::getAssetStore));
   private static AssetStore<String, MovementConfig, IndexedLookupTableAssetMap<String, MovementConfig>> ASSET_STORE;
   public static final int DEFAULT_INDEX = 0;
   public static final String DEFAULT_ID = "BuiltinDefault";
   public static final MovementConfig DEFAULT_MOVEMENT = new MovementConfig("BuiltinDefault") {
      {
         this.velocityResistance = 0.242F;
         this.jumpForce = 11.8F;
         this.swimJumpForce = 10.0F;
         this.jumpBufferDuration = 0.3F;
         this.jumpBufferMaxYVelocity = 3.0F;
         this.acceleration = 0.1F;
         this.airDragMin = 0.96F;
         this.airDragMax = 0.995F;
         this.airDragMinSpeed = 6.0F;
         this.airDragMaxSpeed = 10.0F;
         this.airFrictionMin = 0.02F;
         this.airFrictionMax = 0.045F;
         this.airFrictionMinSpeed = 6.0F;
         this.airFrictionMaxSpeed = 10.0F;
         this.airSpeedMultiplier = 1.0F;
         this.airControlMinSpeed = 0.0F;
         this.airControlMaxSpeed = 3.0F;
         this.airControlMinMultiplier = 0.0F;
         this.airControlMaxMultiplier = 3.13F;
         this.comboAirSpeedMultiplier = 1.05F;
         this.baseSpeed = 5.5F;
         this.climbSpeed = 0.035F;
         this.climbSpeedLateral = 0.035F;
         this.climbUpSprintSpeed = 0.5F;
         this.climbDownSprintSpeed = 0.6F;
         this.horizontalFlySpeed = 10.32F;
         this.verticalFlySpeed = 10.32F;
         this.maxSpeedMultiplier = 1000.0F;
         this.minSpeedMultiplier = 0.01F;
         this.wishDirectionGravityX = 0.5F;
         this.wishDirectionGravityY = 0.5F;
         this.wishDirectionWeightX = 0.5F;
         this.wishDirectionWeightY = 0.5F;
         this.collisionExpulsionForce = 0.04F;
         this.forwardWalkSpeedMultiplier = 0.3F;
         this.backwardWalkSpeedMultiplier = 0.3F;
         this.strafeWalkSpeedMultiplier = 0.3F;
         this.forwardRunSpeedMultiplier = 1.0F;
         this.backwardRunSpeedMultiplier = 0.65F;
         this.strafeRunSpeedMultiplier = 0.8F;
         this.forwardCrouchSpeedMultiplier = 0.55F;
         this.backwardCrouchSpeedMultiplier = 0.4F;
         this.strafeCrouchSpeedMultiplier = 0.45F;
         this.forwardSprintSpeedMultiplier = 1.65F;
         this.variableJumpFallForce = 35.0F;
         this.fallEffectDuration = 0.6F;
         this.fallJumpForce = 7.0F;
         this.fallMomentumLoss = 0.1F;
         this.autoJumpObstacleSpeedLoss = 0.95F;
         this.autoJumpObstacleSprintSpeedLoss = 0.75F;
         this.autoJumpObstacleEffectDuration = 0.2F;
         this.autoJumpObstacleSprintEffectDuration = 0.1F;
         this.autoJumpObstacleMaxAngle = 45.0F;
         this.autoJumpDisableJumping = true;
         this.minSlideEntrySpeed = 8.5F;
         this.slideExitSpeed = 2.5F;
         this.minFallSpeedToEngageRoll = 21.0F;
         this.maxFallSpeedToEngageRoll = 31.0F;
         this.fallDamagePartialMitigationPercent = 33.0F;
         this.maxFallSpeedRollFullMitigation = 25.0F;
         this.rollStartSpeedModifier = 2.5F;
         this.rollExitSpeedModifier = 1.5F;
         this.rollTimeToComplete = 0.9F;
      }
   };
   protected AssetExtraInfo.Data extraData;
   protected String id;
   protected float velocityResistance;
   protected float jumpForce;
   protected float swimJumpForce;
   protected float jumpBufferDuration;
   protected float jumpBufferMaxYVelocity;
   protected float acceleration;
   protected float airDragMin;
   protected float airDragMax;
   protected float airDragMinSpeed;
   protected float airDragMaxSpeed;
   protected float airFrictionMin;
   protected float airFrictionMax;
   protected float airFrictionMinSpeed;
   protected float airFrictionMaxSpeed;
   protected float airSpeedMultiplier;
   protected float airControlMinSpeed;
   protected float airControlMaxSpeed;
   protected float airControlMinMultiplier;
   protected float airControlMaxMultiplier;
   protected float comboAirSpeedMultiplier;
   protected float baseSpeed;
   protected float climbSpeed;
   protected float climbSpeedLateral;
   protected float climbUpSprintSpeed;
   protected float climbDownSprintSpeed;
   protected float horizontalFlySpeed;
   protected float verticalFlySpeed;
   protected float maxSpeedMultiplier;
   protected float minSpeedMultiplier;
   protected float wishDirectionGravityX;
   protected float wishDirectionGravityY;
   protected float wishDirectionWeightX;
   protected float wishDirectionWeightY;
   protected float collisionExpulsionForce;
   protected float forwardWalkSpeedMultiplier;
   protected float backwardWalkSpeedMultiplier;
   protected float strafeWalkSpeedMultiplier;
   protected float forwardRunSpeedMultiplier;
   protected float backwardRunSpeedMultiplier;
   protected float strafeRunSpeedMultiplier;
   protected float forwardCrouchSpeedMultiplier;
   protected float backwardCrouchSpeedMultiplier;
   protected float strafeCrouchSpeedMultiplier;
   protected float forwardSprintSpeedMultiplier;
   protected float variableJumpFallForce;
   protected float fallEffectDuration;
   protected float fallJumpForce;
   protected float fallMomentumLoss;
   protected float autoJumpObstacleSpeedLoss;
   protected float autoJumpObstacleSprintSpeedLoss;
   protected float autoJumpObstacleEffectDuration;
   protected float autoJumpObstacleSprintEffectDuration;
   protected float autoJumpObstacleMaxAngle;
   protected boolean autoJumpDisableJumping;
   protected float minSlideEntrySpeed;
   protected float slideExitSpeed;
   protected float minFallSpeedToEngageRoll;
   protected float maxFallSpeedToEngageRoll;
   protected float fallDamagePartialMitigationPercent;
   protected float maxFallSpeedRollFullMitigation;
   protected float rollStartSpeedModifier;
   protected float rollExitSpeedModifier;
   protected float rollTimeToComplete;

   public static AssetStore<String, MovementConfig, IndexedLookupTableAssetMap<String, MovementConfig>> getAssetStore() {
      if (ASSET_STORE == null) {
         ASSET_STORE = AssetRegistry.getAssetStore(MovementConfig.class);
      }

      return ASSET_STORE;
   }

   public static IndexedLookupTableAssetMap<String, MovementConfig> getAssetMap() {
      return (IndexedLookupTableAssetMap<String, MovementConfig>)getAssetStore().getAssetMap();
   }

   public MovementConfig(@Nonnull MovementConfig movementConfig) {
      this.id = movementConfig.id;
      this.velocityResistance = movementConfig.velocityResistance;
      this.jumpForce = movementConfig.jumpForce;
      this.swimJumpForce = movementConfig.swimJumpForce;
      this.jumpBufferDuration = movementConfig.jumpBufferDuration;
      this.jumpBufferMaxYVelocity = movementConfig.jumpBufferMaxYVelocity;
      this.acceleration = movementConfig.acceleration;
      this.airDragMin = movementConfig.airDragMin;
      this.airDragMax = movementConfig.airDragMax;
      this.airDragMinSpeed = movementConfig.airDragMinSpeed;
      this.airDragMaxSpeed = movementConfig.airDragMaxSpeed;
      this.airFrictionMin = movementConfig.airFrictionMin;
      this.airFrictionMax = movementConfig.airFrictionMax;
      this.airFrictionMinSpeed = movementConfig.airFrictionMinSpeed;
      this.airFrictionMaxSpeed = movementConfig.airFrictionMaxSpeed;
      this.airSpeedMultiplier = movementConfig.airSpeedMultiplier;
      this.airControlMinSpeed = movementConfig.airControlMinSpeed;
      this.airControlMaxSpeed = movementConfig.airControlMaxSpeed;
      this.airControlMinMultiplier = movementConfig.airControlMinMultiplier;
      this.airControlMaxMultiplier = movementConfig.airControlMaxMultiplier;
      this.comboAirSpeedMultiplier = movementConfig.airSpeedMultiplier;
      this.baseSpeed = movementConfig.baseSpeed;
      this.climbSpeed = movementConfig.climbSpeed;
      this.climbSpeedLateral = movementConfig.climbSpeedLateral;
      this.climbUpSprintSpeed = movementConfig.climbUpSprintSpeed;
      this.climbDownSprintSpeed = movementConfig.climbDownSprintSpeed;
      this.horizontalFlySpeed = movementConfig.horizontalFlySpeed;
      this.verticalFlySpeed = movementConfig.verticalFlySpeed;
      this.maxSpeedMultiplier = movementConfig.maxSpeedMultiplier;
      this.minSpeedMultiplier = movementConfig.minSpeedMultiplier;
      this.wishDirectionGravityX = movementConfig.wishDirectionGravityX;
      this.wishDirectionGravityY = movementConfig.wishDirectionGravityY;
      this.wishDirectionWeightX = movementConfig.wishDirectionWeightX;
      this.wishDirectionWeightY = movementConfig.wishDirectionWeightY;
      this.collisionExpulsionForce = movementConfig.collisionExpulsionForce;
      this.forwardWalkSpeedMultiplier = movementConfig.forwardWalkSpeedMultiplier;
      this.backwardWalkSpeedMultiplier = movementConfig.backwardWalkSpeedMultiplier;
      this.strafeWalkSpeedMultiplier = movementConfig.strafeWalkSpeedMultiplier;
      this.forwardRunSpeedMultiplier = movementConfig.forwardRunSpeedMultiplier;
      this.backwardRunSpeedMultiplier = movementConfig.backwardRunSpeedMultiplier;
      this.strafeRunSpeedMultiplier = movementConfig.strafeRunSpeedMultiplier;
      this.forwardCrouchSpeedMultiplier = movementConfig.forwardCrouchSpeedMultiplier;
      this.backwardCrouchSpeedMultiplier = movementConfig.backwardCrouchSpeedMultiplier;
      this.strafeCrouchSpeedMultiplier = movementConfig.strafeCrouchSpeedMultiplier;
      this.forwardSprintSpeedMultiplier = movementConfig.forwardSprintSpeedMultiplier;
      this.variableJumpFallForce = movementConfig.variableJumpFallForce;
      this.autoJumpObstacleSpeedLoss = movementConfig.autoJumpObstacleSpeedLoss;
      this.autoJumpObstacleSprintSpeedLoss = movementConfig.autoJumpObstacleSprintSpeedLoss;
      this.autoJumpObstacleEffectDuration = movementConfig.autoJumpObstacleEffectDuration;
      this.autoJumpObstacleSprintEffectDuration = movementConfig.autoJumpObstacleSprintEffectDuration;
      this.autoJumpObstacleMaxAngle = movementConfig.autoJumpObstacleMaxAngle;
      this.autoJumpDisableJumping = movementConfig.autoJumpDisableJumping;
      this.minSlideEntrySpeed = movementConfig.minSlideEntrySpeed;
      this.slideExitSpeed = movementConfig.slideExitSpeed;
      this.minFallSpeedToEngageRoll = movementConfig.minFallSpeedToEngageRoll;
      this.maxFallSpeedToEngageRoll = movementConfig.maxFallSpeedToEngageRoll;
      this.fallDamagePartialMitigationPercent = movementConfig.fallDamagePartialMitigationPercent;
      this.maxFallSpeedRollFullMitigation = movementConfig.maxFallSpeedRollFullMitigation;
      this.rollStartSpeedModifier = movementConfig.rollStartSpeedModifier;
      this.rollExitSpeedModifier = movementConfig.rollExitSpeedModifier;
      this.rollTimeToComplete = movementConfig.rollTimeToComplete;
   }

   public MovementConfig(String id) {
      this.id = id;
   }

   protected MovementConfig() {
   }

   public String getId() {
      return this.id;
   }

   public AssetExtraInfo.Data getExtraData() {
      return this.extraData;
   }

   public float getVelocityResistance() {
      return this.velocityResistance;
   }

   public float getJumpForce() {
      return this.jumpForce;
   }

   public float getSwimJumpForce() {
      return this.swimJumpForce;
   }

   public float getJumpBufferDuration() {
      return this.jumpBufferDuration;
   }

   public float getJumpBufferMaxYVelocity() {
      return this.jumpBufferMaxYVelocity;
   }

   public float getAcceleration() {
      return this.acceleration;
   }

   public float getAirDragMin() {
      return this.airDragMin;
   }

   public float getAirDragMax() {
      return this.airDragMax;
   }

   public float getAirDragMinSpeed() {
      return this.airDragMinSpeed;
   }

   public float getAirDragMaxSpeed() {
      return this.airDragMaxSpeed;
   }

   public float getAirFrictionMin() {
      return this.airFrictionMin;
   }

   public float getAirFrictionMax() {
      return this.airFrictionMax;
   }

   public float getAirFrictionMinSpeed() {
      return this.airFrictionMinSpeed;
   }

   public float getAirFrictionMaxSpeed() {
      return this.airFrictionMaxSpeed;
   }

   public float getAirSpeedMultiplier() {
      return this.airSpeedMultiplier;
   }

   public float getAirControlMinSpeed() {
      return this.airControlMinSpeed;
   }

   public float getAirControlMaxSpeed() {
      return this.airControlMaxSpeed;
   }

   public float getAirControlMinMultiplier() {
      return this.airControlMinMultiplier;
   }

   public float getAirControlMaxMultiplier() {
      return this.airControlMaxMultiplier;
   }

   public float getComboAirSpeedMultiplier() {
      return this.comboAirSpeedMultiplier;
   }

   public float getBaseSpeed() {
      return this.baseSpeed;
   }

   public float getClimbSpeed() {
      return this.climbSpeed;
   }

   public float getClimbSpeedLateral() {
      return this.climbSpeedLateral;
   }

   public float getClimbUpSprintSpeed() {
      return this.climbUpSprintSpeed;
   }

   public float getClimbDownSprintSpeed() {
      return this.climbDownSprintSpeed;
   }

   public float getHorizontalFlySpeed() {
      return this.horizontalFlySpeed;
   }

   public float getVerticalFlySpeed() {
      return this.verticalFlySpeed;
   }

   public float getMaxSpeedMultiplier() {
      return this.maxSpeedMultiplier;
   }

   public float getMinSpeedMultiplier() {
      return this.minSpeedMultiplier;
   }

   public float getWishDirectionGravityX() {
      return this.wishDirectionGravityX;
   }

   public float getWishDirectionGravityY() {
      return this.wishDirectionGravityY;
   }

   public float getWishDirectionWeightX() {
      return this.wishDirectionWeightX;
   }

   public float getWishDirectionWeightY() {
      return this.wishDirectionWeightY;
   }

   public float getCollisionExpulsionForce() {
      return this.collisionExpulsionForce;
   }

   public float getForwardWalkSpeedMultiplier() {
      return this.forwardWalkSpeedMultiplier;
   }

   public float getBackwardWalkSpeedMultiplier() {
      return this.backwardWalkSpeedMultiplier;
   }

   public float getStrafeWalkSpeedMultiplier() {
      return this.strafeWalkSpeedMultiplier;
   }

   public float getForwardRunSpeedMultiplier() {
      return this.forwardRunSpeedMultiplier;
   }

   public float getBackwardRunSpeedMultiplier() {
      return this.backwardRunSpeedMultiplier;
   }

   public float getStrafeRunSpeedMultiplier() {
      return this.strafeRunSpeedMultiplier;
   }

   public float getForwardCrouchSpeedMultiplier() {
      return this.forwardCrouchSpeedMultiplier;
   }

   public float getBackwardCrouchSpeedMultiplier() {
      return this.backwardCrouchSpeedMultiplier;
   }

   public float getStrafeCrouchSpeedMultiplier() {
      return this.strafeCrouchSpeedMultiplier;
   }

   public float getForwardSprintSpeedMultiplier() {
      return this.forwardSprintSpeedMultiplier;
   }

   public float getVariableJumpFallForce() {
      return this.variableJumpFallForce;
   }

   public float getFallEffectDuration() {
      return this.fallEffectDuration;
   }

   public float getFallJumpForce() {
      return this.fallJumpForce;
   }

   public float getFallMomentumLoss() {
      return this.fallMomentumLoss;
   }

   public float getAutoJumpObstacleSpeedLoss() {
      return this.autoJumpObstacleSpeedLoss;
   }

   public float getAutoJumpObstacleSprintSpeedLoss() {
      return this.autoJumpObstacleSprintSpeedLoss;
   }

   public float getAutoJumpObstacleEffectDuration() {
      return this.autoJumpObstacleEffectDuration;
   }

   public float getAutoJumpObstacleSprintEffectDuration() {
      return this.autoJumpObstacleSprintEffectDuration;
   }

   public float getAutoJumpObstacleMaxAngle() {
      return this.autoJumpObstacleMaxAngle;
   }

   public boolean isAutoJumpDisableJumping() {
      return this.autoJumpDisableJumping;
   }

   public float getMinFallSpeedToEngageRoll() {
      return this.minFallSpeedToEngageRoll;
   }

   public float getMaxFallSpeedToEngageRoll() {
      return this.maxFallSpeedToEngageRoll;
   }

   public float getFallDamagePartialMitigationPercent() {
      return this.fallDamagePartialMitigationPercent;
   }

   public float getMaxFallSpeedRollFullMitigation() {
      return this.maxFallSpeedRollFullMitigation;
   }

   public float getRollStartSpeedModifier() {
      return this.rollStartSpeedModifier;
   }

   public float getRollExitSpeedModifier() {
      return this.rollExitSpeedModifier;
   }

   public float getRollTimeToComplete() {
      return this.rollTimeToComplete;
   }

   @Nonnull
   public MovementSettings toPacket() {
      MovementSettings packet = new MovementSettings();
      packet.velocityResistance = this.velocityResistance;
      packet.jumpForce = this.jumpForce;
      packet.swimJumpForce = this.swimJumpForce;
      packet.jumpBufferDuration = this.jumpBufferDuration;
      packet.jumpBufferMaxYVelocity = this.jumpBufferMaxYVelocity;
      packet.acceleration = this.acceleration;
      packet.airDragMin = this.airDragMin;
      packet.airDragMax = this.airDragMax;
      packet.airDragMinSpeed = this.airDragMinSpeed;
      packet.airDragMaxSpeed = this.airDragMaxSpeed;
      packet.airFrictionMin = this.airFrictionMin;
      packet.airFrictionMax = this.airFrictionMax;
      packet.airFrictionMinSpeed = this.airFrictionMinSpeed;
      packet.airFrictionMaxSpeed = this.airFrictionMaxSpeed;
      packet.airSpeedMultiplier = this.airSpeedMultiplier;
      packet.airControlMinSpeed = this.airControlMinSpeed;
      packet.airControlMaxSpeed = this.airControlMaxSpeed;
      packet.airControlMinMultiplier = this.airControlMinMultiplier;
      packet.airControlMaxMultiplier = this.airControlMaxMultiplier;
      packet.comboAirSpeedMultiplier = this.airSpeedMultiplier;
      packet.baseSpeed = this.baseSpeed;
      packet.climbSpeed = this.climbSpeed;
      packet.climbSpeedLateral = this.climbSpeedLateral;
      packet.climbUpSprintSpeed = this.climbUpSprintSpeed;
      packet.climbDownSprintSpeed = this.climbDownSprintSpeed;
      packet.horizontalFlySpeed = this.horizontalFlySpeed;
      packet.verticalFlySpeed = this.verticalFlySpeed;
      packet.maxSpeedMultiplier = this.maxSpeedMultiplier;
      packet.minSpeedMultiplier = this.minSpeedMultiplier;
      packet.wishDirectionGravityX = this.wishDirectionGravityX;
      packet.wishDirectionGravityY = this.wishDirectionGravityY;
      packet.wishDirectionWeightX = this.wishDirectionWeightX;
      packet.wishDirectionWeightY = this.wishDirectionWeightY;
      packet.collisionExpulsionForce = this.collisionExpulsionForce;
      packet.forwardWalkSpeedMultiplier = this.forwardWalkSpeedMultiplier;
      packet.backwardWalkSpeedMultiplier = this.backwardWalkSpeedMultiplier;
      packet.strafeWalkSpeedMultiplier = this.strafeWalkSpeedMultiplier;
      packet.forwardRunSpeedMultiplier = this.forwardRunSpeedMultiplier;
      packet.backwardRunSpeedMultiplier = this.backwardRunSpeedMultiplier;
      packet.strafeRunSpeedMultiplier = this.strafeRunSpeedMultiplier;
      packet.forwardCrouchSpeedMultiplier = this.forwardCrouchSpeedMultiplier;
      packet.backwardCrouchSpeedMultiplier = this.backwardCrouchSpeedMultiplier;
      packet.strafeCrouchSpeedMultiplier = this.strafeCrouchSpeedMultiplier;
      packet.forwardSprintSpeedMultiplier = this.forwardSprintSpeedMultiplier;
      packet.variableJumpFallForce = this.variableJumpFallForce;
      packet.fallEffectDuration = this.fallEffectDuration;
      packet.fallJumpForce = this.fallJumpForce;
      packet.fallMomentumLoss = this.fallMomentumLoss;
      packet.autoJumpObstacleSpeedLoss = this.autoJumpObstacleSpeedLoss;
      packet.autoJumpObstacleSprintSpeedLoss = this.autoJumpObstacleSprintSpeedLoss;
      packet.autoJumpObstacleEffectDuration = this.autoJumpObstacleEffectDuration;
      packet.autoJumpObstacleSprintEffectDuration = this.autoJumpObstacleSprintEffectDuration;
      packet.autoJumpObstacleMaxAngle = this.autoJumpObstacleMaxAngle;
      packet.autoJumpDisableJumping = this.autoJumpDisableJumping;
      packet.minSlideEntrySpeed = this.minSlideEntrySpeed;
      packet.slideExitSpeed = this.slideExitSpeed;
      packet.minFallSpeedToEngageRoll = this.minFallSpeedToEngageRoll;
      packet.maxFallSpeedToEngageRoll = this.maxFallSpeedToEngageRoll;
      packet.rollStartSpeedModifier = this.rollStartSpeedModifier;
      packet.rollExitSpeedModifier = this.rollExitSpeedModifier;
      packet.rollTimeToComplete = this.rollTimeToComplete;
      return packet;
   }

   @Nonnull
   @Override
   public String toString() {
      return "MovementConfig{id='"
         + this.id
         + "', velocityResistance="
         + this.velocityResistance
         + ", jumpForce="
         + this.jumpForce
         + ", swimJumpForce="
         + this.swimJumpForce
         + ", jumpBufferDuration="
         + this.jumpBufferDuration
         + ", jumpBufferMaxYVelocity="
         + this.jumpBufferMaxYVelocity
         + ", acceleration="
         + this.acceleration
         + ", airDragMin="
         + this.airDragMin
         + ", airDragMax="
         + this.airDragMax
         + ", airDragMinSpeed="
         + this.airDragMinSpeed
         + ", airDragMaxSpeed="
         + this.airDragMaxSpeed
         + ", airFrictionMin="
         + this.airFrictionMin
         + ", airFrictionMax="
         + this.airFrictionMax
         + ", airFrictionMinSpeed="
         + this.airFrictionMinSpeed
         + ", airFrictionMaxSpeed="
         + this.airFrictionMaxSpeed
         + ", airSpeedMultiplier="
         + this.airSpeedMultiplier
         + ", airControlMinSpeed="
         + this.airControlMinSpeed
         + ", airControlMaxSpeed="
         + this.airControlMaxSpeed
         + ", airControlMinMultiplier="
         + this.airControlMinMultiplier
         + ", airControlMaxMultiplier="
         + this.airControlMaxMultiplier
         + ", comboAirSpeedMultiplier="
         + this.comboAirSpeedMultiplier
         + ", baseSpeed="
         + this.baseSpeed
         + ", climbSpeed="
         + this.climbSpeed
         + ", climbSpeedLateral="
         + this.climbSpeedLateral
         + ", climbUpSprintSpeed="
         + this.climbUpSprintSpeed
         + ", climbDownSprintSpeed="
         + this.climbDownSprintSpeed
         + ", horizontalFlySpeed="
         + this.horizontalFlySpeed
         + ", verticalFlySpeed="
         + this.verticalFlySpeed
         + ", maxSpeedMultiplier="
         + this.maxSpeedMultiplier
         + ", minSpeedMultiplier="
         + this.minSpeedMultiplier
         + ", wishDirectionGravityX="
         + this.wishDirectionGravityX
         + ", wishDirectionGravityY="
         + this.wishDirectionGravityY
         + ", wishDirectionWeightX="
         + this.wishDirectionWeightX
         + ", wishDirectionWeightY="
         + this.wishDirectionWeightY
         + ", collisionExpulsionForce="
         + this.collisionExpulsionForce
         + ", forwardWalkSpeedMultiplier="
         + this.forwardWalkSpeedMultiplier
         + ", backwardWalkSpeedMultiplier="
         + this.backwardWalkSpeedMultiplier
         + ", strafeWalkSpeedMultiplier="
         + this.strafeWalkSpeedMultiplier
         + ", forwardRunSpeedMultiplier="
         + this.forwardRunSpeedMultiplier
         + ", backwardRunSpeedMultiplier="
         + this.backwardRunSpeedMultiplier
         + ", strafeRunSpeedMultiplier="
         + this.strafeRunSpeedMultiplier
         + ", forwardCrouchSpeedMultiplier="
         + this.forwardCrouchSpeedMultiplier
         + ", backwardCrouchSpeedMultiplier="
         + this.backwardCrouchSpeedMultiplier
         + ", strafeCrouchSpeedMultiplier="
         + this.strafeCrouchSpeedMultiplier
         + ", forwardSprintSpeedMultiplier="
         + this.forwardSprintSpeedMultiplier
         + ", variableJumpFallForce="
         + this.variableJumpFallForce
         + ", fallEffectDuration="
         + this.fallEffectDuration
         + ", fallJumpForce="
         + this.fallJumpForce
         + ", fallMomentumLoss="
         + this.fallMomentumLoss
         + ", autoJumpObstacleSpeedLoss="
         + this.autoJumpObstacleSpeedLoss
         + ", autoJumpObstacleSprintSpeedLoss="
         + this.autoJumpObstacleSprintSpeedLoss
         + ", autoJumpObstacleEffectDuration="
         + this.autoJumpObstacleEffectDuration
         + ", autoJumpObstacleSprintEffectDuration="
         + this.autoJumpObstacleSprintEffectDuration
         + ", autoJumpObstacleMaxAngle="
         + this.autoJumpObstacleMaxAngle
         + ", autoJumpDisableJumping="
         + this.autoJumpDisableJumping
         + ", minSlideEntrySpeed="
         + this.minSlideEntrySpeed
         + ", slideExitSpeed="
         + this.slideExitSpeed
         + ", minFallSpeedToEngageRoll="
         + this.minFallSpeedToEngageRoll
         + ", maxFallSpeedToEngageRoll="
         + this.maxFallSpeedToEngageRoll
         + ", fallDamagePartialMitigationPercent="
         + this.fallDamagePartialMitigationPercent
         + ", maxFallSpeedRollFullMitigation="
         + this.maxFallSpeedRollFullMitigation
         + ", rollStartSpeedModifier="
         + this.rollStartSpeedModifier
         + ", rollExitSpeedModifier="
         + this.rollExitSpeedModifier
         + ", rollTimeToComplete="
         + this.rollTimeToComplete
         + "}";
   }
}
