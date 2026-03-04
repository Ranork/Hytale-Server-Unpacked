package com.hypixel.hytale.server.npc.corecomponents.movement.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderBodyMotionBase;
import com.hypixel.hytale.server.npc.corecomponents.movement.BodyMotionMaintainDistance;
import com.hypixel.hytale.server.npc.movement.controllers.MotionControllerWalk;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import javax.annotation.Nonnull;

public class BuilderBodyMotionMaintainDistance extends BuilderBodyMotionBase {
   public static final String MIN_RANGE_PARAMETER = "MinRange";
   public static final String MAX_RANGE_PARAMETER = "MaxRange";
   public static final String POSITIONING_ANGLE_PARAMETER = "PositioningAngle";
   public static final double NO_POSITIONING = Double.MAX_VALUE;
   public static final double[] DEFAULT_STRAFING_DURATION_RANGE = new double[]{0.0, 0.0};
   public static final double[] DEFAULT_STRAFING_FREQUENCY_RANGE = new double[]{2.0, 2.0};
   protected final NumberArrayHolder desiredDistanceRange = new NumberArrayHolder();
   protected final DoubleHolder targetDistanceFactor = new DoubleHolder();
   protected final DoubleHolder moveThreshold = new DoubleHolder();
   protected final DoubleHolder relativeForwardsSpeed = new DoubleHolder();
   protected final DoubleHolder relativeBackwardsSpeed = new DoubleHolder();
   protected final DoubleHolder moveTowardsSlowdownThreshold = new DoubleHolder();
   protected final NumberArrayHolder strafingDurationRange = new NumberArrayHolder();
   protected final NumberArrayHolder strafingFrequencyRange = new NumberArrayHolder();

   @Nonnull
   public BodyMotionMaintainDistance build(@Nonnull BuilderSupport builderSupport) {
      return new BodyMotionMaintainDistance(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Maintain distance from a given position";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderBodyMotionMaintainDistance readConfig(@Nonnull JsonElement data) {
      this.requireDoubleRange(
         data,
         "DesiredDistanceRange",
         this.desiredDistanceRange,
         DoubleSequenceValidator.betweenWeaklyMonotonic(-Double.MAX_VALUE, Double.MAX_VALUE),
         BuilderDescriptorState.Stable,
         "The desired distance to remain at.",
         null
      );
      this.getDouble(
         data,
         "TargetDistanceFactor",
         this.targetDistanceFactor,
         0.5,
         DoubleRangeValidator.between(0.0, 1.0),
         BuilderDescriptorState.Stable,
         "A factor used to decide what distance to move to within the target range.",
         "A factor used to decide what distance to move to within the target range when the target falls outside of it. 0 will result in moving the shortest distance to fall within the range, 1 the furthest distance, and 0.5 roughly the middle of the range."
      );
      this.getDouble(
         data,
         "MoveThreshold",
         this.moveThreshold,
         1.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "An extra threshold distance on either side of the desired range before the NPC will trigger movement.",
         null
      );
      this.getDouble(
         data,
         "RelativeForwardsSpeed",
         this.relativeForwardsSpeed,
         1.0,
         DoubleRangeValidator.fromExclToIncl(0.0, 2.0),
         BuilderDescriptorState.Stable,
         "Maximum relative speed for the NPC moving forwards",
         null
      );
      this.getDouble(
         data,
         "RelativeBackwardsSpeed",
         this.relativeBackwardsSpeed,
         1.0,
         DoubleRangeValidator.fromExclToIncl(0.0, 2.0),
         BuilderDescriptorState.Stable,
         "Maximum relative speed for the NPC moving backwards",
         null
      );
      this.getDouble(
         data,
         "MoveTowardsSlowdownThreshold",
         this.moveTowardsSlowdownThreshold,
         2.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Stable,
         "The distance away from the target stopping point at which the NPC will start to slow down while moving towards the target",
         null
      );
      this.getDoubleRange(
         data,
         "StrafingDurationRange",
         this.strafingDurationRange,
         DEFAULT_STRAFING_DURATION_RANGE,
         DoubleSequenceValidator.betweenWeaklyMonotonic(0.0, Double.MAX_VALUE),
         BuilderDescriptorState.Stable,
         "How long to strafe for.",
         "How long to strafe for (moving left or right around the target). If set to [ 0, 0 ], will not move horizontally at all."
      );
      this.getDoubleRange(
         data,
         "StrafingFrequencyRange",
         this.strafingFrequencyRange,
         DEFAULT_STRAFING_FREQUENCY_RANGE,
         DoubleSequenceValidator.fromExclToInclWeaklyMonotonic(0.0, Double.MAX_VALUE),
         BuilderDescriptorState.Stable,
         "How frequently to execute strafing",
         null
      );
      this.requireFeature(Feature.AnyPosition);
      return this;
   }

   @Override
   public boolean validate(
      String configName, @Nonnull NPCLoadTimeValidationHelper validationHelper, ExecutionContext context, Scope globalScope, @Nonnull List<String> errors
   ) {
      boolean result = super.validate(configName, validationHelper, context, globalScope, errors);
      validationHelper.requireMotionControllerType(MotionControllerWalk.class);
      return result;
   }

   public double[] getDesiredDistanceRange(@Nonnull BuilderSupport support) {
      return this.desiredDistanceRange.get(support.getExecutionContext());
   }

   public double getTargetDistanceFactor(@Nonnull BuilderSupport support) {
      return this.targetDistanceFactor.get(support.getExecutionContext());
   }

   public double getMoveThreshold(@Nonnull BuilderSupport support) {
      return this.moveThreshold.get(support.getExecutionContext());
   }

   public double getRelativeForwardsSpeed(@Nonnull BuilderSupport support) {
      return this.relativeForwardsSpeed.get(support.getExecutionContext());
   }

   public double getRelativeBackwardsSpeed(@Nonnull BuilderSupport support) {
      return this.relativeBackwardsSpeed.get(support.getExecutionContext());
   }

   public double getMoveTowardsSlowdownThreshold(@Nonnull BuilderSupport support) {
      return this.moveTowardsSlowdownThreshold.get(support.getExecutionContext());
   }

   public double[] getStrafingDurationRange(@Nonnull BuilderSupport support) {
      return this.strafingDurationRange.get(support.getExecutionContext());
   }

   public double[] getStrafingFrequencyRange(@Nonnull BuilderSupport support) {
      return this.strafingFrequencyRange.get(support.getExecutionContext());
   }
}
