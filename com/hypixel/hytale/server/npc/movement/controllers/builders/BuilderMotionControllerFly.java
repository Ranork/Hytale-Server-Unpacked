package com.hypixel.hytale.server.npc.movement.controllers.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.movement.controllers.MotionControllerFly;
import com.hypixel.hytale.server.spawning.SpawnTestResult;
import com.hypixel.hytale.server.spawning.SpawningContext;
import javax.annotation.Nonnull;

public class BuilderMotionControllerFly extends BuilderMotionControllerBase {
   private double minAirSpeed;
   private double maxClimbSpeed;
   private double maxSinkSpeed;
   private double maxSinkSpeedFluid;
   private double maxFallSpeed;
   private float maxClimbAngle;
   private float maxSinkAngle;
   private double acceleration;
   private double deceleration;
   private double gravity;
   private float maxTurnSpeed;
   private float maxRollAngle;
   private float maxRollSpeed;
   private float rollDamping;
   private final DoubleHolder minHeightOverGround = new DoubleHolder();
   private final DoubleHolder maxHeightOverGround = new DoubleHolder();
   private double fastFlyThreshold;
   private boolean autoLevel;
   private double desiredAltitudeWeight;

   @Nonnull
   public MotionControllerFly build(@Nonnull BuilderSupport builderSupport) {
      return new MotionControllerFly(builderSupport, this);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Flight motion controller";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.WorkInProgress;
   }

