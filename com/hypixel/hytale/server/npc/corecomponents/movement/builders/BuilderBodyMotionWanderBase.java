package com.hypixel.hytale.server.npc.corecomponents.movement.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumSetHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.FloatHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.IntHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.IntSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.RelationalOperator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderBodyMotionBase;
import com.hypixel.hytale.server.npc.corecomponents.movement.BodyMotionWanderBase;
import com.hypixel.hytale.server.npc.instructions.BodyMotion;
import com.hypixel.hytale.server.npc.movement.constraints.RelaxedConstraint;
import java.util.EnumSet;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BuilderBodyMotionWanderBase extends BuilderBodyMotionBase {
   private static final String[] AVOID_OR_RELAXED_ATTRIBUTES = new String[]{"AvoidBlockDamage", "RelaxedConstraints"};
   private static final String[] LEGACY_OR_RELAXED_ATTRIBUTES = new String[]{"RelaxedMoveConstraints", "RelaxedConstraints"};
   protected final DoubleHolder minWalkTime = new DoubleHolder();
   protected final DoubleHolder maxWalkTime = new DoubleHolder();
   protected final FloatHolder minHeadingChange = new FloatHolder();
   protected final FloatHolder maxHeadingChange = new FloatHolder();
   protected final BooleanHolder relaxHeadingChange = new BooleanHolder();
   protected final DoubleHolder relativeSpeed = new DoubleHolder();
   protected final DoubleHolder minMoveDistance = new DoubleHolder();
   protected final DoubleHolder stopDistance = new DoubleHolder();
   protected final BooleanHolder isAvoidingBlockDamage = new BooleanHolder();
   protected final BooleanHolder isLegacyRelaxedMoveConstraints = new BooleanHolder();
   protected final EnumSetHolder<RelaxedConstraint> relaxedConstraints = new EnumSetHolder<>();
   protected final IntHolder testsPerTick = new IntHolder();
   protected final DoubleHolder desiredAltitudeWeight = new DoubleHolder();
   private final boolean[] avoidOrRelaxedPresent = new boolean[2];
   private final boolean[] legacyOrRelaxedPresent = new boolean[2];

   @Nullable
   public BodyMotionWanderBase build(@Nonnull BuilderSupport builderSupport) {
      builderSupport.setRequireLeashPosition();
      return null;
   }

   @Nonnull
   @Override
   public Builder<BodyMotion> readCommonConfig(@Nonnull JsonElement data) {
      super.readCommonConfig(data);
      this.getDouble(
         data,
         "MinWalkTime",
         this.minWalkTime,
         2.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Stable,
         "Minimum time to wander for a segment.",
         null
      );
      this.getDouble(
         data,
         "MaxWalkTime",
         this.maxWalkTime,
         4.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Maximum time to wander for a segment.",
         null
      );
      this.getFloat(
         data,
         "MinHeadingChange",
         this.minHeadingChange,
         0.0,
         DoubleRangeValidator.between(0.0, 180.0),
         BuilderDescriptorState.Stable,
         "Approximate minimum heading change between segments",
         null
      );
      this.getFloat(
         data,
         "MaxHeadingChange",
         this.maxHeadingChange,
         90.0,
         DoubleRangeValidator.between(0.0, 180.0),
         BuilderDescriptorState.Stable,
         "Approximate maximum heading change between segments",
         null
      );
      this.getBoolean(
         data,
         "RelaxHeadingChange",
         this.relaxHeadingChange,
         true,
         BuilderDescriptorState.Stable,
         "Allow other directions when preferred directions blocked",
         null
      );
      this.getDouble(
         data,
         "RelativeSpeed",
         this.relativeSpeed,
         0.5,
         DoubleRangeValidator.fromExclToIncl(0.0, 2.0),
         BuilderDescriptorState.Stable,
         "Relative wander speed",
         null
      );
      this.getDouble(
         data,
         "MinMoveDistance",
         this.minMoveDistance,
         0.5,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Minimum distance to move in a segment",
         null
      );
      this.getDouble(
         data, "StopDistance", this.stopDistance, 0.5, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Distance to stop at target", null
      );
      this.getInt(data, "TestsPerTick", this.testsPerTick, 1, IntSingleValidator.greater0(), BuilderDescriptorState.Stable, "Direction tests per tick", null);
      this.avoidOrRelaxedPresent[0] = this.getBoolean(
         data, "AvoidBlockDamage", this.isAvoidingBlockDamage, true, BuilderDescriptorState.Deprecated, "Should avoid environmental damage from blocks", null
      );
      this.legacyOrRelaxedPresent[0] = this.getBoolean(
         data,
         "RelaxedMoveConstraints",
         this.isLegacyRelaxedMoveConstraints,
         false,
         BuilderDescriptorState.Deprecated,
         "NPC can do movements like wading (depends on motion controller type)",
         null
      );
      this.avoidOrRelaxedPresent[1] = this.getEnumSet(
         data,
         "RelaxedConstraints",
         this.relaxedConstraints,
         RelaxedConstraint.class,
         EnumSet.noneOf(RelaxedConstraint.class),
         BuilderDescriptorState.Stable,
         "List of constraints to relax for this motion (new mode; empty = no relaxed constraints)",
         null
      );
      this.legacyOrRelaxedPresent[1] = this.avoidOrRelaxedPresent[1];
      this.validateOneOrNonePresent(AVOID_OR_RELAXED_ATTRIBUTES, this.avoidOrRelaxedPresent);
      this.validateOneOrNonePresent(LEGACY_OR_RELAXED_ATTRIBUTES, this.legacyOrRelaxedPresent);
      this.getDouble(
         data,
         "DesiredAltitudeWeight",
         this.desiredAltitudeWeight,
         -1.0,
         DoubleRangeValidator.between(-1.0, 1.0),
         BuilderDescriptorState.Stable,
         "How much this NPC prefers being within the desired height range",
         "How much this NPC prefers being within the desired height range. 0 means it doesn't care much, 1 means it will do its best to get there fast. Values below 0 mean the default in the motion controller will be used."
      );
      this.validateDoubleRelation(this.minWalkTime, RelationalOperator.LessEqual, this.maxWalkTime);
      this.validateFloatRelation(this.minHeadingChange, RelationalOperator.LessEqual, this.maxHeadingChange);
      return this;
   }

   public double getMinWalkTime(@Nonnull BuilderSupport support) {
      return this.minWalkTime.get(support.getExecutionContext());
   }

   public double getMaxWalkTime(@Nonnull BuilderSupport support) {
      return this.maxWalkTime.get(support.getExecutionContext());
   }

   public float getMinHeadingChange(@Nonnull BuilderSupport support) {
      return this.minHeadingChange.get(support.getExecutionContext());
   }

   public float getMaxHeadingChange(@Nonnull BuilderSupport support) {
      return this.maxHeadingChange.get(support.getExecutionContext());
   }

   public boolean isRelaxHeadingChange(@Nonnull BuilderSupport support) {
      return this.relaxHeadingChange.get(support.getExecutionContext());
   }

   public double getRelativeSpeed(@Nonnull BuilderSupport support) {
      return this.relativeSpeed.get(support.getExecutionContext());
   }

   public double getMinMoveDistance(@Nonnull BuilderSupport support) {
      return this.minMoveDistance.get(support.getExecutionContext());
   }

   public double getStopDistance(@Nonnull BuilderSupport support) {
      return this.stopDistance.get(support.getExecutionContext());
   }

   public boolean isAvoidingBlockDamage(@Nonnull BuilderSupport support) {
      return this.isAvoidingBlockDamage.get(support.getExecutionContext());
   }

   public boolean isLegacyRelaxedMoveConstraints(@Nonnull BuilderSupport support) {
      return this.isLegacyRelaxedMoveConstraints.get(support.getExecutionContext());
   }

   @Nonnull
   public EnumSet<RelaxedConstraint> getRelaxedConstraints(@Nonnull BuilderSupport support) {
      return this.relaxedConstraints.get(support.getExecutionContext());
   }

   public boolean isRelaxedConstraintsPresent() {
      return this.avoidOrRelaxedPresent[1];
   }

   public int getTestsPerTick(@Nonnull BuilderSupport support) {
      return this.testsPerTick.get(support.getExecutionContext());
   }

   public double getDesiredAltitudeWeight(@Nonnull BuilderSupport support) {
      return this.desiredAltitudeWeight.get(support.getExecutionContext());
   }
}
