package com.hypixel.hytale.protocol;

import com.hypixel.hytale.protocol.io.ValidationResult;
import io.netty.buffer.ByteBuf;
import java.util.Objects;
import javax.annotation.Nonnull;

public class MovementSettings {
   public static final int NULLABLE_BIT_FIELD_SIZE = 0;
   public static final int FIXED_BLOCK_SIZE = 251;
   public static final int VARIABLE_FIELD_COUNT = 0;
   public static final int VARIABLE_BLOCK_START = 251;
   public static final int MAX_SIZE = 251;
   public float mass;
   public float dragCoefficient;
   public boolean invertedGravity;
   public float velocityResistance;
   public float jumpForce;
   public float swimJumpForce;
   public float jumpBufferDuration;
   public float jumpBufferMaxYVelocity;
   public float acceleration;
   public float airDragMin;
   public float airDragMax;
   public float airDragMinSpeed;
   public float airDragMaxSpeed;
   public float airFrictionMin;
   public float airFrictionMax;
   public float airFrictionMinSpeed;
   public float airFrictionMaxSpeed;
   public float airSpeedMultiplier;
   public float airControlMinSpeed;
   public float airControlMaxSpeed;
   public float airControlMinMultiplier;
   public float airControlMaxMultiplier;
   public float comboAirSpeedMultiplier;
   public float baseSpeed;
   public float climbSpeed;
   public float climbSpeedLateral;
   public float climbUpSprintSpeed;
   public float climbDownSprintSpeed;
   public float horizontalFlySpeed;
   public float verticalFlySpeed;
   public float maxSpeedMultiplier;
   public float minSpeedMultiplier;
   public float wishDirectionGravityX;
   public float wishDirectionGravityY;
   public float wishDirectionWeightX;
   public float wishDirectionWeightY;
   public boolean canFly;
   public float collisionExpulsionForce;
   public float forwardWalkSpeedMultiplier;
   public float backwardWalkSpeedMultiplier;
   public float strafeWalkSpeedMultiplier;
   public float forwardRunSpeedMultiplier;
   public float backwardRunSpeedMultiplier;
   public float strafeRunSpeedMultiplier;
   public float forwardCrouchSpeedMultiplier;
   public float backwardCrouchSpeedMultiplier;
   public float strafeCrouchSpeedMultiplier;
   public float forwardSprintSpeedMultiplier;
   public float variableJumpFallForce;
   public float fallEffectDuration;
   public float fallJumpForce;
   public float fallMomentumLoss;
   public float autoJumpObstacleSpeedLoss;
   public float autoJumpObstacleSprintSpeedLoss;
   public float autoJumpObstacleEffectDuration;
   public float autoJumpObstacleSprintEffectDuration;
   public float autoJumpObstacleMaxAngle;
   public boolean autoJumpDisableJumping;
   public float minSlideEntrySpeed;
   public float slideExitSpeed;
   public float minFallSpeedToEngageRoll;
   public float maxFallSpeedToEngageRoll;
   public float rollStartSpeedModifier;
   public float rollExitSpeedModifier;
   public float rollTimeToComplete;

   public MovementSettings() {
   }

