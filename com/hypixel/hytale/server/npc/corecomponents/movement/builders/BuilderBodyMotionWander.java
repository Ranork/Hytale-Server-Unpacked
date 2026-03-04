package com.hypixel.hytale.server.npc.corecomponents.movement.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.movement.BodyMotionWander;
import javax.annotation.Nonnull;

public class BuilderBodyMotionWander extends BuilderBodyMotionWanderBase {
   @Nonnull
   public BodyMotionWander build(@Nonnull BuilderSupport builderSupport) {
      return new BodyMotionWander(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Random movement";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Random movement in short linear pieces.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderBodyMotionWanderBase readConfig(JsonElement data) {
      return this;
   }
}
