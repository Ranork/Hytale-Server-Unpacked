package com.hypixel.hytale.server.npc.movement.controllers.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.RelationalOperator;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.movement.controllers.MotionControllerDive;
import com.hypixel.hytale.server.spawning.SpawnTestResult;
import com.hypixel.hytale.server.spawning.SpawningContext;
import javax.annotation.Nonnull;

public class BuilderMotionControllerDive extends BuilderMotionControllerBase {
   private double minHorizontalSpeed;
   private double maxVerticalSpeed;
   private double acceleration;
   private double gravity;
   private double maxFallSpeed;
   private double maxSinkSpeed;
   private double maxRotationSpeed;
   private float maxMoveTurnAngle;
   private double minDiveDepth;
   private double maxDiveDepth;
   private double minWaterDepth;
   private double maxWaterDepth;
   private double minDepthAboveGround;
   private double minDepthBelowSurface;
   private double swimDepth;
   private double sinkRatio;
   private double fastDiveThreshold;
   private double desiredDepthWeight;

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Provide diving abilities for NPC";
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
   public MotionControllerDive build(@Nonnull BuilderSupport builderSupport) {
      return new MotionControllerDive(builderSupport, this);
   }

   @Nonnull
   public BuilderMotionControllerDive readConfig(@Nonnull JsonElement data) {
      this.getDouble(
         data, "MaxSwimSpeed", this.maxHorizontalSpeed, 3.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Maximum horizontal speed", null
      );
      this.getDouble(
         data,
         "MaxDiveSpeed",
         v -> this.maxVerticalSpeed = v,
         8.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Maximum vertical speed",
         null
      );
      this.getDouble(
         data,
         "MaxFallSpeed",
         v -> this.maxFallSpeed = v,
         10.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Terminal velocity falling in air",
         null
      );
      this.getDouble(
         data,
         "MaxSinkSpeed",
         v -> this.maxSinkSpeed = v,
         4.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Terminal velocity sinking in water",
         null
      );
      this.getDouble(data, "Gravity", v -> this.gravity = v, 10.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Gravity", null);
      this.getDouble(
         data, "Acceleration", v -> this.acceleration = v, 3.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Acceleration", null
      );
      this.getDouble(
         data,
         "MaxRotationSpeed",
         v -> this.maxRotationSpeed = v,
         360.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Maximum rotational speed in degrees",
         null
      );
      this.getFloat(
         data,
         "MaxSwimTurnAngle",
         v -> this.maxMoveTurnAngle = v,
         90.0F,
         DoubleRangeValidator.between(0.0, 180.0),
         BuilderDescriptorState.WorkInProgress,
         "Maximum angle NPC can walk without explicit turning in degrees",
         null
      );
      this.validateDoubleRelation("MinSwimSpeed", this.minHorizontalSpeed, RelationalOperator.LessEqual, this.maxHorizontalSpeed);
      this.getDouble(
         data,
         "FastSwimThreshold",
         v -> this.fastDiveThreshold = v,
         0.6,
         DoubleRangeValidator.between01(),
         BuilderDescriptorState.WorkInProgress,
         "Relative threshold when fast swimming animation should be used",
         null
      );
      this.getDouble(
         data,
         "SwimDepth",
         v -> this.swimDepth = v,
         0.4,
         DoubleRangeValidator.between(-1.0, 1.0),
         BuilderDescriptorState.WorkInProgress,
         "Minimum height NPC needs to be submerged to be able to swim",
         "0 is at eye height, -1 is bottom of bounding box, +1 top of bounding box. other values between -1 and +1 scale linear"
      );
      this.getDouble(
         data,
         "SinkRatio",
         v -> this.sinkRatio = v,
         1.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.WorkInProgress,
         "Relative sink or climb speed while wandering",
         null
      );
      this.getDouble(data, "MinDiveDepth", v -> this.minDiveDepth = v, 0.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Unknown, null, null);
      this.getDouble(
         data,
         "MaxDiveDepth",
         v -> this.maxDiveDepth = v,
         Double.MAX_VALUE,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.WorkInProgress,
         "Maximum dive depth below surface desired",
         null
      );
      this.getDouble(
         data,
         "MinDepthAboveGround",
         v -> this.minDepthAboveGround = v,
         1.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.WorkInProgress,
         "Minimum distance from ground desired",
         null
      );
      this.getDouble(
         data,
         "MinDepthBelowSurface",
         v -> this.minDepthBelowSurface = v,
         1.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.WorkInProgress,
         "Minimum distance from water surface desired",
         null
      );
      this.getDouble(data, "MinWaterDepth", v -> this.minWaterDepth = v, 1.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Unknown, null, null);
      this.getDouble(data, "MaxWaterDepth", v -> this.maxWaterDepth = v, 0.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Unknown, null, null);
      this.getDouble(
         data,
         "DesiredDepthWeight",
         v -> this.desiredDepthWeight = v,
         0.0,
         DoubleRangeValidator.between(0.0, 1.0),
         BuilderDescriptorState.Stable,
         "How much this NPC prefers being within the desired height range",
         "How much this NPC prefers being within the desired height range. 0 means it doesn't care much, 1 means it will do its best to get there fast."
      );
      return this;
   }

   @Nonnull
   @Override
   public Class<MotionController> category() {
      return MotionController.class;
   }

   public double getMinHorizontalSpeed() {
      return this.minHorizontalSpeed;
   }

   public double getAcceleration() {
      return this.acceleration;
   }

   public double getMaxVerticalSpeed() {
      return this.maxVerticalSpeed;
   }

   public double getGravity() {
      return this.gravity;
   }

   public double getMaxFallSpeed() {
      return this.maxFallSpeed;
   }

   public double getMaxSinkSpeed() {
      return this.maxSinkSpeed;
   }

   public float getMaxMoveTurnAngle() {
      return (float) (Math.PI / 180.0) * this.maxMoveTurnAngle;
   }

   public double getMaxRotationSpeed() {
      return this.maxRotationSpeed * (float) (Math.PI / 180.0);
   }

   public double getMinDiveDepth() {
      return this.minDiveDepth;
   }

   public double getMaxDiveDepth() {
      return this.maxDiveDepth;
   }

   public double getMinWaterDepth() {
      return this.minWaterDepth;
   }

   public double getMaxWaterDepth() {
      return this.maxWaterDepth;
   }

   public double getMinDepthAboveGround() {
      return this.minDepthAboveGround;
   }

   public double getMinDepthBelowSurface() {
      return this.minDepthBelowSurface;
   }

   public double getSwimDepth() {
      return this.swimDepth;
   }

   public double getSinkRatio() {
      return this.sinkRatio;
   }

   public double getFastDiveThreshold() {
      return this.fastDiveThreshold;
   }

   public double getDesiredDepthWeight() {
      return this.desiredDepthWeight;
   }

   @Nonnull
   @Override
   public SpawnTestResult canSpawn(@Nonnull SpawningContext context) {
      Model model = context.getModel();
      double swimDepth = model == null
         ? 0.5
         : MotionControllerDive.relativeSwimDepthToHeight(this.getSwimDepth(), model.getBoundingBox(), model.getEyeHeight());
      if (!context.isInWater((float)swimDepth)) {
         return SpawnTestResult.FAIL_NO_POSITION;
      } else {
         return context.validatePosition(20) ? SpawnTestResult.TEST_OK : SpawnTestResult.FAIL_INVALID_POSITION;
      }
   }

   @Nonnull
   @Override
   public Class<? extends MotionController> getClassType() {
      return MotionControllerDive.class;
   }
}