   public MovementSettings(
      float mass,
      float dragCoefficient,
      boolean invertedGravity,
      float velocityResistance,
      float jumpForce,
      float swimJumpForce,
      float jumpBufferDuration,
      float jumpBufferMaxYVelocity,
      float acceleration,
      float airDragMin,
      float airDragMax,
      float airDragMinSpeed,
      float airDragMaxSpeed,
      float airFrictionMin,
      float airFrictionMax,
      float airFrictionMinSpeed,
      float airFrictionMaxSpeed,
      float airSpeedMultiplier,
      float airControlMinSpeed,
      float airControlMaxSpeed,
      float airControlMinMultiplier,
      float airControlMaxMultiplier,
      float comboAirSpeedMultiplier,
      float baseSpeed,
      float climbSpeed,
      float climbSpeedLateral,
      float climbUpSprintSpeed,
      float climbDownSprintSpeed,
      float horizontalFlySpeed,
      float verticalFlySpeed,
      float maxSpeedMultiplier,
      float minSpeedMultiplier,
      float wishDirectionGravityX,
      float wishDirectionGravityY,
      float wishDirectionWeightX,
      float wishDirectionWeightY,
      boolean canFly,
      float collisionExpulsionForce,
      float forwardWalkSpeedMultiplier,
      float backwardWalkSpeedMultiplier,
      float strafeWalkSpeedMultiplier,
      float forwardRunSpeedMultiplier,
      float backwardRunSpeedMultiplier,
      float strafeRunSpeedMultiplier,
      float forwardCrouchSpeedMultiplier,
      float backwardCrouchSpeedMultiplier,
      float strafeCrouchSpeedMultiplier,
      float forwardSprintSpeedMultiplier,
      float variableJumpFallForce,
      float fallEffectDuration,
      float fallJumpForce,
      float fallMomentumLoss,
      float autoJumpObstacleSpeedLoss,
      float autoJumpObstacleSprintSpeedLoss,
      float autoJumpObstacleEffectDuration,
      float autoJumpObstacleSprintEffectDuration,
      float autoJumpObstacleMaxAngle,
      boolean autoJumpDisableJumping,
      float minSlideEntrySpeed,
      float slideExitSpeed,
      float minFallSpeedToEngageRoll,
      float maxFallSpeedToEngageRoll,
      float rollStartSpeedModifier,
      float rollExitSpeedModifier,
      float rollTimeToComplete
   ) {
      this.mass = mass;
      this.dragCoefficient = dragCoefficient;
      this.invertedGravity = invertedGravity;
      this.velocityResistance = velocityResistance;
      this.jumpForce = jumpForce;
      this.swimJumpForce = swimJumpForce;
      this.jumpBufferDuration = jumpBufferDuration;
      this.jumpBufferMaxYVelocity = jumpBufferMaxYVelocity;
      this.acceleration = acceleration;
      this.airDragMin = airDragMin;
      this.airDragMax = airDragMax;
      this.airDragMinSpeed = airDragMinSpeed;
      this.airDragMaxSpeed = airDragMaxSpeed;
      this.airFrictionMin = airFrictionMin;
      this.airFrictionMax = airFrictionMax;
      this.airFrictionMinSpeed = airFrictionMinSpeed;
      this.airFrictionMaxSpeed = airFrictionMaxSpeed;
      this.airSpeedMultiplier = airSpeedMultiplier;
      this.airControlMinSpeed = airControlMinSpeed;
      this.airControlMaxSpeed = airControlMaxSpeed;
      this.airControlMinMultiplier = airControlMinMultiplier;
      this.airControlMaxMultiplier = airControlMaxMultiplier;
      this.comboAirSpeedMultiplier = comboAirSpeedMultiplier;
      this.baseSpeed = baseSpeed;
      this.climbSpeed = climbSpeed;
      this.climbSpeedLateral = climbSpeedLateral;
      this.climbUpSprintSpeed = climbUpSprintSpeed;
      this.climbDownSprintSpeed = climbDownSprintSpeed;
      this.horizontalFlySpeed = horizontalFlySpeed;
      this.verticalFlySpeed = verticalFlySpeed;
      this.maxSpeedMultiplier = maxSpeedMultiplier;
      this.minSpeedMultiplier = minSpeedMultiplier;
      this.wishDirectionGravityX = wishDirectionGravityX;
      this.wishDirectionGravityY = wishDirectionGravityY;
      this.wishDirectionWeightX = wishDirectionWeightX;
      this.wishDirectionWeightY = wishDirectionWeightY;
      this.canFly = canFly;
      this.collisionExpulsionForce = collisionExpulsionForce;
      this.forwardWalkSpeedMultiplier = forwardWalkSpeedMultiplier;
      this.backwardWalkSpeedMultiplier = backwardWalkSpeedMultiplier;
      this.strafeWalkSpeedMultiplier = strafeWalkSpeedMultiplier;
      this.forwardRunSpeedMultiplier = forwardRunSpeedMultiplier;
      this.backwardRunSpeedMultiplier = backwardRunSpeedMultiplier;
      this.strafeRunSpeedMultiplier = strafeRunSpeedMultiplier;
      this.forwardCrouchSpeedMultiplier = forwardCrouchSpeedMultiplier;
      this.backwardCrouchSpeedMultiplier = backwardCrouchSpeedMultiplier;
      this.strafeCrouchSpeedMultiplier = strafeCrouchSpeedMultiplier;
      this.forwardSprintSpeedMultiplier = forwardSprintSpeedMultiplier;
      this.variableJumpFallForce = variableJumpFallForce;
      this.fallEffectDuration = fallEffectDuration;
      this.fallJumpForce = fallJumpForce;
      this.fallMomentumLoss = fallMomentumLoss;
      this.autoJumpObstacleSpeedLoss = autoJumpObstacleSpeedLoss;
      this.autoJumpObstacleSprintSpeedLoss = autoJumpObstacleSprintSpeedLoss;
      this.autoJumpObstacleEffectDuration = autoJumpObstacleEffectDuration;
      this.autoJumpObstacleSprintEffectDuration = autoJumpObstacleSprintEffectDuration;
      this.autoJumpObstacleMaxAngle = autoJumpObstacleMaxAngle;
      this.autoJumpDisableJumping = autoJumpDisableJumping;
      this.minSlideEntrySpeed = minSlideEntrySpeed;
      this.slideExitSpeed = slideExitSpeed;
      this.minFallSpeedToEngageRoll = minFallSpeedToEngageRoll;
      this.maxFallSpeedToEngageRoll = maxFallSpeedToEngageRoll;
      this.rollStartSpeedModifier = rollStartSpeedModifier;
      this.rollExitSpeedModifier = rollExitSpeedModifier;
      this.rollTimeToComplete = rollTimeToComplete;
   }

