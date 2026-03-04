package com.hypixel.hytale.server.npc.corecomponents.movement.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.movement.ActionOverrideAltitude;
import com.hypixel.hytale.server.npc.instructions.Action;
import javax.annotation.Nonnull;

public class BuilderActionOverrideAltitude extends BuilderActionBase {
   protected final NumberArrayHolder desiredAltitudeRange = new NumberArrayHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Temporarily override the preferred altitude of a flying NPC";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Temporarily override the preferred altitude of a flying NPC. Must be refreshed each tick";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionOverrideAltitude(this, builderSupport);
   }

   @Nonnull
   @Override
   public Builder<Action> readConfig(@Nonnull JsonElement data) {
      this.requireDoubleRange(
         data,
         "DesiredAltitudeRange",
         this.desiredAltitudeRange,
         DoubleSequenceValidator.betweenWeaklyMonotonic(0.0, Double.MAX_VALUE),
         BuilderDescriptorState.Stable,
         "The desired altitude range",
         null
      );
      return this;
   }

   public double[] getDesiredAltitudeRange(@Nonnull BuilderSupport support) {
      return this.desiredAltitudeRange.get(support.getExecutionContext());
   }
}
