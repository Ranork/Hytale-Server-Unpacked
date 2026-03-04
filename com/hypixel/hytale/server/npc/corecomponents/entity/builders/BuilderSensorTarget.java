package com.hypixel.hytale.server.npc.corecomponents.entity.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.BuilderValidationHelper;
import com.hypixel.hytale.server.npc.asset.builder.ComponentContext;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorWithEntityFilters;
import com.hypixel.hytale.server.npc.corecomponents.entity.SensorTarget;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorTarget extends BuilderSensorWithEntityFilters {
   protected final DoubleHolder range = new DoubleHolder();
   protected final BooleanHolder autoUnlockTarget = new BooleanHolder();
   protected final StringHolder targetSlot = new StringHolder();

   @Nonnull
   public SensorTarget build(@Nonnull BuilderSupport builderSupport) {
      return new SensorTarget(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Test if given target matches a series of criteria and optional entity filters";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.getString(
         data, "TargetSlot", this.targetSlot, "LockedTarget", StringNotEmptyValidator.get(), BuilderDescriptorState.Stable, "The target slot to check", null
      );
      this.getDouble(
         data, "Range", this.range, Double.MAX_VALUE, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Maximum range of locked target", null
      );
      this.getBoolean(data, "AutoUnlockTarget", this.autoUnlockTarget, false, BuilderDescriptorState.Stable, "Unlock locked target if match fails", null);
      BuilderValidationHelper builderHelper = this.createFilterValidationHelper(ComponentContext.SensorTarget);
      this.getArray(data, "Filters", this.filters, null, BuilderDescriptorState.Stable, "A series of entity filter sensors to test", null, builderHelper);
      this.provideFeature(Feature.LiveEntity);
      return this;
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   public double getRange(@Nonnull BuilderSupport support) {
      return this.range.get(support.getExecutionContext());
   }

   public boolean getAutoUnlockTarget(@Nonnull BuilderSupport support) {
      return this.autoUnlockTarget.get(support.getExecutionContext());
   }

   public int getTargetSlot(@Nonnull BuilderSupport builderSupport) {
      return builderSupport.getTargetSlot(this.targetSlot.get(builderSupport.getExecutionContext()));
   }
}