   @Nonnull
   public BuilderMotionControllerFly readConfig(@Nonnull JsonElement data) {
      this.getDouble(
         data,
         "MinAirSpeed",
         v -> this.minAirSpeed = v,
         0.1,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.WorkInProgress,
         "Minimum in air speed",
         null
      );
      this.getDouble(
         data,
         "MaxHorizontalSpeed",
         this.maxHorizontalSpeed,
         8.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Maximum horizontal speed",
         null
      );
      this.getDouble(
         data,
         "MaxClimbSpeed",
         v -> this.maxClimbSpeed = v,
         6.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Maximum climbing speed",
         null
      );
      this.getDouble(
         data,
         "MaxSinkSpeed",
         v -> this.maxSinkSpeed = v,
         10.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Maximum sink/drop speed",
         null
      );
      this.getDouble(
         data, "MaxFallSpeed", v -> this.maxFallSpeed = v, 40.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Maximum fall speed", null
      );
      this.getDouble(
         data,
         "MaxSinkSpeedFluid",
         v -> this.maxSinkSpeedFluid = v,
         4.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Stable,
         "Maximum sink/fall speed in fluids",
         null
      );
      this.getFloat(
         data,
         "MaxClimbAngle",
         v -> this.maxClimbAngle = v,
         45.0F,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Maximum climb angle",
         null
      );
      this.getFloat(
         data, "MaxSinkAngle", v -> this.maxSinkAngle = v, 85.0F, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Maximum sink angle", null
      );
      this.getDouble(
         data, "Acceleration", v -> this.acceleration = v, 4.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Maximum Acceleration", null
      );
      this.getDouble(
         data, "Deceleration", v -> this.deceleration = v, 4.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Maximum deceleration", null
      );
      this.getDouble(data, "Gravity", v -> this.gravity = v, 40.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Gravity", null);
      this.getFloat(
         data,
         "MaxTurnSpeed",
         v -> this.maxTurnSpeed = v,
         180.0F,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Maximum turn speed in degrees per second",
         null
      );
      this.getFloat(
         data,
         "MaxRollAngle",
         v -> this.maxRollAngle = v,
         45.0F,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Maximum roll angle in degrees",
         null
      );
      this.getFloat(
         data,
         "MaxRollSpeed",
         v -> this.maxRollSpeed = v,
         180.0F,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Maximum roll speed in degrees per second",
         null
      );
      this.getFloat(
         data, "RollDamping", v -> this.rollDamping = v, 0.9F, DoubleRangeValidator.between(0.0, 1.0), BuilderDescriptorState.Stable, "Roll damping", null
      );
      this.getDouble(
         data,
         "MinHeightOverGround",
         this.minHeightOverGround,
         1.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Stable,
         "Minimum height over ground",
         null
      );
      this.getDouble(
         data,
         "MaxHeightOverGround",
         this.maxHeightOverGround,
         20.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Maximum height over ground",
         null
      );
      this.getDouble(
         data,
         "FastFlyThreshold",
         v -> this.fastFlyThreshold = v,
         0.6,
         DoubleRangeValidator.between01(),
         BuilderDescriptorState.WorkInProgress,
         "Relative threshold when fast flying animation should be used",
         null
      );
      this.getBoolean(data, "AutoLevel", v -> this.autoLevel = v, true, BuilderDescriptorState.Stable, "Set pitch to 0 when no steering forces applied", null);
      this.getDouble(
         data,
         "DesiredAltitudeWeight",
         v -> this.desiredAltitudeWeight = v,
         0.0,
         DoubleRangeValidator.between(0.0, 1.0),
         BuilderDescriptorState.Stable,
         "How much this NPC prefers being within the desired height range",
         "How much this NPC prefers being within the desired height range. 0 means it doesn't care much, 1 means it will do its best to get there fast."
      );
      return this;
   }

   public double getMinAirSpeed() {
      return this.minAirSpeed;
   }

   public double getMaxClimbSpeed() {
      return this.maxClimbSpeed;
   }

   public double getMaxSinkSpeed() {
      return this.maxSinkSpeed;
   }

   public double getMaxFallSpeed() {
      return this.maxFallSpeed;
   }

   public double getMaxSinkSpeedFluid() {
      return this.maxSinkSpeedFluid;
   }

   public float getMaxClimbAngle() {
      return (float) (Math.PI / 180.0) * this.maxClimbAngle;
   }

   public float getMaxSinkAngle() {
      return (float) (Math.PI / 180.0) * this.maxSinkAngle;
   }

   public double getAcceleration() {
      return this.acceleration;
   }

   public double getDeceleration() {
      return this.deceleration;
   }

   public double getGravity() {
      return this.gravity;
   }

   public float getMaxTurnSpeed() {
      return (float) (Math.PI / 180.0) * this.maxTurnSpeed;
   }

   public float getMaxRollAngle() {
      return (float) (Math.PI / 180.0) * this.maxRollAngle;
   }

   public float getMaxRollSpeed() {
      return (float) (Math.PI / 180.0) * this.maxRollSpeed;
   }

   public float getRollDamping() {
      return this.rollDamping;
   }

   public double getMinHeightOverGround(BuilderSupport support) {
      return this.minHeightOverGround.get(support.getExecutionContext());
   }

   public double getMaxHeightOverGround(BuilderSupport support) {
      return this.maxHeightOverGround.get(support.getExecutionContext());
   }

   public double getFastFlyThreshold() {
      return this.fastFlyThreshold;
   }

   public boolean isAutoLevel() {
      return this.autoLevel;
   }

   public double getDesiredAltitudeWeight() {
      return this.desiredAltitudeWeight;
   }

   @Nonnull
   @Override
   public Class<MotionController> category() {
      return MotionController.class;
   }

   @Nonnull
   @Override
   public String getType() {
      return "fly";
   }

   @Nonnull
   @Override
   public SpawnTestResult canSpawn(@Nonnull SpawningContext context) {
      if (!context.isInAir(2.0)) {
         return SpawnTestResult.FAIL_NO_POSITION;
      } else {
         return context.validatePosition(22) ? SpawnTestResult.TEST_OK : SpawnTestResult.FAIL_INVALID_POSITION;
      }
   }

   @Nonnull
   @Override
   public Class<? extends MotionController> getClassType() {
      return MotionControllerFly.class;
   }
}
