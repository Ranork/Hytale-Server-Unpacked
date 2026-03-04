package com.hypixel.hytale.server.npc.corecomponents.world.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.IntHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleRangeValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.IntSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderHeadMotionBase;
import com.hypixel.hytale.server.npc.corecomponents.world.HeadMotionObserve;
import javax.annotation.Nonnull;

public class BuilderHeadMotionObserve extends BuilderHeadMotionBase {
   protected static final double[] DEFAULT_PAUSE_TIME_RANGE = new double[]{2.0, 2.0};
   protected final NumberArrayHolder angleRange = new NumberArrayHolder();
   protected final NumberArrayHolder pauseTimeRange = new NumberArrayHolder();
   protected final BooleanHolder pickRandomAngle = new BooleanHolder();
   protected final IntHolder viewSegments = new IntHolder();
   protected final DoubleHolder relativeTurnSpeed = new DoubleHolder();

   @Nonnull
   public HeadMotionObserve build(@Nonnull BuilderSupport builderSupport) {
      return new HeadMotionObserve(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Observe surroundings in various ways.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Observe surroundings in various ways. This includes looking in random directions within an angle, or sweeping the head left and right, etc. Angles are relative to body angle at any given time.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderHeadMotionObserve readConfig(@Nonnull JsonElement data) {
      this.requireDoubleRange(
         data,
         "AngleRange",
         this.angleRange,
         DoubleSequenceValidator.betweenWeaklyMonotonic(-180.0, 180.0),
         BuilderDescriptorState.Stable,
         "The angle range to observe in degrees",
         "The angle range to observe in degrees, offset from facing forwards"
      );
      this.getDoubleRange(
         data,
         "PauseTimeRange",
         this.pauseTimeRange,
         DEFAULT_PAUSE_TIME_RANGE,
         DoubleSequenceValidator.betweenWeaklyMonotonic(0.0, Double.MAX_VALUE),
         BuilderDescriptorState.Stable,
         "How long to continue looking in a given direction",
         null
      );
      this.getBoolean(
         data,
         "PickRandomAngle",
         this.pickRandomAngle,
         false,
         BuilderDescriptorState.Stable,
         "Whether to pick random angles within the range",
         "Whether to pick random angles within the range. If false, will instead sweep across the range, pausing at either end."
      );
      this.getInt(
         data,
         "ViewSegments",
         this.viewSegments,
         1,
         IntSingleValidator.greater0(),
         BuilderDescriptorState.Stable,
         "The number of distinct segments to stop at when sweeping from left to right",
         null
      );
      this.getDouble(
         data,
         "RelativeTurnSpeed",
         this.relativeTurnSpeed,
         1.0,
         DoubleRangeValidator.fromExclToIncl(0.0, 2.0),
         BuilderDescriptorState.Stable,
         "The relative turn speed modifier",
         null
      );
      return this;
   }

   public float[] getAngleRange(@Nonnull BuilderSupport support) {
      double[] range = this.angleRange.get(support.getExecutionContext());
      return new float[]{(float)(range[0] * (float) (Math.PI / 180.0)), (float)(range[1] * (float) (Math.PI / 180.0))};
   }

   public double[] getPauseTimeRange(@Nonnull BuilderSupport support) {
      return this.pauseTimeRange.get(support.getExecutionContext());
   }

   public boolean isPickRandomAngle(@Nonnull BuilderSupport support) {
      return this.pickRandomAngle.get(support.getExecutionContext());
   }

   public int getViewSegments(@Nonnull BuilderSupport support) {
      return this.viewSegments.get(support.getExecutionContext());
   }

   public double getRelativeTurnSpeed(@Nonnull BuilderSupport support) {
      return this.relativeTurnSpeed.get(support.getExecutionContext());
   }
}
