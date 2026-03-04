package com.hypixel.hytale.server.npc.movement.controllers.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.core.asset.type.blockset.config.BlockSet;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.FloatHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.RelationalOperator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.BlockSetExistsValidator;
import com.hypixel.hytale.server.npc.movement.controllers.MotionController;
import com.hypixel.hytale.server.npc.movement.controllers.MotionControllerWalk;
import com.hypixel.hytale.server.spawning.SpawnTestResult;
import com.hypixel.hytale.server.spawning.SpawningContext;
import javax.annotation.Nonnull;

public class BuilderMotionControllerWalk extends BuilderMotionControllerBase {
   private static final double[] DEFAULT_JUMP_RANGE = new double[]{0.0, 0.0};
   private double minHorizontalSpeed;
   private double maxVerticalSpeed;
   private final DoubleHolder acceleration = new DoubleHolder();
   private double gravity;
   private final DoubleHolder maxRotationSpeed = new DoubleHolder();
   private final FloatHolder maxMoveTurnAngle = new FloatHolder();
   private final FloatHolder blendRestTurnAngle = new FloatHolder();
   private final DoubleHolder blendRestRelativeSpeed = new DoubleHolder();
   private final DoubleHolder maxClimbHeight = new DoubleHolder();
   private final DoubleHolder jumpHeight = new DoubleHolder();
   private final DoubleHolder jumpForce = new DoubleHolder();
   private final DoubleHolder jumpDescentSteepness = new DoubleHolder();
   private final DoubleHolder minJumpHeight = new DoubleHolder();
   private final DoubleHolder minJumpDistance = new DoubleHolder();
   private final DoubleHolder jumpBlending = new DoubleHolder();
   private final DoubleHolder jumpDescentBlending = new DoubleHolder();
   private final DoubleHolder climbSpeedMult = new DoubleHolder();
   private final DoubleHolder climbSpeedPow = new DoubleHolder();
   private final DoubleHolder climbSpeedConst = new DoubleHolder();
   private final DoubleHolder minDescentAnimationHeight = new DoubleHolder();
   private final DoubleHolder descendFlatness = new DoubleHolder();
   private final DoubleHolder descendSpeedCompensation = new DoubleHolder();
   private final DoubleHolder descentSteepness = new DoubleHolder();
   private final DoubleHolder descentBlending = new DoubleHolder();
   private final DoubleHolder maxDropHeight = new DoubleHolder();
   private double maxVerticalSpeedFluid;
   private final NumberArrayHolder jumpRange = new NumberArrayHolder();
   private double minHover;
   private double maxHover;
   private double minHoverClimb;
   private double minHoverDrop;
   private boolean floatsDown;
   private float hoverFreq;
   private double maxWalkSpeedAfterHitMultiplier;
   private String fenceBlockSet;
   private final EnumHolder<MotionControllerWalk.DescentAnimationType> descentAnimationType = new EnumHolder<>();
   private final EnumHolder<MotionControllerWalk.AscentAnimationType> ascentAnimationType = new EnumHolder<>();

