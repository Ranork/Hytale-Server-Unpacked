package com.hypixel.hytale.server.npc.corecomponents.timer.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.timer.SensorTimer;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import com.hypixel.hytale.server.npc.util.Timer;
import javax.annotation.Nonnull;

public class BuilderSensorTimer extends BuilderSensorBase {
   public static final double[] DEFAULT_TIME_ELAPSED_RANGE = new double[]{0.0, Double.MAX_VALUE};
   protected final NumberArrayHolder timeRemainingRange = new NumberArrayHolder();
   protected final StringHolder name = new StringHolder();
   protected Timer.TimerState timerState;

   @Nonnull
   public SensorTimer build(@Nonnull BuilderSupport builderSupport) {
      return new SensorTimer(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Tests if a timer exists and the value is within a certain range";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Tests if a timer exists and the value is within a certain range.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.requireString(data, "Name", this.name, StringNotEmptyValidator.get(), BuilderDescriptorState.Stable, "The name of the timer", null);
      this.getEnum(
         data,
         "State",
         e -> this.timerState = e,
         Timer.TimerState.class,
         Timer.TimerState.ANY,
         BuilderDescriptorState.Stable,
         "The timer's state to check",
         null
      );
      this.getDoubleRange(
         data,
         "TimeRemainingRange",
         this.timeRemainingRange,
         DEFAULT_TIME_ELAPSED_RANGE,
         DoubleSequenceValidator.betweenWeaklyMonotonic(0.0, Double.MAX_VALUE),
         BuilderDescriptorState.Stable,
         "The acceptable remaining time on the timer.",
         null
      );
      return this;
   }

   public double[] getRemainingTimeRange(@Nonnull BuilderSupport support) {
      return this.timeRemainingRange.get(support.getExecutionContext());
   }

   public Timer getTimer(@Nonnull BuilderSupport support) {
      return support.getTimerByName(this.name.get(support.getExecutionContext()));
   }

   public Timer.TimerState getTimerState() {
      return this.timerState;
   }
}
