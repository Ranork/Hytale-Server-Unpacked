package com.hypixel.hytale.builtin.npccombatactionevaluator.corecomponents.builders;

import com.hypixel.hytale.builtin.npccombatactionevaluator.corecomponents.SensorHasHostileTargetMemory;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderSensorBase;
import com.hypixel.hytale.server.npc.instructions.Sensor;
import javax.annotation.Nonnull;

public class BuilderSensorHasHostileTargetMemory extends BuilderSensorBase {
   @Nonnull
   public Sensor build(BuilderSupport builderSupport) {
      return new SensorHasHostileTargetMemory(this);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Checks if there is currently a hostile target in the target memory.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }
}
