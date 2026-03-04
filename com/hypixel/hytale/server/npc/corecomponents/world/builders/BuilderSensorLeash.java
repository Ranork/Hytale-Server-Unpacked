package com.hypixel.hytale.server.npc.corecomponents.world.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.world.SensorLeash;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorLeash extends BuilderSensorBase {
   protected final DoubleHolder range = new DoubleHolder();

   @Nonnull
   public SensorLeash build(@Nonnull BuilderSupport builderSupport) {
      builderSupport.setRequireLeashPosition();
      return new SensorLeash(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Triggers when the NPC is outside a specified range from the leash point";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Triggers when the NPC is outside a specified range from the leash point";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.requireDouble(
         data, "Range", this.range, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "The farthest distance allowed from the leash point", null
      );
      this.provideFeature(Feature.Position);
      return this;
   }

   public double getRange(@Nonnull BuilderSupport builderSupport) {
      return this.range.get(builderSupport.getExecutionContext());
   }
}
