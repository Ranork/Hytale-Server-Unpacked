package com.hypixel.hytale.server.flock.corecomponents.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.flock.corecomponents.BodyMotionFlock;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderBodyMotionBase;
import com.hypixel.hytale.server.npc.instructions.BodyMotion;
import javax.annotation.Nonnull;

public class BuilderBodyMotionFlock extends BuilderBodyMotionBase {
   @Nonnull
   public BodyMotionFlock build(BuilderSupport builderSupport) {
      return new BodyMotionFlock(this);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Flocking - WIP";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Flocking - WIP";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Experimental;
   }

   @Nonnull
   @Override
   public Builder<BodyMotion> readConfig(JsonElement data) {
      return this;
   }
}
