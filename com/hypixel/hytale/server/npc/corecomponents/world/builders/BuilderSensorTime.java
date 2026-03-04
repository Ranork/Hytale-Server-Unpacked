package com.hypixel.hytale.server.npc.corecomponents.world.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.world.SensorTime;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorTime extends BuilderSensorBase {
   protected final NumberArrayHolder period = new NumberArrayHolder();
   protected boolean checkDay;
   protected boolean checkYear;
   protected boolean scaleDayTimeRange;

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorTime(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Check if the day/year time is within some specified time.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Check if the day/year time is within some specified time. If you want to check a range of time which crosses through midnight and switches to the next day, use the greater time as the min value and the lesser value as the max value.";
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
         data, "Period", this.period, DoubleSequenceValidator.between(0.0, 24.0), BuilderDescriptorState.Stable, "The time period to trigger within", null
      );
      this.getBoolean(
         data,
         "CheckDay",
         b -> this.checkDay = b,
         true,
         BuilderDescriptorState.Stable,
         "Check the day time.",
         "Check the day time. When using a double the values go from [.00, .99]. Don't get confused with there only being 60 minutes in an hour."
      );
      this.getBoolean(
         data,
         "CheckYear",
         b -> this.checkYear = b,
         false,
         BuilderDescriptorState.WorkInProgress,
         "Check the year time.",
         "Check the year time. When using a double the values go from [.00, .99]. Don't get confused with there only being 60 minutes in an hour."
      );
      this.getBoolean(
         data,
         "ScaleDayTimeRange",
         b -> this.scaleDayTimeRange = b,
         true,
         BuilderDescriptorState.Stable,
         "Whether to use a relative scale for the day time",
         "Whether to use a relative scale for the day time. Sunrise will be at relative 6, Noon at 12, and Sunset at 18, regardless of actual in-game time"
      );
      return this;
   }

   public double[] getPeriod(@Nonnull BuilderSupport support) {
      return this.period.get(support.getExecutionContext());
   }

   public boolean isCheckDay() {
      return this.checkDay;
   }

   public boolean isCheckYear() {
      return this.checkYear;
   }

   public boolean isScaleDayTimeRange() {
      return this.scaleDayTimeRange;
   }
}
