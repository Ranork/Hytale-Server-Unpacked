package com.hypixel.hytale.builtin.npccombatactionevaluator.corecomponents.builders;

import com.hypixel.hytale.builtin.npccombatactionevaluator.corecomponents.ActionCombatAbility;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import javax.annotation.Nonnull;

public class BuilderActionCombatAbility extends BuilderActionBase {
   @Nonnull
   public ActionCombatAbility build(@Nonnull BuilderSupport builderSupport) {
      return new ActionCombatAbility(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Starts the combat ability selected by the combat action evaluator.";
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
