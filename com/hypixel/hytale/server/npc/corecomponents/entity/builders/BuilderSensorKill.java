package com.hypixel.hytale.server.npc.corecomponents.entity.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNullOrNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.SensorKill;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorKill extends BuilderSensorBase {
   protected final StringHolder targetSlot = new StringHolder();

   @Nonnull
   public SensorKill build(@Nonnull BuilderSupport builderSupport) {
      return new SensorKill(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Test if NPC made a kill";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Return true if NPC made a kill. Target position is killed entity position.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(@Nonnull JsonElement data) {
      this.getString(
         data,
         "TargetSlot",
         this.targetSlot,
         null,
         StringNullOrNotEmptyValidator.get(),
         BuilderDescriptorState.Stable,
         "The target slot to check if killed. If omitted, will accept any entity killed",
         null
      );
      this.provideFeature(Feature.Position);
      return this;
   }

   public int getTargetSlot(@Nonnull BuilderSupport support) {
      String slot = this.targetSlot.get(support.getExecutionContext());
      return slot == null ? Integer.MIN_VALUE : support.getTargetSlot(slot);
   }
}