   public MovementSettings(@Nonnull MovementSettings other) {
      this.mass = other.mass;
      this.dragCoefficient = other.dragCoefficient;
      this.invertedGravity = other.invertedGravity;
      this.velocityResistance = other.velocityResistance;
      this.jumpForce = other.jumpForce;
      this.swimJumpForce = other.swimJumpForce;
      this.jumpBufferDuration = other.jumpBufferDuration;
      this.jumpBufferMaxYVelocity = other.jumpBufferMaxYVelocity;
      this.acceleration = other.acceleration;
      this.airDragMin = other.airDragMin;
      this.airDragMax = other.airDragMax;
      this.airDragMinSpeed = other.airDragMinSpeed;
      this.airDragMaxSpeed = other.airDragMaxSpeed;
      this.airFrictionMin = other.airFrictionMin;
      this.airFrictionMax = other.airFrictionMax;
      this.airFrictionMinSpeed = other.airFrictionMinSpeed;
      this.airFrictionMaxSpeed = other.airFrictionMaxSpeed;
      this.airSpeedMultiplier = other.airSpeedMultiplier;
      this.airControlMinSpeed = other.airControlMinSpeed;
      this.airControlMaxSpeed = other.airControlMaxSpeed;
      this.airControlMinMultiplier = other.airControlMinMultiplier;
      this.airControlMaxMultiplier = other.airControlMaxMultiplier;
      this.comboAirSpeedMultiplier = other.comboAirSpeedMultiplier;
      this.baseSpeed = other.baseSpeed;
      this.climbSpeed = other.climbSpeed;
      this.climbSpeedLateral = other.climbSpeedLateral;
      this.climbUpSprintSpeed = other.climbUpSprintSpeed;
      this.climbDownSprintSpeed = other.climbDownSprintSpeed;
      this.horizontalFlySpeed = other.horizontalFlySpeed;
      this.verticalFlySpeed = other.verticalFlySpeed;
      this.maxSpeedMultiplier = other.maxSpeedMultiplier;
      this.minSpeedMultiplier = other.minSpeedMultiplier;
      this.wishDirectionGravityX = other.wishDirectionGravityX;
      this.wishDirectionGravityY = other.wishDirectionGravityY;
      this.wishDirectionWeightX = other.wishDirectionWeightX;
      this.wishDirectionWeightY = other.wishDirectionWeightY;
      this.canFly = other.canFly;
      this.collisionExpulsionForce = other.collisionExpulsionForce;
      this.forwardWalkSpeedMultiplier = other.forwardWalkSpeedMultiplier;
      this.backwardWalkSpeedMultiplier = other.backwardWalkSpeedMultiplier;
      this.strafeWalkSpeedMultiplier = other.strafeWalkSpeedMultiplier;
      this.forwardRunSpeedMultiplier = other.forwardRunSpeedMultiplier;
      this.backwardRunSpeedMultiplier = other.backwardRunSpeedMultiplier;
      this.strafeRunSpeedMultiplier = other.strafeRunSpeedMultiplier;
      this.forwardCrouchSpeedMultiplier = other.forwardCrouchSpeedMultiplier;
      this.backwardCrouchSpeedMultiplier = other.backwardCrouchSpeedMultiplier;
      this.strafeCrouchSpeedMultiplier = other.strafeCrouchSpeedMultiplier;
      this.forwardSprintSpeedMultiplier = other.forwardSprintSpeedMultiplier;
      this.variableJumpFallForce = other.variableJumpFallForce;
      this.fallEffectDuration = other.fallEffectDuration;
      this.fallJumpForce = other.fallJumpForce;
      this.fallMomentumLoss = other.fallMomentumLoss;
      this.autoJumpObstacleSpeedLoss = other.autoJumpObstacleSpeedLoss;
      this.autoJumpObstacleSprintSpeedLoss = other.autoJumpObstacleSprintSpeedLoss;
      this.autoJumpObstacleEffectDuration = other.autoJumpObstacleEffectDuration;
      this.autoJumpObstacleSprintEffectDuration = other.autoJumpObstacleSprintEffectDuration;
      this.autoJumpObstacleMaxAngle = other.autoJumpObstacleMaxAngle;
      this.autoJumpDisableJumping = other.autoJumpDisableJumping;
      this.minSlideEntrySpeed = other.minSlideEntrySpeed;
      this.slideExitSpeed = other.slideExitSpeed;
      this.minFallSpeedToEngageRoll = other.minFallSpeedToEngageRoll;
      this.maxFallSpeedToEngageRoll = other.maxFallSpeedToEngageRoll;
      this.rollStartSpeedModifier = other.rollStartSpeedModifier;
      this.rollExitSpeedModifier = other.rollExitSpeedModifier;
      this.rollTimeToComplete = other.rollTimeToComplete;
   }

