package com.hypixel.hytale.server.npc.corecomponents.world.builders;

import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.world.ActionResetPath;
import com.hypixel.hytale.server.npc.instructions.Action;
import javax.annotation.Nonnull;

public class BuilderActionResetPath extends BuilderActionBase {
   @Nonnull
   public Action build(BuilderSupport builderSupport) {
      return new ActionResetPath(this);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Resets the current patrol path this NPC follows.";
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
