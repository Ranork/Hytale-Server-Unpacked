package com.hypixel.hytale.server.flock.corecomponents.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.flock.corecomponents.ActionFlockLeave;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import javax.annotation.Nonnull;

public class BuilderActionFlockLeave extends BuilderActionBase {
   @Nonnull
   public ActionFlockLeave build(BuilderSupport builderSupport) {
      return new ActionFlockLeave(this);
   }

   @Nonnull
   public BuilderActionFlockLeave readConfig(JsonElement data) {
      return this;
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Leave flock.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "NPC leaves flock currently in. Does nothing when not in flock.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }
}