   @Nonnull
   public static MovementSettings deserialize(@Nonnull ByteBuf buf, int offset) {
      MovementSettings obj = new MovementSettings();
      obj.mass = buf.getFloatLE(offset + 0);
      obj.dragCoefficient = buf.getFloatLE(offset + 4);
      obj.invertedGravity = buf.getByte(offset + 8) != 0;
      obj.velocityResistance = buf.getFloatLE(offset + 9);
      obj.jumpForce = buf.getFloatLE(offset + 13);
      obj.swimJumpForce = buf.getFloatLE(offset + 17);
      obj.jumpBufferDuration = buf.getFloatLE(offset + 21);
      obj.jumpBufferMaxYVelocity = buf.getFloatLE(offset + 25);
      obj.acceleration = buf.getFloatLE(offset + 29);
      obj.airDragMin = buf.getFloatLE(offset + 33);
      obj.airDragMax = buf.getFloatLE(offset + 37);
      obj.airDragMinSpeed = buf.getFloatLE(offset + 41);
      obj.airDragMaxSpeed = buf.getFloatLE(offset + 45);
      obj.airFrictionMin = buf.getFloatLE(offset + 49);
      obj.airFrictionMax = buf.getFloatLE(offset + 53);
      obj.airFrictionMinSpeed = buf.getFloatLE(offset + 57);
      obj.airFrictionMaxSpeed = buf.getFloatLE(offset + 61);
      obj.airSpeedMultiplier = buf.getFloatLE(offset + 65);
      obj.airControlMinSpeed = buf.getFloatLE(offset + 69);
      obj.airControlMaxSpeed = buf.getFloatLE(offset + 73);
      obj.airControlMinMultiplier = buf.getFloatLE(offset + 77);
      obj.airControlMaxMultiplier = buf.getFloatLE(offset + 81);
      obj.comboAirSpeedMultiplier = buf.getFloatLE(offset + 85);
      obj.baseSpeed = buf.getFloatLE(offset + 89);
      obj.climbSpeed = buf.getFloatLE(offset + 93);
      obj.climbSpeedLateral = buf.getFloatLE(offset + 97);
      obj.climbUpSprintSpeed = buf.getFloatLE(offset + 101);
      obj.climbDownSprintSpeed = buf.getFloatLE(offset + 105);
      obj.horizontalFlySpeed = buf.getFloatLE(offset + 109);
      obj.verticalFlySpeed = buf.getFloatLE(offset + 113);
      obj.maxSpeedMultiplier = buf.getFloatLE(offset + 117);
      obj.minSpeedMultiplier = buf.getFloatLE(offset + 121);
      obj.wishDirectionGravityX = buf.getFloatLE(offset + 125);
      obj.wishDirectionGravityY = buf.getFloatLE(offset + 129);
      obj.wishDirectionWeightX = buf.getFloatLE(offset + 133);
      obj.wishDirectionWeightY = buf.getFloatLE(offset + 137);
      obj.canFly = buf.getByte(offset + 141) != 0;
      obj.collisionExpulsionForce = buf.getFloatLE(offset + 142);
      obj.forwardWalkSpeedMultiplier = buf.getFloatLE(offset + 146);
      obj.backwardWalkSpeedMultiplier = buf.getFloatLE(offset + 150);
      obj.strafeWalkSpeedMultiplier = buf.getFloatLE(offset + 154);
      obj.forwardRunSpeedMultiplier = buf.getFloatLE(offset + 158);
      obj.backwardRunSpeedMultiplier = buf.getFloatLE(offset + 162);
      obj.strafeRunSpeedMultiplier = buf.getFloatLE(offset + 166);
      obj.forwardCrouchSpeedMultiplier = buf.getFloatLE(offset + 170);
      obj.backwardCrouchSpeedMultiplier = buf.getFloatLE(offset + 174);
      obj.strafeCrouchSpeedMultiplier = buf.getFloatLE(offset + 178);
      obj.forwardSprintSpeedMultiplier = buf.getFloatLE(offset + 182);
      obj.variableJumpFallForce = buf.getFloatLE(offset + 186);
      obj.fallEffectDuration = buf.getFloatLE(offset + 190);
      obj.fallJumpForce = buf.getFloatLE(offset + 194);
      obj.fallMomentumLoss = buf.getFloatLE(offset + 198);
      obj.autoJumpObstacleSpeedLoss = buf.getFloatLE(offset + 202);
      obj.autoJumpObstacleSprintSpeedLoss = buf.getFloatLE(offset + 206);
      obj.autoJumpObstacleEffectDuration = buf.getFloatLE(offset + 210);
      obj.autoJumpObstacleSprintEffectDuration = buf.getFloatLE(offset + 214);
      obj.autoJumpObstacleMaxAngle = buf.getFloatLE(offset + 218);
      obj.autoJumpDisableJumping = buf.getByte(offset + 222) != 0;
      obj.minSlideEntrySpeed = buf.getFloatLE(offset + 223);
      obj.slideExitSpeed = buf.getFloatLE(offset + 227);
      obj.minFallSpeedToEngageRoll = buf.getFloatLE(offset + 231);
      obj.maxFallSpeedToEngageRoll = buf.getFloatLE(offset + 235);
      obj.rollStartSpeedModifier = buf.getFloatLE(offset + 239);
      obj.rollExitSpeedModifier = buf.getFloatLE(offset + 243);
      obj.rollTimeToComplete = buf.getFloatLE(offset + 247);
      return obj;
   }

