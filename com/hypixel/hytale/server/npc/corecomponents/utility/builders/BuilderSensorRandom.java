package com.hypixel.hytale.server.npc.corecomponents.utility.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.utility.SensorRandom;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorRandom extends BuilderSensorBase {
   protected final NumberArrayHolder falseRange = new NumberArrayHolder();
   protected final NumberArrayHolder trueRange = new NumberArrayHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Alternates between returning true and false for specified random durations";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorRandom(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.requireDoubleRange(
         data,
         "TrueDurationRange",
         this.trueRange,
         DoubleSequenceValidator.fromExclToInclWeaklyMonotonic(0.0, Double.MAX_VALUE),
         BuilderDescriptorState.Stable,
         "The duration range to pick a random period to return true",
         null
      );
      this.requireDoubleRange(
         data,
         "FalseDurationRange",
         this.falseRange,
         DoubleSequenceValidator.fromExclToInclWeaklyMonotonic(0.0, Double.MAX_VALUE),
         BuilderDescriptorState.Stable,
         "The duration range to pick a random period to return false",
         null
      );
      return this;
   }

   public double[] getFalseRange(@Nonnull BuilderSupport support) {
      return this.falseRange.get(support.getExecutionContext());
   }

   public double[] getTrueRange(@Nonnull BuilderSupport support) {
      return this.trueRange.get(support.getExecutionContext());
   }
}
