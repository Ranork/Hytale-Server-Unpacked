package com.hypixel.hytale.server.npc.corecomponents.world.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.world.SensorReadPosition;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorReadPosition extends BuilderSensorBase {
   protected final StringHolder slot = new StringHolder();
   protected final BooleanHolder useMarkedTarget = new BooleanHolder();
   protected final DoubleHolder range = new DoubleHolder();
   protected final DoubleHolder minRange = new DoubleHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Read a stored position with some conditions";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Sensor build(@Nonnull BuilderSupport builderSupport) {
      return new SensorReadPosition(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.requireString(data, "Slot", this.slot, StringNotEmptyValidator.get(), BuilderDescriptorState.Stable, "The slot to read the position from", null);
      this.getDouble(
         data, "MinRange", this.minRange, 0.0, DoubleSingleValidator.greaterEqual0(), BuilderDescriptorState.Stable, "Minimum range from stored position", null
      );
      this.requireDouble(data, "Range", this.range, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "Maximum range from stored position", null);
      this.getBoolean(
         data,
         "UseMarkedTarget",
         this.useMarkedTarget,
         false,
         BuilderDescriptorState.Stable,
         "Whether to read from a marked target slot instead of a position slot",
         null
      );
      this.provideFeature(Feature.Position);
      return this;
   }

   public int getSlot(@Nonnull BuilderSupport support) {
      String slotName = this.slot.get(support.getExecutionContext());
      return this.useMarkedTarget.get(support.getExecutionContext()) ? support.getTargetSlot(slotName) : support.getPositionSlot(slotName);
   }

   public boolean isUseMarkedTarget(@Nonnull BuilderSupport support) {
      return this.useMarkedTarget.get(support.getExecutionContext());
   }

   public double getMinRange(@Nonnull BuilderSupport support) {
      return this.minRange.get(support.getExecutionContext());
   }

   public double getRange(@Nonnull BuilderSupport support) {
      return this.range.get(support.getExecutionContext());
   }
}
