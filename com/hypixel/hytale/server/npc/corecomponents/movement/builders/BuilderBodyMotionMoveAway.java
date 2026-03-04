package com.hypixel.hytale.server.npc.corecomponents.movement.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.RelationalOperator;
import com.hypixel.hytale.server.npc.corecomponents.movement.BodyMotionMoveAway;
import javax.annotation.Nonnull;

public class BuilderBodyMotionMoveAway extends BuilderBodyMotionFindWithTarget {
   private static final double[] DEFAULT_HOLD_DIRECTION_DURATION_RANGE = new double[]{2.0, 5.0};
   protected final DoubleHolder slowdownDistance = new DoubleHolder();
   protected final DoubleHolder stopDistance = new DoubleHolder();
   protected final DoubleHolder falloff = new DoubleHolder();
   protected final NumberArrayHolder holdDirectionDurationRange = new NumberArrayHolder();
   protected final DoubleHolder changeDirectionViewSector = new DoubleHolder();
   protected final DoubleHolder directionJitter = new DoubleHolder();
   protected final DoubleHolder erraticDistance = new DoubleHolder();
   protected final DoubleHolder erraticExtraJitter = new DoubleHolder();
   protected final DoubleHolder erraticChangeDurationMultiplier = new DoubleHolder();

   @Nonnull
   public BodyMotionMoveAway build(@Nonnull BuilderSupport builderSupport) {
      return new BodyMotionMoveAway(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Move away from target";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Move away from a target using path finding or steering";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Experimental;
   }

   @Nonnull
   public BuilderBodyMotionMoveAway readConfig(@Nonnull JsonElement data) {
      super.readConfig(data);
      this.getDouble(
         data,
         "SlowDownDistance",
         this.slowdownDistance,
         8.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Distance from target when NPC should start to slowdown",
         null
      );
      this.getDouble(
         data,
         "StopDistance",
         this.stopDistance,
         10.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Distance from target when NPC should halt",
         null
      );
      this.getDouble(
         data,
         "Falloff",
         this.falloff,
         3.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Rate how fast the slowdown should happen relative to distance",
         null
      );
      this.getDoubleRange(
         data,
         "HoldDirectionTimeRange",
         this.holdDirectionDurationRange,
         DEFAULT_HOLD_DIRECTION_DURATION_RANGE,
         DoubleSequenceValidator.betweenWeaklyMonotonic(0.0, Double.MAX_VALUE),
         BuilderDescriptorState.Stable,
         "How often to change heading",
         null
      );
      this.getDouble(
         data,
         "ChangeDirectionViewSector",
         this.changeDirectionViewSector,
         230.0,
         DoubleRangeValidator.between(0.0, 360.0),
         BuilderDescriptorState.Stable,
         "The view sector the NPC uses to decide if it should switch direction",
         null
      );
      this.getDouble(
         data,
         "DirectionJitter",
         this.directionJitter,
         45.0,
         DoubleRangeValidator.between(0.0, 180.0),
         BuilderDescriptorState.Stable,
         "How much jitter in degrees to add to the heading the NPC uses",
         null
      );
      this.getDouble(
         data,
         "ErraticDistance",
         this.erraticDistance,
         4.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "If the player is closer than this distance, the NPC will behave more erratically using the additional jitter parameter",
         null
      );
      this.getDouble(
         data,
         "ErraticExtraJitter",
         this.erraticExtraJitter,
         45.0,
         DoubleRangeValidator.between(0.0, 180.0),
         BuilderDescriptorState.Stable,
         "Extra jitter to add to the NPC heading on top of the standard when the target is too close",
         null
      );
      this.getDouble(
         data,
         "ErraticChangeDurationMultiplier",
         this.erraticChangeDurationMultiplier,
         0.5,
         DoubleRangeValidator.fromExclToIncl(0.0, 1.0),
         BuilderDescriptorState.Stable,
         "A multiplier to decrease the duration between direction changes when the target is too close",
         null
      );
      this.validateDoubleRelation(this.slowdownDistance, RelationalOperator.LessEqual, this.stopDistance);
      return this;
   }

   public double getSlowdownDistance(@Nonnull BuilderSupport support) {
      return this.slowdownDistance.get(support.getExecutionContext());
   }

   public double getStopDistance(@Nonnull BuilderSupport support) {
      return this.stopDistance.get(support.getExecutionContext());
   }

   public double getFalloff(@Nonnull BuilderSupport support) {
      return this.falloff.get(support.getExecutionContext());
   }

   public double[] getHoldDirectionDurationRange(@Nonnull BuilderSupport support) {
      return this.holdDirectionDurationRange.get(support.getExecutionContext());
   }

   public float getChangeDirectionViewSectorRadians(@Nonnull BuilderSupport support) {
      return (float)(this.changeDirectionViewSector.get(support.getExecutionContext()) * (float) (Math.PI / 180.0));
   }

   public float getDirectionJitterRadians(@Nonnull BuilderSupport support) {
      return (float)(this.directionJitter.get(support.getExecutionContext()) * (float) (Math.PI / 180.0));
   }

   public double getErraticDistance(@Nonnull BuilderSupport support) {
      return this.erraticDistance.get(support.getExecutionContext());
   }

   public float getErraticExtraJitterRadians(@Nonnull BuilderSupport support) {
      return (float)(this.erraticExtraJitter.get(support.getExecutionContext()) * (float) (Math.PI / 180.0));
   }

   public double getErraticChangeDurationMultiplier(@Nonnull BuilderSupport support) {
      return this.erraticChangeDurationMultiplier.get(support.getExecutionContext());
   }
}
