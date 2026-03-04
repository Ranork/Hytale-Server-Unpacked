package com.hypixel.hytale.server.npc.corecomponents.entity.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.BuilderValidationHelper;
import com.hypixel.hytale.server.npc.asset.builder.ComponentContext;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorWithEntityFilters;
import com.hypixel.hytale.server.npc.corecomponents.entity.SensorSelf;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorSelf extends BuilderSensorWithEntityFilters {
   @Nonnull
   public SensorSelf build(@Nonnull BuilderSupport builderSupport) {
      return new SensorSelf(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Test if the NPC itself matches a set of entity filters";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      BuilderValidationHelper builderHelper = this.createFilterValidationHelper(ComponentContext.SensorSelf);
      this.requireArray(data, "Filters", this.filters, null, BuilderDescriptorState.Stable, "A series of entity filter sensors to test", null, builderHelper);
      this.provideFeature(Feature.Position);
      return this;
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }
}
