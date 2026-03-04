package com.hypixel.hytale.server.npc.corecomponents.combat.builders;

import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.corecomponents.combat.SensorIsBackingAway;
import javax.annotation.Nonnull;

public class BuilderSensorIsBackingAway extends BuilderSensorBase {
   @Nonnull
   public SensorIsBackingAway build(@Nonnull BuilderSupport builderSupport) {
      return new SensorIsBackingAway(this);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Test if the NPC is currently backing away from something.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Test if the NPC is currently backing away from something.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }
}
