package com.hypixel.hytale.server.npc.corecomponents.world.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.core.asset.type.blockset.config.BlockSet;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.FloatHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.BlockSetExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.world.SensorSearchRay;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorSearchRay extends BuilderSensorBase {
   protected final StringHolder id = new StringHolder();
   protected final FloatHolder angle = new FloatHolder();
   protected final DoubleHolder range = new DoubleHolder();
   protected final AssetHolder blockSet = new AssetHolder();
   protected final FloatHolder minRetestAngle = new FloatHolder();
   protected final DoubleHolder minRetestMove = new DoubleHolder();
   protected final DoubleHolder throttleTime = new DoubleHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Fire a ray at a specific angle to see if what it hits matches a given sought block";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorSearchRay(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.requireString(
         data,
         "Name",
         this.id,
         StringNotEmptyValidator.get(),
         BuilderDescriptorState.Stable,
         "The id of this search ray sensor so the position can be cached",
         null
      );
      this.requireFloat(
         data,
         "Angle",
         this.angle,
         DoubleRangeValidator.between(-90.0, 90.0),
         BuilderDescriptorState.Stable,
         "Angle to fire the ray",
         "Angle to fire the ray. Horizontal is 0. Positive is downwards"
      );
      this.requireDouble(data, "Range", this.range, DoubleRangeValidator.fromExclToIncl(0.0, 96.0), BuilderDescriptorState.Stable, "How far to search", null);
      this.requireAsset(data, "Blocks", this.blockSet, BlockSetExistsValidator.required(), BuilderDescriptorState.Stable, "The blockset to search for", null);
      this.getFloat(
         data,
         "MinRetestAngle",
         this.minRetestAngle,
         5.0,
         DoubleRangeValidator.between(0.0, 360.0),
         BuilderDescriptorState.Stable,
         "The minimum change in NPC rotation before rays stop being throttled",
         null
      );
      this.getDouble(
         data,
         "MinRetestMove",
         this.minRetestMove,
         1.0,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "The minimum distance the NPC needs to move while facing the same direction before rays stop being throttled",
         null
      );
      this.getDouble(
         data,
         "ThrottleTime",
         this.throttleTime,
         0.5,
         DoubleSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "The delay between retests when an NPC is facing the same direction",
         null
      );
      this.provideFeature(Feature.Position);
      return this;
   }

   public float getAngle(@Nonnull BuilderSupport support) {
      return this.angle.get(support.getExecutionContext()) * (float) (Math.PI / 180.0);
   }

   public double getRange(@Nonnull BuilderSupport support) {
      return this.range.get(support.getExecutionContext());
   }

   public int getBlockSet(@Nonnull BuilderSupport support) {
      String blockSetId = this.blockSet.get(support.getExecutionContext());
      int index = BlockSet.getAssetMap().getIndex(blockSetId);
      if (index == Integer.MIN_VALUE) {
         throw new IllegalArgumentException("Unknown key! " + blockSetId);
      } else {
         return index;
      }
   }

   public float getMinRetestAngle(@Nonnull BuilderSupport support) {
      return this.minRetestAngle.get(support.getExecutionContext()) * (float) (Math.PI / 180.0);
   }

   public double getMinRetestMove(@Nonnull BuilderSupport support) {
      return this.minRetestMove.get(support.getExecutionContext());
   }

   public double getThrottleTime(@Nonnull BuilderSupport support) {
      return this.throttleTime.get(support.getExecutionContext());
   }

   public int getId(@Nonnull BuilderSupport support) {
      return support.getSearchRaySlot(this.id.get(support.getExecutionContext()));
   }
}
