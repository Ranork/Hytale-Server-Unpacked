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

public class BuilderActionTimerModify extends BuilderActionTimer {
   private static final double[] DEFAULT_RESTART_VALUE_RANGE = new double[]{0.0, 0.0};
   protected final DoubleHolder increaseValue = new DoubleHolder();
   protected final DoubleHolder setValue = new DoubleHolder();
   protected final NumberArrayHolder restartValueRange = new NumberArrayHolder();
   protected final DoubleHolder rate = new DoubleHolder();
   protected final BooleanHolder repeating = new BooleanHolder();
   protected boolean modifyRepeating;

   @Nonnull
   public ActionTimer build(@Nonnull BuilderSupport builderSupport) {
      return new ActionTimer(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Modify values of a timer";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Modify values of a timer";
   }

   @Nonnull
   @Override
   public BuilderActionTimer readConfig(@Nonnull JsonElement data) {
      super.readConfig(data);
      this.getDouble(
         data, "AddValue", this.increaseValue, 0.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "Add value to the timer", null
      );
      this.getDoubleRange(
         data,
         "MaxValue",
         this.restartValueRange,
         DEFAULT_RESTART_VALUE_RANGE,
         DoubleSequenceValidator.between(0.0, Double.MAX_VALUE),
         BuilderDescriptorState.Stable,
         "Set the restart value range the timer can have",
         "Set the restart value range the timer can have. If [ 0, 0 ] (default) it will be ignored"
      );
      this.getDouble(
         data,
         "Rate",
         this.rate,
         0.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Stable,
         "Set the rate at which the timer will decrease",
         "Set the rate at which the timer will decrease. If 0 (default) it will be ignored"
      );
      this.getDouble(
         data,
         "SetValue",
         this.setValue,
         0.0,
         DoubleSingleValidator.greaterEqual0(),
         BuilderDescriptorState.Stable,
         "Set the value of the timer",
         "Set the value of the timer. If 0 (default) it will be ignored"
      );
      this.modifyRepeating = this.getBoolean(
         data, "Repeating", this.repeating, false, BuilderDescriptorState.Stable, "Whether to repeat the timer when countdown finishes", null
      );
      return this;
   }

   @Nonnull
   @Override
   public Timer.TimerAction getTimerAction() {
      return Timer.TimerAction.MODIFY;
   }

   public double getIncreaseValue(@Nonnull BuilderSupport builderSupport) {
      return this.increaseValue.get(builderSupport.getExecutionContext());
   }

   public double[] getRestartValueRange(@Nonnull BuilderSupport builderSupport) {
      return this.restartValueRange.get(builderSupport.getExecutionContext());
   }

   public double getRate(@Nonnull BuilderSupport builderSupport) {
      return this.rate.get(builderSupport.getExecutionContext());
   }

   public double getSetValue(@Nonnull BuilderSupport builderSupport) {
      return this.setValue.get(builderSupport.getExecutionContext());
   }

   public boolean isModifyRepeating() {
      return this.modifyRepeating;
   }

   public boolean isRepeating(@Nonnull BuilderSupport support) {
      return this.repeating.get(support.getExecutionContext());
   }
}
