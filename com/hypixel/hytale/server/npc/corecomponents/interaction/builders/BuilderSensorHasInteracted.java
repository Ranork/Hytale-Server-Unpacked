package com.hypixel.hytale.server.npc.corecomponents.interaction.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.InstructionType;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.interaction.SensorHasInteracted;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class BuilderSensorHasInteracted extends BuilderSensorBase {
   @Nonnull
   @Override
   public String getShortDescription() {
      return "Checks whether the currently iterated player in the interaction instruction has interacted with this NPC";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Sensor build(BuilderSupport builderSupport) {
      return new SensorHasInteracted(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(JsonElement data) {
      this.requireInstructionType(EnumSet.of(InstructionType.Interaction));
      return this;
   }
}
