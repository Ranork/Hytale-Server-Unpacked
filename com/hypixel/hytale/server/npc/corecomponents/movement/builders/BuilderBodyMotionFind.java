package com.hypixel.hytale.server.npc.corecomponents.movement.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.RelationalOperator;
import com.hypixel.hytale.server.npc.corecomponents.movement.BodyMotionFind;
import javax.annotation.Nonnull;

public class BuilderBodyMotionFind extends BuilderBodyMotionFindWithTarget {
   private static final double[] DEFAULT_HEIGHT_DIFFERENCE = new double[]{-1.0, 1.0};
   private final BooleanHolder reachable = new BooleanHolder();
   private final NumberArrayHolder heightDifference = new NumberArrayHolder();
   private final DoubleHolder slowDownDistance = new DoubleHolder();
   private final DoubleHolder stopDistance = new DoubleHolder();
   private final DoubleHolder abortDistance = new DoubleHolder();
   private final DoubleHolder falloff = new DoubleHolder();
   private final DoubleHolder switchToSteeringDistance = new DoubleHolder();

   public BodyMotionFind build(@Nonnull BuilderSupport builderSupport) {
      return new BodyMotionFind(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Chase target";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Move towards a target using path finding or steering";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Experimental;
   }

   @Nonnull
   public BuilderBodyMotionFind readConfig(@Nonnull JsonElement data) {
      super.readConfig(data);
      this.getBoolean(
         data, "Reachable", this.reachable, false, BuilderDescriptorState.Experimental, "Target must be reachable so that hitboxes can overlap", null
      );
      this.getDoubleRange(
         data,
         "HeightDifference",
         this.heightDifference,
         DEFAULT_HEIGHT_DIFFERENCE,
         DoubleSequenceValidator.monotonic(),
         BuilderDescriptorState.Experimental,
         "Height difference allowed to target",
         null
      );
      this.getDouble(
         data,
         "SlowDownDistance",
         this.slowDownDistance,
         8.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Distance when to slow down when approaching",
         null
      );
      this.getDouble(
         data, "StopDistance", this.stopDistance, 10.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Distance to stop at", null
      );
      this.getDouble(
         data, "AbortDistance", this.abortDistance, 96.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Distance to abort behaviour", null
      );
      this.getDouble(
         data, "Falloff", this.falloff, 3.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Deceleration when approaching target", null
      );
      this.getDouble(
         data,
         "SwitchToSteeringDistance",
         this.switchToSteeringDistance,
         20.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Distance below NPC can test if target is reachable and abort existing path",
         null
      );
      this.validateDoubleRelation(this.slowDownDistance, RelationalOperator.GreaterEqual, this.stopDistance);
      this.requireFeature(Feature.AnyPosition);
      return this;
   }

   public boolean getReachable(@Nonnull BuilderSupport support) {
      return this.reachable.get(support.getExecutionContext());
   }

   public double getStopDistance(@Nonnull BuilderSupport support) {
      return this.stopDistance.get(support.getExecutionContext());
   }

   public double[] getHeightDifference(@Nonnull BuilderSupport support) {
      return this.heightDifference.get(support.getExecutionContext());
   }

   public double getAbortDistance(@Nonnull BuilderSupport support) {
      return this.abortDistance.get(support.getExecutionContext());
   }

   public double getFalloff(@Nonnull BuilderSupport support) {
      return this.falloff.get(support.getExecutionContext());
   }

   public double getSlowDownDistance(@Nonnull BuilderSupport builderSupport) {
      return this.slowDownDistance.get(builderSupport.getExecutionContext());
   }

   public double getSwitchToSteeringDistance(@Nonnull BuilderSupport support) {
      return this.switchToSteeringDistance.get(support.getExecutionContext());
   }
}
