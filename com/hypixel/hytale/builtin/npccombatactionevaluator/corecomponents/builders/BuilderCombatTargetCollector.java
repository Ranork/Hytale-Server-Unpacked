package com.hypixel.hytale.builtin.npccombatactionevaluator.corecomponents.builders;

import com.hypixel.hytale.builtin.npccombatactionevaluator.corecomponents.CombatTargetCollector;
import com.hypixel.hytale.server.npc.asset.builder.BuilderBase;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ISensorEntityCollector;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import javax.annotation.Nonnull;

public class BuilderCombatTargetCollector extends BuilderBase<ISensorEntityCollector> {
   @Nonnull
   public ISensorEntityCollector build(BuilderSupport builderSupport) {
      return new CombatTargetCollector();
   }

   @Nonnull
   @Override
   public Class<ISensorEntityCollector> category() {
      return ISensorEntityCollector.class;
   }

   @Override
   public boolean isEnabled(ExecutionContext context) {
      return true;
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "A collector which processes matched friendly and hostile targets and adds them to the NPC's short-term combat memory.";
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