   public static int computeBytesConsumed(@Nonnull ByteBuf buf, int offset) {
      return 251;
   }

   public void serialize(@Nonnull ByteBuf buf) {
      buf.writeFloatLE(this.mass);
      buf.writeFloatLE(this.dragCoefficient);
      buf.writeByte(this.invertedGravity ? 1 : 0);
      buf.writeFloatLE(this.velocityResistance);
      buf.writeFloatLE(this.jumpForce);
      buf.writeFloatLE(this.swimJumpForce);
      buf.writeFloatLE(this.jumpBufferDuration);
      buf.writeFloatLE(this.jumpBufferMaxYVelocity);
      buf.writeFloatLE(this.acceleration);
      buf.writeFloatLE(this.airDragMin);
      buf.writeFloatLE(this.airDragMax);
      buf.writeFloatLE(this.airDragMinSpeed);
      buf.writeFloatLE(this.airDragMaxSpeed);
      buf.writeFloatLE(this.airFrictionMin);
      buf.writeFloatLE(this.airFrictionMax);
      buf.writeFloatLE(this.airFrictionMinSpeed);
      buf.writeFloatLE(this.airFrictionMaxSpeed);
      buf.writeFloatLE(this.airSpeedMultiplier);
      buf.writeFloatLE(this.airControlMinSpeed);
      buf.writeFloatLE(this.airControlMaxSpeed);
      buf.writeFloatLE(this.airControlMinMultiplier);
      buf.writeFloatLE(this.airControlMaxMultiplier);
      buf.writeFloatLE(this.comboAirSpeedMultiplier);
      buf.writeFloatLE(this.baseSpeed);
      buf.writeFloatLE(this.climbSpeed);
      buf.writeFloatLE(this.climbSpeedLateral);
      buf.writeFloatLE(this.climbUpSprintSpeed);
      buf.writeFloatLE(this.climbDownSprintSpeed);
      buf.writeFloatLE(this.horizontalFlySpeed);
      buf.writeFloatLE(this.verticalFlySpeed);
      buf.writeFloatLE(this.maxSpeedMultiplier);
      buf.writeFloatLE(this.minSpeedMultiplier);
      buf.writeFloatLE(this.wishDirectionGravityX);
      buf.writeFloatLE(this.wishDirectionGravityY);
      buf.writeFloatLE(this.wishDirectionWeightX);
      buf.writeFloatLE(this.wishDirectionWeightY);
      buf.writeByte(this.canFly ? 1 : 0);
      buf.writeFloatLE(this.collisionExpulsionForce);
      buf.writeFloatLE(this.forwardWalkSpeedMultiplier);
      buf.writeFloatLE(this.backwardWalkSpeedMultiplier);
      buf.writeFloatLE(this.strafeWalkSpeedMultiplier);
      buf.writeFloatLE(this.forwardRunSpeedMultiplier);
      buf.writeFloatLE(this.backwardRunSpeedMultiplier);
      buf.writeFloatLE(this.strafeRunSpeedMultiplier);
      buf.writeFloatLE(this.forwardCrouchSpeedMultiplier);
      buf.writeFloatLE(this.backwardCrouchSpeedMultiplier);
      buf.writeFloatLE(this.strafeCrouchSpeedMultiplier);
      buf.writeFloatLE(this.forwardSprintSpeedMultiplier);
      buf.writeFloatLE(this.variableJumpFallForce);
      buf.writeFloatLE(this.fallEffectDuration);
      buf.writeFloatLE(this.fallJumpForce);
      buf.writeFloatLE(this.fallMomentumLoss);
      buf.writeFloatLE(this.autoJumpObstacleSpeedLoss);
      buf.writeFloatLE(this.autoJumpObstacleSprintSpeedLoss);
      buf.writeFloatLE(this.autoJumpObstacleEffectDuration);
      buf.writeFloatLE(this.autoJumpObstacleSprintEffectDuration);
      buf.writeFloatLE(this.autoJumpObstacleMaxAngle);
      buf.writeByte(this.autoJumpDisableJumping ? 1 : 0);
      buf.writeFloatLE(this.minSlideEntrySpeed);
      buf.writeFloatLE(this.slideExitSpeed);
      buf.writeFloatLE(this.minFallSpeedToEngageRoll);
      buf.writeFloatLE(this.maxFallSpeedToEngageRoll);
      buf.writeFloatLE(this.rollStartSpeedModifier);
      buf.writeFloatLE(this.rollExitSpeedModifier);
      buf.writeFloatLE(this.rollTimeToComplete);
   }

