package com.hypixel.hytale.server.npc.corecomponents.lifecycle.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.FloatHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.IntSequenceValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNullOrNotEmptyValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.FlockAssetExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.ActionSpawn;
import javax.annotation.Nonnull;

public class BuilderActionSpawn extends BuilderActionBase {
   public static final double[] DEFAULT_DISTANCE_RANGE = new double[]{1.0, 1.0};
   public static final int[] DEFAULT_COUNT_RANGE = new int[]{5, 5};
   public static final double[] DEFAULT_DELAY_RANGE = new double[]{0.25, 0.25};
   protected final FloatHolder spawnDirection = new FloatHolder();
   protected final FloatHolder spawnAngle = new FloatHolder();
   protected final BooleanHolder fanOut = new BooleanHolder();
   protected final NumberArrayHolder distanceRange = new NumberArrayHolder();
   protected final NumberArrayHolder countRange = new NumberArrayHolder();
   protected final NumberArrayHolder delayRange = new NumberArrayHolder();
   protected final StringHolder kind = new StringHolder();
   protected final AssetHolder flock = new AssetHolder();
   protected final BooleanHolder launchAtTarget = new BooleanHolder();
   protected final BooleanHolder pitchHigh = new BooleanHolder();
   protected final DoubleHolder spread = new DoubleHolder();
   protected final BooleanHolder joinFlock = new BooleanHolder();
   protected final StringHolder spawnState = new StringHolder();
   protected final StringHolder spawnSubState = new StringHolder();

   @Nonnull
   public ActionSpawn build(@Nonnull BuilderSupport builderSupport) {
      return new ActionSpawn(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Spawn an NPC";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Spawn an NPC";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Experimental;
   }

   @Nonnull
   public BuilderActionSpawn readConfig(@Nonnull JsonElement data) {
      this.getFloat(
         data,
         "SpawnDirection",
         this.spawnDirection,
         0.0,
         DoubleRangeValidator.between(-180.0, 180.0),
         BuilderDescriptorState.Experimental,
         "Direction of spawn cone relative to view direction (in degrees)",
         null
      );
      this.getFloat(
         data,
         "SpawnAngle",
         this.spawnAngle,
         360.0,
         DoubleRangeValidator.between(0.0, 360.0),
         BuilderDescriptorState.Experimental,
         "Cone width of spawn direction (in degrees)",
         null
      );
      this.getBoolean(data, "FanOut", this.fanOut, false, BuilderDescriptorState.Experimental, "Fan NPCs out equally over angle", null);
      this.getDoubleRange(
         data,
         "DistanceRange",
         this.distanceRange,
         DEFAULT_DISTANCE_RANGE,
         DoubleSequenceValidator.fromExclToInclWeaklyMonotonic(0.0, 128.0),
         BuilderDescriptorState.Stable,
         "Distance from spawner to spawn",
         null
      );
      this.getIntRange(
         data,
         "CountRange",
         this.countRange,
         DEFAULT_COUNT_RANGE,
         IntSequenceValidator.fromExclToInclWeaklyMonotonic(0, 100),
         BuilderDescriptorState.Stable,
         "Number of NPCs to spawn",
         null
      );
      this.getDoubleRange(
         data,
         "DelayRange",
         this.delayRange,
         DEFAULT_DELAY_RANGE,
         DoubleSequenceValidator.betweenWeaklyMonotonic(0.0, Double.MAX_VALUE),
         BuilderDescriptorState.Stable,
         "Time between consecutive spawns in seconds",
         null
      );
      this.requireString(data, "Kind", this.kind, StringNotEmptyValidator.get(), BuilderDescriptorState.Experimental, "NPC role to spawn", null);
      this.getAsset(
         data,
         "Flock",
         this.flock,
         null,
         FlockAssetExistsValidator.withConfig(AssetValidator.CanBeEmpty),
         BuilderDescriptorState.Stable,
         "Flock definition to spawn",
         null
      );
      this.getBoolean(
         data, "LaunchAtTarget", this.launchAtTarget, false, BuilderDescriptorState.WorkInProgress, "Launch the spawned NPC at target position/entity", null
      );
      this.getBoolean(data, "PitchHigh", this.pitchHigh, true, BuilderDescriptorState.Stable, "If launching at a target, use high pitch", null);
      this.getDouble(
         data,
         "LaunchSpread",
         this.spread,
         0.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Stable,
         "The radius of the circle centred on the target within which to spread thrown NPCs",
         null
      );
      this.getBoolean(data, "JoinFlock", this.joinFlock, false, BuilderDescriptorState.Stable, "Whether to join the parent NPC's flock", null);
      this.getString(
         data,
         "SpawnState",
         this.spawnState,
         null,
         StringNullOrNotEmptyValidator.get(),
         BuilderDescriptorState.Stable,
         "An optional state to set on the spawned NPC if it exists",
         null
      );
      this.getString(
         data,
         "SpawnSubState",
         this.spawnSubState,
         null,
         StringNullOrNotEmptyValidator.get(),
         BuilderDescriptorState.Stable,
         "An optional substate to set on the spawned NPC if it exists",
         null
      );
      this.requireFeatureIf(this.launchAtTarget, true, Feature.AnyPosition);
      return this;
   }

   public float getSpawnDirection(@Nonnull BuilderSupport support) {
      float spawnDirection = this.spawnDirection.get(support.getExecutionContext());
      return (float) (Math.PI / 180.0) * (spawnDirection < 0.0F ? spawnDirection + (float) (Math.PI * 2) : spawnDirection);
   }

   public float getSpawnAngle(@Nonnull BuilderSupport support) {
      return (float) (Math.PI / 180.0) * this.spawnAngle.get(support.getExecutionContext());
   }

   public boolean isFanOut(@Nonnull BuilderSupport support) {
      return this.fanOut.get(support.getExecutionContext());
   }

   public double[] getDistanceRange(@Nonnull BuilderSupport support) {
      return this.distanceRange.get(support.getExecutionContext());
   }

   public String getKind(@Nonnull BuilderSupport support) {
      return this.kind.get(support.getExecutionContext());
   }

   public String getFlock(@Nonnull BuilderSupport support) {
      return this.flock.get(support.getExecutionContext());
   }

   public int[] getCountRange(@Nonnull BuilderSupport support) {
      return this.countRange.getIntArray(support.getExecutionContext());
   }

   public double[] getDelayRange(@Nonnull BuilderSupport support) {
      return this.delayRange.get(support.getExecutionContext());
   }

   public boolean isLaunchAtTarget(@Nonnull BuilderSupport support) {
      return this.launchAtTarget.get(support.getExecutionContext());
   }

   public boolean isPitchHigh(@Nonnull BuilderSupport support) {
      return this.pitchHigh.get(support.getExecutionContext());
   }

   public double getSpread(@Nonnull BuilderSupport support) {
      return this.spread.get(support.getExecutionContext());
   }

   public boolean isJoinFlock(@Nonnull BuilderSupport support) {
      return this.joinFlock.get(support.getExecutionContext());
   }

   public String getSpawnState(@Nonnull BuilderSupport support) {
      return this.spawnState.get(support.getExecutionContext());
   }

   public String getSpawnSubState(@Nonnull BuilderSupport support) {
      return this.spawnSubState.get(support.getExecutionContext());
   }
}
