package com.hypixel.hytale.server.npc.corecomponents.lifecycle.builders;

import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.ActionDie;
import javax.annotation.Nonnull;

public class BuilderActionDie extends BuilderActionBase {
   @Nonnull
   public ActionDie build(BuilderSupport builderSupport) {
      return new ActionDie(this);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Kill the NPC";
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
