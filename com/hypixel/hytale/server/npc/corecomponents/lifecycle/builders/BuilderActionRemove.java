package com.hypixel.hytale.server.npc.corecomponents.lifecycle.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.ActionRemove;
import com.hypixel.hytale.server.npc.instructions.Action;
import javax.annotation.Nonnull;

public class BuilderActionRemove extends BuilderActionBase {
   protected final BooleanHolder useTarget = new BooleanHolder();

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionRemove(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Erase the target entity from the world (no death animation).";
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

   @Nonnull
   @Override
   public Builder<Action> readConfig(@Nonnull JsonElement data) {
      this.getBoolean(data, "UseTarget", this.useTarget, true, BuilderDescriptorState.Stable, "Use the sensor-provided target for the action", null);
      this.requireFeatureIf(this.useTarget, true, Feature.LiveEntity);
      return this;
   }

   public boolean getUseTarget(@Nonnull BuilderSupport support) {
      return this.useTarget.get(support.getExecutionContext());
   }
}