   public int computeSize() {
      return 251;
   }

   public static ValidationResult validateStructure(@Nonnull ByteBuf buffer, int offset) {
      return buffer.readableBytes() - offset < 251 ? ValidationResult.error("Buffer too small: expected at least 251 bytes") : ValidationResult.OK;
   }

   public MovementSettings clone() {
      MovementSettings copy = new MovementSettings();
      copy.mass = this.mass;
      copy.dragCoefficient = this.dragCoefficient;
      copy.invertedGravity = this.invertedGravity;
      copy.velocityResistance = this.velocityResistance;
      copy.jumpForce = this.jumpForce;
      copy.swimJumpForce = this.swimJumpForce;
      copy.jumpBufferDuration = this.jumpBufferDuration;
      copy.jumpBufferMaxYVelocity = this.jumpBufferMaxYVelocity;
      copy.acceleration = this.acceleration;
      copy.airDragMin = this.airDragMin;
      copy.airDragMax = this.airDragMax;
      copy.airDragMinSpeed = this.airDragMinSpeed;
      copy.airDragMaxSpeed = this.airDragMaxSpeed;
      copy.airFrictionMin = this.airFrictionMin;
      copy.airFrictionMax = this.airFrictionMax;
      copy.airFrictionMinSpeed = this.airFrictionMinSpeed;
      copy.airFrictionMaxSpeed = this.airFrictionMaxSpeed;
      copy.airSpeedMultiplier = this.airSpeedMultiplier;
      copy.airControlMinSpeed = this.airControlMinSpeed;
      copy.airControlMaxSpeed = this.airControlMaxSpeed;
      copy.airControlMinMultiplier = this.airControlMinMultiplier;
      copy.airControlMaxMultiplier = this.airControlMaxMultiplier;
      copy.comboAirSpeedMultiplier = this.comboAirSpeedMultiplier;
      copy.baseSpeed = this.baseSpeed;
      copy.climbSpeed = this.climbSpeed;
      copy.climbSpeedLateral = this.climbSpeedLateral;
      copy.climbUpSprintSpeed = this.climbUpSprintSpeed;
      copy.climbDownSprintSpeed = this.climbDownSprintSpeed;
      copy.horizontalFlySpeed = this.horizontalFlySpeed;
      copy.verticalFlySpeed = this.verticalFlySpeed;
      copy.maxSpeedMultiplier = this.maxSpeedMultiplier;
      copy.minSpeedMultiplier = this.minSpeedMultiplier;
      copy.wishDirectionGravityX = this.wishDirectionGravityX;
      copy.wishDirectionGravityY = this.wishDirectionGravityY;
      copy.wishDirectionWeightX = this.wishDirectionWeightX;
      copy.wishDirectionWeightY = this.wishDirectionWeightY;
      copy.canFly = this.canFly;
      copy.collisionExpulsionForce = this.collisionExpulsionForce;
      copy.forwardWalkSpeedMultiplier = this.forwardWalkSpeedMultiplier;
      copy.backwardWalkSpeedMultiplier = this.backwardWalkSpeedMultiplier;
      copy.strafeWalkSpeedMultiplier = this.strafeWalkSpeedMultiplier;
      copy.forwardRunSpeedMultiplier = this.forwardRunSpeedMultiplier;
      copy.backwardRunSpeedMultiplier = this.backwardRunSpeedMultiplier;
      copy.strafeRunSpeedMultiplier = this.strafeRunSpeedMultiplier;
      copy.forwardCrouchSpeedMultiplier = this.forwardCrouchSpeedMultiplier;
      copy.backwardCrouchSpeedMultiplier = this.backwardCrouchSpeedMultiplier;
      copy.strafeCrouchSpeedMultiplier = this.strafeCrouchSpeedMultiplier;
      copy.forwardSprintSpeedMultiplier = this.forwardSprintSpeedMultiplier;
      copy.variableJumpFallForce = this.variableJumpFallForce;
      copy.fallEffectDuration = this.fallEffectDuration;
      copy.fallJumpForce = this.fallJumpForce;
      copy.fallMomentumLoss = this.fallMomentumLoss;
      copy.autoJumpObstacleSpeedLoss = this.autoJumpObstacleSpeedLoss;
      copy.autoJumpObstacleSprintSpeedLoss = this.autoJumpObstacleSprintSpeedLoss;
      copy.autoJumpObstacleEffectDuration = this.autoJumpObstacleEffectDuration;
      copy.autoJumpObstacleSprintEffectDuration = this.autoJumpObstacleSprintEffectDuration;
      copy.autoJumpObstacleMaxAngle = this.autoJumpObstacleMaxAngle;
      copy.autoJumpDisableJumping = this.autoJumpDisableJumping;
      copy.minSlideEntrySpeed = this.minSlideEntrySpeed;
      copy.slideExitSpeed = this.slideExitSpeed;
      copy.minFallSpeedToEngageRoll = this.minFallSpeedToEngageRoll;
      copy.maxFallSpeedToEngageRoll = this.maxFallSpeedToEngageRoll;
      copy.rollStartSpeedModifier = this.rollStartSpeedModifier;
      copy.rollExitSpeedModifier = this.rollExitSpeedModifier;
      copy.rollTimeToComplete = this.rollTimeToComplete;
      return copy;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else {
         return !(obj instanceof MovementSettings other)
            ? false
            : this.mass == other.mass
               && this.dragCoefficient == other.dragCoefficient
               && this.invertedGravity == other.invertedGravity
               && this.velocityResistance == other.velocityResistance
               && this.jumpForce == other.jumpForce
               && this.swimJumpForce == other.swimJumpForce
               && this.jumpBufferDuration == other.jumpBufferDuration
               && this.jumpBufferMaxYVelocity == other.jumpBufferMaxYVelocity
               && this.acceleration == other.acceleration
               && this.airDragMin == other.airDragMin
               && this.airDragMax == other.airDragMax
               && this.airDragMinSpeed == other.airDragMinSpeed
               && this.airDragMaxSpeed == other.airDragMaxSpeed
               && this.airFrictionMin == other.airFrictionMin
               && this.airFrictionMax == other.airFrictionMax
               && this.airFrictionMinSpeed == other.airFrictionMinSpeed
               && this.airFrictionMaxSpeed == other.airFrictionMaxSpeed
               && this.airSpeedMultiplier == other.airSpeedMultiplier
               && this.airControlMinSpeed == other.airControlMinSpeed
               && this.airControlMaxSpeed == other.airControlMaxSpeed
               && this.airControlMinMultiplier == other.airControlMinMultiplier
               && this.airControlMaxMultiplier == other.airControlMaxMultiplier
               && this.comboAirSpeedMultiplier == other.comboAirSpeedMultiplier
               && this.baseSpeed == other.baseSpeed
               && this.climbSpeed == other.climbSpeed
               && this.climbSpeedLateral == other.climbSpeedLateral
               && this.climbUpSprintSpeed == other.climbUpSprintSpeed
               && this.climbDownSprintSpeed == other.climbDownSprintSpeed
               && this.horizontalFlySpeed == other.horizontalFlySpeed
               && this.verticalFlySpeed == other.verticalFlySpeed
               && this.maxSpeedMultiplier == other.maxSpeedMultiplier
               && this.minSpeedMultiplier == other.minSpeedMultiplier
               && this.wishDirectionGravityX == other.wishDirectionGravityX
               && this.wishDirectionGravityY == other.wishDirectionGravityY
               && this.wishDirectionWeightX == other.wishDirectionWeightX
               && this.wishDirectionWeightY == other.wishDirectionWeightY
               && this.canFly == other.canFly
               && this.collisionExpulsionForce == other.collisionExpulsionForce
               && this.forwardWalkSpeedMultiplier == other.forwardWalkSpeedMultiplier
               && this.backwardWalkSpeedMultiplier == other.backwardWalkSpeedMultiplier
               && this.strafeWalkSpeedMultiplier == other.strafeWalkSpeedMultiplier
               && this.forwardRunSpeedMultiplier == other.forwardRunSpeedMultiplier
               && this.backwardRunSpeedMultiplier == other.backwardRunSpeedMultiplier
               && this.strafeRunSpeedMultiplier == other.strafeRunSpeedMultiplier
               && this.forwardCrouchSpeedMultiplier == other.forwardCrouchSpeedMultiplier
               && this.backwardCrouchSpeedMultiplier == other.backwardCrouchSpeedMultiplier
               && this.strafeCrouchSpeedMultiplier == other.strafeCrouchSpeedMultiplier
               && this.forwardSprintSpeedMultiplier == other.forwardSprintSpeedMultiplier
               && this.variableJumpFallForce == other.variableJumpFallForce
               && this.fallEffectDuration == other.fallEffectDuration
               && this.fallJumpForce == other.fallJumpForce
               && this.fallMomentumLoss == other.fallMomentumLoss
               && this.autoJumpObstacleSpeedLoss == other.autoJumpObstacleSpeedLoss
               && this.autoJumpObstacleSprintSpeedLoss == other.autoJumpObstacleSprintSpeedLoss
               && this.autoJumpObstacleEffectDuration == other.autoJumpObstacleEffectDuration
               && this.autoJumpObstacleSprintEffectDuration == other.autoJumpObstacleSprintEffectDuration
               && this.autoJumpObstacleMaxAngle == other.autoJumpObstacleMaxAngle
               && this.autoJumpDisableJumping == other.autoJumpDisableJumping
               && this.minSlideEntrySpeed == other.minSlideEntrySpeed
               && this.slideExitSpeed == other.slideExitSpeed
               && this.minFallSpeedToEngageRoll == other.minFallSpeedToEngageRoll
               && this.maxFallSpeedToEngageRoll == other.maxFallSpeedToEngageRoll
               && this.rollStartSpeedModifier == other.rollStartSpeedModifier
               && this.rollExitSpeedModifier == other.rollExitSpeedModifier
               && this.rollTimeToComplete == other.rollTimeToComplete;
      }
   }