   @Nonnull
   public MotionControllerWalk build(@Nonnull BuilderSupport builderSupport) {
      return new MotionControllerWalk(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Provide walk on ground abilities for NPC";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Provide walk on ground abilities for NPC";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.WorkInProgress;
   }

   @Nonnull
   public BuilderMotionControllerWalk readConfig(@Nonnull JsonElement data) {
      this.getDouble(
         data, "MaxWalkSpeed", this.maxHorizontalSpeed, 3.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Maximum horizontal speed", null
      );
      this.getDouble(
         data,
         "MinWalkSpeed",
         v -> this.minHorizontalSpeed = v,
         0.1,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.WorkInProgress,
         "Minimum horizontal speed",
         null
      );
      this.getDouble(
         data, "MaxFallSpeed", v -> this.maxVerticalSpeed = v, 8.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Maximum fall speed", null
      );
      this.getDouble(
         data,
         "MaxSinkSpeedFluid",
         v -> this.maxVerticalSpeedFluid = v,
         4.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Maximum sink speed in fluids",
         null
      );
      this.getDouble(data, "Gravity", v -> this.gravity = v, 10.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Gravity", null);
      this.getDouble(data, "Acceleration", this.acceleration, 3.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Acceleration", null);
      this.getDouble(
         data,
         "MaxRotationSpeed",
         this.maxRotationSpeed,
         360.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Maximum rotational speed in degrees",
         null
      );
      this.getFloat(
         data,
         "MaxWalkTurnAngle",
         this.maxMoveTurnAngle,
         90.0,
         DoubleRangeValidator.between(0.0, 180.0),
         BuilderDescriptorState.WorkInProgress,
         "Maximum angle NPC can walk without explicit turning in degrees",
         null
      );
      this.getFloat(
         data,
         "BlendRestTurnAngle",
         this.blendRestTurnAngle,
         60.0,
         DoubleRangeValidator.between(0.0, 180.0),
         BuilderDescriptorState.WorkInProgress,
         "When NPC is blending heading and turn angle required is larger than this value speed is reduced",
         null
      );
      this.getDouble(
         data,
         "BlendRestRelativeSpeed",
         this.blendRestRelativeSpeed,
         0.2,
         DoubleRangeValidator.between01(),
         BuilderDescriptorState.WorkInProgress,
         "When NPC is blending heading relative speed used when reducing speed",
         null
      );
      this.getDouble(
         data,
         "MaxClimbHeight",
         this.maxClimbHeight,
         1.3,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Maximum height NPC can climb",
         null
      );
      this.getDouble(
         data,
         "JumpHeight",
         this.jumpHeight,
         0.5,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Experimental,
         "How high the NPC jumps above climb height",
         null
      );
      this.getDouble(
         data,
         "MinJumpHeight",
         this.minJumpHeight,
         0.6,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Experimental,
         "Minimum height above which a jump will be attempted",
         null
      );
      this.getDouble(
         data,
         "MinJumpDistance",
         this.minJumpDistance,
         0.2,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Experimental,
         "Minimum distance above which a jump will be executed",
         null
      );
      this.getDouble(
         data,
         "JumpForce",
         this.jumpForce,
         1.5,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Experimental,
         "The force multiplier for the upward motion of the jump",
         null
      );
      this.getDouble(
         data,
         "JumpBlending",
         this.jumpBlending,
         1.0,
         DoubleRangeValidator.between01(),
         BuilderDescriptorState.Experimental,
         "The blending of the upwards jump pattern",
         "The blending of the upward jump pattern. 0 is more curved and 1 is linear"
      );
      this.getDouble(
         data,
         "JumpDescentBlending",
         this.jumpDescentBlending,
         1.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Experimental,
         "The blending of the jump descent pattern",
         "The blending of the jump descent pattern. 0 is linear while higher values become more curved"
      );
      this.getDouble(
         data,
         "JumpDescentSteepness",
         this.jumpDescentSteepness,
         1.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Experimental,
         "The steepness of the descent portion of the jump",
         null
      );
      this.getEnum(
         data,
         "AscentAnimationType",
         this.ascentAnimationType,
         MotionControllerWalk.AscentAnimationType.class,
         MotionControllerWalk.AscentAnimationType.Walk,
         BuilderDescriptorState.Stable,
         "The animation to play when walking up a block",
         null
      );
      this.getDouble(
         data,
         "ClimbSpeedMult",
         this.climbSpeedMult,
         0.0,
         null,
         BuilderDescriptorState.WorkInProgress,
         "Climb speed multiplier (const + multiplier * walkspeed ** power)",
         null
      );
      this.getDouble(
         data,
         "ClimbSpeedPow",
         this.climbSpeedPow,
         1.0,
         null,
         BuilderDescriptorState.WorkInProgress,
         "Climb speed power (const + multiplier * walkspeed ** power)",
         null
      );
      this.getDouble(
         data,
         "ClimbSpeedConst",
         this.climbSpeedConst,
         5.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.WorkInProgress,
         "Climb speed constant (const + multiplier * walkspeed ** power)",
         null
      );
      this.getDouble(
         data,
         "MinDescentAnimationHeight",
         this.minDescentAnimationHeight,
         1.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Stable,
         "The min drop distance to switch from the standard walking animation to the specified descent animation",
         null
      );
      this.getDouble(
         data,
         "DescendFlatness",
         this.descendFlatness,
         0.7,
         DoubleRangeValidator.between01(),
         BuilderDescriptorState.WorkInProgress,
         "Relative scale how fast NPC moves forward while climbing down",
         null
      );
      this.getDouble(
         data,
         "DescendSpeedCompensation",
         this.descendSpeedCompensation,
         0.9,
         DoubleRangeValidator.between01(),
         BuilderDescriptorState.WorkInProgress,
         "Factor to compensate forward speed reduction while moving downwards",
         null
      );
      this.getEnum(
         data,
         "DescentAnimationType",
         this.descentAnimationType,
         MotionControllerWalk.DescentAnimationType.class,
         MotionControllerWalk.DescentAnimationType.Fall,
         BuilderDescriptorState.Experimental,
         "The animation to play when moving down a block",
         null
      );
      this.getDouble(
         data,
         "DescentSteepness",
         this.descentSteepness,
         1.4,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Experimental,
         "The relative steepness of the descent",
         null
      );
      this.getDouble(
         data,
         "DescentBlending",
         this.descentBlending,
         1.8,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Experimental,
         "The blending of the descent pattern",
         "The blending of the descent pattern. 0 is linear, while higher values become more curved"
      );
      this.getDouble(
         data,
         "MaxDropHeight",
         this.maxDropHeight,
         3.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.WorkInProgress,
         "Maximum height NPC considers drop safe",
         null
      );
      this.getAsset(
         data,
         "FenceBlockSet",
         v -> this.fenceBlockSet = v,
         "Fence",
         BlockSetExistsValidator.withConfig(AssetValidator.CanBeEmpty),
         BuilderDescriptorState.Stable,
         "Unclimbable blocks",
         null
      );
      this.getDoubleRange(
         data,
         "JumpRange",
         this.jumpRange,
         DEFAULT_JUMP_RANGE,
         DoubleSequenceValidator.betweenWeaklyMonotonic(0.0, 10.0),
         BuilderDescriptorState.WorkInProgress,
         "Jump distance range",
         null
      );
      this.getDouble(
         data,
         "MinHover",
         v -> this.minHover = v,
         0.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.WorkInProgress,
         "Minimum hover height over ground",
         null
      );
      this.getDouble(
         data,
         "MinHoverClimb",
         v -> this.minHoverClimb = v,
         0.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.WorkInProgress,
         "Minimum hover height over ground when climbing",
         null
      );
      this.getDouble(
         data,
         "MinHoverDrop",
         v -> this.minHoverDrop = v,
         0.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.WorkInProgress,
         "Minimum hover height over ground when dropping",
         null
      );
      this.getBoolean(
         data,
         "FloatsDown",
         v -> this.floatsDown = v,
         true,
         BuilderDescriptorState.WorkInProgress,
         "If true NPC floats down when hovering enabled else gravity decides",
         null
      );
      this.getDouble(
         data,
         "MaxHover",
         v -> this.maxHover = v,
         0.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.WorkInProgress,
         "Maximum hover height over ground",
         null
      );
      this.getFloat(
         data,
         "HoverFreq",
         v -> this.hoverFreq = v,
         0.0F,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.WorkInProgress,
         "Hover frequency",
         null
      );
      this.getDouble(
         data,
         "MinHitSlowdown",
         v -> this.maxWalkSpeedAfterHitMultiplier = 1.0 - v,
         0.1,
         DoubleRangeValidator.between01(),
         BuilderDescriptorState.Stable,
         "The minimum percentage to slow down by when attacked from behind",
         null
      );
      this.validateDoubleRelation("MinHover", this.minHover, RelationalOperator.LessEqual, "MaxHover", this.maxHover);
      this.validateDoubleRelation("MinHoverClimb", this.minHoverClimb, RelationalOperator.LessEqual, "MinHover", this.minHover);
      this.validateDoubleRelation("MinHoverDrop", this.minHoverDrop, RelationalOperator.LessEqual, "MinHover", this.minHover);
      this.validateDoubleRelation("MinWalkSpeed", this.minHorizontalSpeed, RelationalOperator.LessEqual, this.maxHorizontalSpeed);
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

   public double getAcceleration(@Nonnull BuilderSupport builderSupport) {
      return this.acceleration.get(builderSupport.getExecutionContext());
   }

   public double getMaxVerticalSpeed() {
      return this.maxVerticalSpeed;
   }

   public double getMaxVerticalSpeedFluid() {
      return this.maxVerticalSpeedFluid;
   }

   public double getGravity() {
      return this.gravity;
   }

   public float getMaxMoveTurnAngle(@Nonnull BuilderSupport builderSupport) {
      return (float) (Math.PI / 180.0) * this.maxMoveTurnAngle.get(builderSupport.getExecutionContext());
   }

   public double getMaxRotationSpeed(@Nonnull BuilderSupport builderSupport) {
      return (float) (Math.PI / 180.0) * this.maxRotationSpeed.get(builderSupport.getExecutionContext());
   }

   public float getBlendRestTurnAngle(@Nonnull BuilderSupport builderSupport) {
      return (float) (Math.PI / 180.0) * this.blendRestTurnAngle.get(builderSupport.getExecutionContext());
   }

   public double getBlendRestRelativeSpeed(@Nonnull BuilderSupport builderSupport) {
      return this.blendRestRelativeSpeed.get(builderSupport.getExecutionContext());
   }

   public double getMaxClimbHeight(@Nonnull BuilderSupport support) {
      return this.maxClimbHeight.get(support.getExecutionContext());
   }

   public double getClimbSpeedMult(BuilderSupport support) {
      return this.climbSpeedMult.get(support.getExecutionContext());
   }

   public double getClimbSpeedPow(BuilderSupport support) {
      return this.climbSpeedPow.get(support.getExecutionContext());
   }

   public double getClimbSpeedConst(BuilderSupport support) {
      return this.climbSpeedConst.get(support.getExecutionContext());
   }

   public double getDescendForwardAmount(@Nonnull BuilderSupport builderSupport) {
      return this.descendFlatness.get(builderSupport.getExecutionContext());
   }

   public double getDescendSpeedCompensation(@Nonnull BuilderSupport builderSupport) {
      return this.descendSpeedCompensation.get(builderSupport.getExecutionContext());
   }

   public double getMaxDropHeight(@Nonnull BuilderSupport support) {
      return this.maxDropHeight.get(support.getExecutionContext());
   }

   public int getFenceBlockSet() {
      int index = BlockSet.getAssetMap().getIndex(this.fenceBlockSet);
      if (index == Integer.MIN_VALUE) {
         throw new IllegalArgumentException("Unknown key! " + this.fenceBlockSet);
      } else {
         return index;
      }
   }

   public double getMinHover() {
      return this.minHover;
   }

   public double getMaxHover() {
      return this.maxHover;
   }

   public float getHoverFreq() {
      return this.hoverFreq;
   }

   public double getMinHoverClimb() {
      return this.minHoverClimb;
   }

   public double getMinHoverDrop() {
      return this.minHoverDrop;
   }

   public boolean isFloatsDown() {
      return this.floatsDown;
   }

   public double getMaxWalkSpeedAfterHitMultiplier() {
      return this.maxWalkSpeedAfterHitMultiplier;
   }

   public double getJumpHeight(@Nonnull BuilderSupport support) {
      return this.jumpHeight.get(support.getExecutionContext());
   }

   public double getMinJumpHeight(@Nonnull BuilderSupport support) {
      return this.minJumpHeight.get(support.getExecutionContext());
   }

   public double getMinJumpDistance(@Nonnull BuilderSupport support) {
      return this.minJumpDistance.get(support.getExecutionContext());
   }

   public double getJumpForce(@Nonnull BuilderSupport support) {
      return this.jumpForce.get(support.getExecutionContext());
   }

   public double getJumpDescentSteepness(@Nonnull BuilderSupport support) {
      return this.jumpDescentSteepness.get(support.getExecutionContext());
   }

   public double getJumpBlending(@Nonnull BuilderSupport support) {
      return this.jumpBlending.get(support.getExecutionContext());
   }

   public double getJumpDescentBlending(@Nonnull BuilderSupport support) {
      return this.jumpDescentBlending.get(support.getExecutionContext());
   }

   public MotionControllerWalk.DescentAnimationType getDescentAnimationType(BuilderSupport support) {
      return this.descentAnimationType.get(support.getExecutionContext());
   }

   public MotionControllerWalk.AscentAnimationType getAscentAnimationType(BuilderSupport support) {
      return this.ascentAnimationType.get(support.getExecutionContext());
   }

   public double getDescentSteepness(@Nonnull BuilderSupport support) {
      return this.descentSteepness.get(support.getExecutionContext());
   }

   public double getDescentBlending(@Nonnull BuilderSupport support) {
      return this.descentBlending.get(support.getExecutionContext());
   }

   public double getMinDescentAnimationHeight(@Nonnull BuilderSupport support) {
      return this.minDescentAnimationHeight.get(support.getExecutionContext());
   }

   public double[] getJumpRange(@Nonnull BuilderSupport support) {
      return this.jumpRange.get(support.getExecutionContext());
   }

   @Nonnull
   @Override
   public SpawnTestResult canSpawn(@Nonnull SpawningContext context) {
      if (!context.isOnSolidGround()) {
         return SpawnTestResult.FAIL_NO_POSITION;
      } else {
         return context.validatePosition(20) ? SpawnTestResult.TEST_OK : SpawnTestResult.FAIL_INVALID_POSITION;
      }
   }

   @Nonnull
   @Override
   public Class<? extends MotionController> getClassType() {
      return MotionControllerWalk.class;
   }
}
