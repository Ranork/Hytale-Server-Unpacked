package com.hypixel.hytale.server.npc.corecomponents.timer.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectReferenceHelper;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderMotionBase;
import com.hypixel.hytale.server.npc.instructions.Motion;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class BuilderMotionTimer<T extends Motion> extends BuilderMotionBase<T> {
   public static final double[] DEFAULT_TIMER_RANGE = new double[]{1.0, 1.0};
   protected final NumberArrayHolder timerRange = new NumberArrayHolder();
   protected BuilderObjectReferenceHelper<T> motion;

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Execute a Motion for a specific maximum time";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Execute a Motion for a specific maximum time. If the motion finishes earlier the Timer also finishes.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderMotionTimer<T> readConfig(@Nonnull JsonElement data) {
      this.getDoubleRange(
         data,
         "Time",
         this.timerRange,
         DEFAULT_TIMER_RANGE,
         DoubleSequenceValidator.betweenWeaklyMonotonic(0.0, Double.MAX_VALUE),
         BuilderDescriptorState.Stable,
         "Range of time from which the random timer length can be chosen",
         null
      );
      this.requireObject(data, "Motion", this.motion, BuilderDescriptorState.Stable, "Motion to execute", null, this.validationHelper);
      return this;
   }

   @Override
   public boolean validate(
      String configName,
      @Nonnull NPCLoadTimeValidationHelper validationHelper,
      @Nonnull ExecutionContext context,
      Scope globalScope,
      @Nonnull List<String> errors
   ) {
      return super.validate(configName, validationHelper, context, globalScope, errors)
         & this.motion.validate(configName, validationHelper, this.builderManager, context, globalScope, errors);
   }

   public double[] getTimerRange(@Nonnull BuilderSupport support) {
      return this.timerRange.get(support.getExecutionContext());
   }

   @Nullable
   public T getMotion(@Nonnull BuilderSupport builderSupport) {
      return this.motion.build(builderSupport);
   }
}