   @Override
   public int hashCode() {
      return Objects.hash(
         this.mass,
         this.dragCoefficient,
         this.invertedGravity,
         this.velocityResistance,
         this.jumpForce,
         this.swimJumpForce,
         this.jumpBufferDuration,
         this.jumpBufferMaxYVelocity,
         this.acceleration,
         this.airDragMin,
         this.airDragMax,
         this.airDragMinSpeed,
         this.airDragMaxSpeed,
         this.airFrictionMin,
         this.airFrictionMax,
         this.airFrictionMinSpeed,
         this.airFrictionMaxSpeed,
         this.airSpeedMultiplier,
         this.airControlMinSpeed,
         this.airControlMaxSpeed,
         this.airControlMinMultiplier,
         this.airControlMaxMultiplier,
         this.comboAirSpeedMultiplier,
         this.baseSpeed,
         this.climbSpeed,
         this.climbSpeedLateral,
         this.climbUpSprintSpeed,
         this.climbDownSprintSpeed,
         this.horizontalFlySpeed,
         this.verticalFlySpeed,
         this.maxSpeedMultiplier,
         this.minSpeedMultiplier,
         this.wishDirectionGravityX,
         this.wishDirectionGravityY,
         this.wishDirectionWeightX,
         this.wishDirectionWeightY,
         this.canFly,
         this.collisionExpulsionForce,
         this.forwardWalkSpeedMultiplier,
         this.backwardWalkSpeedMultiplier,
         this.strafeWalkSpeedMultiplier,
         this.forwardRunSpeedMultiplier,
         this.backwardRunSpeedMultiplier,
         this.strafeRunSpeedMultiplier,
         this.forwardCrouchSpeedMultiplier,
         this.backwardCrouchSpeedMultiplier,
         this.strafeCrouchSpeedMultiplier,
         this.forwardSprintSpeedMultiplier,
         this.variableJumpFallForce,
         this.fallEffectDuration,
         this.fallJumpForce,
         this.fallMomentumLoss,
         this.autoJumpObstacleSpeedLoss,
         this.autoJumpObstacleSprintSpeedLoss,
         this.autoJumpObstacleEffectDuration,
         this.autoJumpObstacleSprintEffectDuration,
         this.autoJumpObstacleMaxAngle,
         this.autoJumpDisableJumping,
         this.minSlideEntrySpeed,
         this.slideExitSpeed,
         this.minFallSpeedToEngageRoll,
         this.maxFallSpeedToEngageRoll,
         this.rollStartSpeedModifier,
         this.rollExitSpeedModifier,
         this.rollTimeToComplete
      );
   }
}
