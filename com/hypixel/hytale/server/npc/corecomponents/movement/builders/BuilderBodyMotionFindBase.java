package com.hypixel.hytale.server.npc.corecomponents.movement.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.IntHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.IntSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderBodyMotionBase;
import com.hypixel.hytale.server.npc.corecomponents.movement.BodyMotionFindBase;
import com.hypixel.hytale.server.npc.instructions.BodyMotion;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public abstract class BuilderBodyMotionFindBase extends BuilderBodyMotionBase implements Builder<BodyMotion> {
   protected static final double[] THROTTLE_DELAY = new double[]{3.0, 5.0};
   @Nonnull
   protected EnumSet<BodyMotionFindBase.DebugFlags> parsedDebugFlags = EnumSet.noneOf(BodyMotionFindBase.DebugFlags.class);
   protected String debugFlags;
   protected final IntHolder nodesPerTick = new IntHolder();
   protected final IntHolder maxPathLength = new IntHolder();
   protected final IntHolder maxOpenNodes = new IntHolder();
   protected final IntHolder maxTotalNodes = new IntHolder();
   protected final BooleanHolder diagonalMoves = new BooleanHolder();
   protected final BooleanHolder useBestPath = new BooleanHolder();
   protected final BooleanHolder buildOptimisedPath = new BooleanHolder();
   protected final IntHolder pathSmoothing = new IntHolder();
   protected final DoubleHolder relativeSpeed = new DoubleHolder();
   protected final DoubleHolder relativeSpeedWaypoint = new DoubleHolder();
   protected final DoubleHolder waypointRadius = new DoubleHolder();
   protected final DoubleHolder rejectionWeight = new DoubleHolder();
   protected final DoubleHolder blendHeading = new DoubleHolder();
   protected final BooleanHolder isAvoidingBlockDamage = new BooleanHolder();
   protected final BooleanHolder isRelaxedMoveConstraints = new BooleanHolder();
   protected final NumberArrayHolder throttleDelayRangeHolder = new NumberArrayHolder();
   protected final IntHolder throttleIgnoreCount = new IntHolder();
   protected final BooleanHolder useSteering = new BooleanHolder();
   protected final BooleanHolder usePathfinder = new BooleanHolder();
   protected final BooleanHolder skipSteering = new BooleanHolder();
   protected final DoubleHolder minPathLength = new DoubleHolder();
   protected final DoubleHolder desiredAltitudeWeight = new DoubleHolder();
   protected final boolean enableSteering;

   public BuilderBodyMotionFindBase() {
      this.enableSteering = true;
   }

   public BuilderBodyMotionFindBase(boolean enableSteering) {
      this.enableSteering = enableSteering;
   }

   @Nonnull
   public BuilderBodyMotionFindBase readConfig(@Nonnull JsonElement data) {
      this.getDouble(
         data,
         "RelativeSpeed",
         this.relativeSpeed,
         1.0,
         DoubleRangeValidator.fromExclToIncl(0.0, 2.0),
         BuilderDescriptorState.Stable,
         "Maximum relative speed the NPC should move",
         null
      );
      this.getDouble(
         data,
         "RelativeSpeedWaypoint",
         this.relativeSpeedWaypoint,
         0.5,
         DoubleRangeValidator.fromExclToIncl(0.0, 1.0),
         BuilderDescriptorState.Stable,
         "Maximum relative speed the NPC should move close to waypoints",
         null
      );
      this.getDouble(
         data,
         "WaypointRadius",
         this.waypointRadius,
         0.5,
         DoubleSingleValidator.greater(0.1),
         BuilderDescriptorState.Stable,
         "Radius to slow down around waypoints",
         null
      );
      this.getBoolean(data, "UseBestPath", this.useBestPath, true, BuilderDescriptorState.Stable, "Use best partial path if goal can't be reached", null);
      this.getDoubleRange(
         data,
         "ThrottleDelayRange",
         this.throttleDelayRangeHolder,
         THROTTLE_DELAY,
         DoubleSequenceValidator.betweenWeaklyMonotonic(0.0, Double.MAX_VALUE),
         BuilderDescriptorState.Stable,
         "Time to delay after no path finding solution found",
         null
      );
      this.getInt(
         data,
         "ThrottleIgnoreCount",
         this.throttleIgnoreCount,
         3,
         IntSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Stable,
         "How often no valid path solution can be found before throttling delay is applied",
         null
      );
      this.getBoolean(
         data, "BuildOptimisedPath", this.buildOptimisedPath, true, BuilderDescriptorState.Stable, "Try to reduce number of nodes of generated path", null
      );
      this.getBoolean(
         data, "AvoidBlockDamage", this.isAvoidingBlockDamage, true, BuilderDescriptorState.Stable, "Should avoid environmental damage from blocks", null
      );
      this.getBoolean(
         data,
         "RelaxedMoveConstraints",
         this.isRelaxedMoveConstraints,
         true,
         BuilderDescriptorState.Stable,
         "NPC can do movements like wading (depends on motion controller type)",
         null
      );
      this.getDouble(
         data,
         "BlendHeading",
         this.blendHeading,
         0.5,
         DoubleRangeValidator.between01(),
         BuilderDescriptorState.Stable,
         "Relative rotation angle into next waypoint when arriving at current waypoint",
         null
      );
      this.getInt(
         data,
         "PathSmoothing",
         this.pathSmoothing,
         2,
         IntSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Stable,
         "Try to smooth followed path. Larger values smooth more.",
         null
      );
      this.getDouble(
         data,
         "RejectionWeight",
         this.rejectionWeight,
         3.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Weight of rejection vector pushing entity closer to original path",
         null
      );
      if (this.enableSteering) {
         this.getBoolean(data, "UseSteering", this.useSteering, true, BuilderDescriptorState.Stable, "Use simple/cheap steering if available", null);
         this.getBoolean(data, "SkipSteering", this.skipSteering, true, BuilderDescriptorState.Experimental, "Skip steering if target not reachable", null);
         this.getBoolean(data, "UsePathfinder", this.usePathfinder, true, BuilderDescriptorState.Stable, "Use path finder", null);
      }

      this.getDouble(
         data,
         "MinPathLength",
         this.minPathLength,
         2.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Experimental,
         "Minimum length of path required when not able to reach target (should be greater equal 2)",
         null
      );
      this.getBoolean(data, "DiagonalMoves", this.diagonalMoves, true, BuilderDescriptorState.Stable, "Allow diagonal moves", null);
      this.getInt(data, "StepsPerTick", this.nodesPerTick, 50, IntSingleValidator.greater0(), BuilderDescriptorState.Stable, "Steps per iteration", null);
      this.getInt(
         data,
         "MaxPathLength",
         this.maxPathLength,
         200,
         IntSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Max path steps before aborting path finding",
         null
      );
      this.getInt(
         data,
         "MaxOpenNodes",
         this.maxOpenNodes,
         200,
         IntSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Max open nodes before aborting path finding",
         null
      );
      this.getInt(
         data,
         "MaxTotalNodes",
         this.maxTotalNodes,
         900,
         IntSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "Max total node before aborting path finding",
         null
      );
      this.getString(data, "Debug", e -> this.debugFlags = e, "", null, BuilderDescriptorState.Stable, "Debugging flags", null);
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
      if (this.debugFlags != null && !this.debugFlags.isEmpty()) {
         this.toSet("Debug", BodyMotionFindBase.DebugFlags.class, this.parsedDebugFlags, this.debugFlags);
      }

      return this;
   }

   @Nonnull
   public EnumSet<BodyMotionFindBase.DebugFlags> getParsedDebugFlags() {
      return this.parsedDebugFlags;
   }

   public int getNodesPerTick(@Nonnull BuilderSupport support) {
      return this.nodesPerTick.get(support.getExecutionContext());
   }

   public int getMaxPathLength(@Nonnull BuilderSupport support) {
      return this.maxPathLength.get(support.getExecutionContext());
   }

   public int getMaxOpenNodes(@Nonnull BuilderSupport support) {
      return this.maxOpenNodes.get(support.getExecutionContext());
   }

   public int getMaxTotalNodes(@Nonnull BuilderSupport support) {
      return this.maxTotalNodes.get(support.getExecutionContext());
   }

   public boolean isDiagonalMoves(@Nonnull BuilderSupport support) {
      return this.diagonalMoves.get(support.getExecutionContext());
   }

   public boolean getUseBestPath(@Nonnull BuilderSupport support) {
      return this.useBestPath.get(support.getExecutionContext());
   }

   public boolean isBuildOptimisedPath(@Nonnull BuilderSupport support) {
      return this.buildOptimisedPath.get(support.getExecutionContext());
   }

   public int getPathSmoothing(@Nonnull BuilderSupport support) {
      return this.pathSmoothing.get(support.getExecutionContext());
   }

   public double getRelativeSpeed(@Nonnull BuilderSupport support) {
      return this.relativeSpeed.get(support.getExecutionContext());
   }

   public double getRelativeSpeedWaypoint(@Nonnull BuilderSupport support) {
      return this.relativeSpeedWaypoint.get(support.getExecutionContext());
   }

   public double getWaypointRadius(@Nonnull BuilderSupport support) {
      return this.waypointRadius.get(support.getExecutionContext());
   }

   public double getRejectionWeight(@Nonnull BuilderSupport support) {
      return this.rejectionWeight.get(support.getExecutionContext());
   }

   public double getBlendHeading(@Nonnull BuilderSupport support) {
      return this.blendHeading.get(support.getExecutionContext());
   }

   public boolean isAvoidingBlockDamage(@Nonnull BuilderSupport support) {
      return this.isAvoidingBlockDamage.get(support.getExecutionContext());
   }

   public boolean isRelaxedMoveConstraints(@Nonnull BuilderSupport support) {
      return this.isRelaxedMoveConstraints.get(support.getExecutionContext());
   }

   public double[] getThrottleDelayRange(@Nonnull BuilderSupport support) {
      return this.throttleDelayRangeHolder.get(support.getExecutionContext());
   }

   public int getThrottleIgnoreCount(@Nonnull BuilderSupport support) {
      return this.throttleIgnoreCount.get(support.getExecutionContext());
   }

   public boolean isUseSteering(@Nonnull BuilderSupport support) {
      return this.enableSteering && this.useSteering.get(support.getExecutionContext());
   }

   public boolean isUsePathfinder(@Nonnull BuilderSupport support) {
      return !this.enableSteering || this.usePathfinder.get(support.getExecutionContext());
   }

   public boolean isSkipSteering(@Nonnull BuilderSupport support) {
      return this.enableSteering && this.skipSteering.get(support.getExecutionContext());
   }

   public double getMinPathLength(@Nonnull BuilderSupport support) {
      return this.minPathLength.get(support.getExecutionContext());
   }

   public double getDesiredAltitudeWeight(@Nonnull BuilderSupport support) {
      return this.desiredAltitudeWeight.get(support.getExecutionContext());
   }
}
