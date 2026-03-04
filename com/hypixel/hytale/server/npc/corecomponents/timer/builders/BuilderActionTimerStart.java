package com.hypixel.hytale.server.npc.corecomponents.timer.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.timer.ActionTimer;
import com.hypixel.hytale.server.npc.util.Timer;
import javax.annotation.Nonnull;

public class BuilderActionTimerStart extends BuilderActionTimer {
   protected final NumberArrayHolder startValueRange = new NumberArrayHolder();
   protected final NumberArrayHolder restartValueRange = new NumberArrayHolder();
   protected final DoubleHolder rate = new DoubleHolder();
   protected final BooleanHolder repeating = new BooleanHolder();

   @Nonnull
   public ActionTimer build(@Nonnull BuilderSupport builderSupport) {
      return new ActionTimer(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Start a timer";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Start a timer";
   }

   @Nonnull
   @Override
   public BuilderActionTimer readConfig(@Nonnull JsonElement data) {
      super.readConfig(data);
      this.requireDoubleRange(
         data,
         "StartValueRange",
         this.startValueRange,
         DoubleSequenceValidator.fromExclToInclWeaklyMonotonic(0.0, Double.MAX_VALUE),
         BuilderDescriptorState.Stable,
         "The range from which to pick an initial value to start at",
         null
      );
      this.requireDoubleRange(
         data,
         "RestartValueRange",
         this.restartValueRange,
         DoubleSequenceValidator.fromExclToInclWeaklyMonotonic(0.0, Double.MAX_VALUE),
         BuilderDescriptorState.Stable,
         "The range from which to pick a value when the timer is restarted",
         "The range from which to pick a value when the timer is restarted. The upper bound is also the timer max"
      );
      this.getDouble(
         data, "Rate", this.rate, 1.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "The rate at which the timer will decrease", null
      );
      this.getBoolean(data, "Repeating", this.repeating, false, BuilderDescriptorState.Stable, "Whether to repeat the timer when countdown finishes", null);
      return this;
   }

   @Nonnull
   @Override
   public Timer.TimerAction getTimerAction() {
      return Timer.TimerAction.START;
   }

   public double[] getStartValueRange(@Nonnull BuilderSupport builderSupport) {
      return this.startValueRange.get(builderSupport.getExecutionContext());
   }

   public double[] getRestartValueRange(@Nonnull BuilderSupport builderSupport) {
      return this.restartValueRange.get(builderSupport.getExecutionContext());
   }

   public double getRate(@Nonnull BuilderSupport builderSupport) {
      return this.rate.get(builderSupport.getExecutionContext());
   }

   public boolean isRepeating(@Nonnull BuilderSupport support) {
      return this.repeating.get(support.getExecutionContext());
   }
}
