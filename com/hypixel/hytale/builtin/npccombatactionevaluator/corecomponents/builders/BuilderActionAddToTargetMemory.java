package com.hypixel.hytale.builtin.npccombatactionevaluator.corecomponents.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.builtin.npccombatactionevaluator.corecomponents.ActionAddToTargetMemory;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import javax.annotation.Nonnull;

public class BuilderActionAddToTargetMemory extends BuilderActionBase {
   @Nonnull
   @Override
   public String getShortDescription() {
      return "Adds the passed target from the sensor to the hostile target memory";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(BuilderSupport builderSupport) {
      return new ActionAddToTargetMemory(this);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<Action> readConfig(JsonElement data) {
      this.requireFeature(Feature.LiveEntity);
      return this;
   }
}
