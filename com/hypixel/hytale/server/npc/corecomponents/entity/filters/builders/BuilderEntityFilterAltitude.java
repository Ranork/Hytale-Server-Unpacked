package com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.EntityFilterAltitude;
import javax.annotation.Nonnull;

public class BuilderEntityFilterAltitude extends BuilderEntityFilterBase {
   protected final NumberArrayHolder altitudeRange = new NumberArrayHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Matches targets if they're within the defined range above the ground";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public IEntityFilter build(@Nonnull BuilderSupport builderSupport) {
      return new EntityFilterAltitude(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<IEntityFilter> readConfig(@Nonnull JsonElement data) {
      this.requireDoubleArray(
         data,
         "AltitudeRange",
         this.altitudeRange,
         0,
         Integer.MAX_VALUE,
         DoubleSequenceValidator.betweenWeaklyMonotonic(0.0, Double.MAX_VALUE),
         BuilderDescriptorState.Stable,
         "The range above the ground to match",
         null
      );
      return this;
   }

   public double[] getAltitudeRange(@Nonnull BuilderSupport support) {
      return this.altitudeRange.get(support.getExecutionContext());
   }
}
