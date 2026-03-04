package com.hypixel.hytale.server.npc.corecomponents.movement.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.movement.SensorOnGround;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorOnGround extends BuilderSensorBase {
   @Nonnull
   public SensorOnGround build(BuilderSupport builderSupport) {
      return new SensorOnGround(this);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Test if NPC is on ground";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Return true if NPC is on ground. No target is returned.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Sensor> readConfig(JsonElement data) {
      return this;
   }
}
