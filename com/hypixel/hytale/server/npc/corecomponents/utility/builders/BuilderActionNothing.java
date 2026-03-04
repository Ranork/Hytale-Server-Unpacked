package com.hypixel.hytale.server.npc.corecomponents.utility.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.utility.ActionNothing;
import javax.annotation.Nonnull;

public class BuilderActionNothing extends BuilderActionBase {
   @Nonnull
   public ActionNothing build(BuilderSupport builderSupport) {
      return new ActionNothing(this);
   }

   @Nonnull
   public BuilderActionNothing readConfig(JsonElement data) {
      return this;
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Do nothing";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Do nothing. Used often as placeholder.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }
}
