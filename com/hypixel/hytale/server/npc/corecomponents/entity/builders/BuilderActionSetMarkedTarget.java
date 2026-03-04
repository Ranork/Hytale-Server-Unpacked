package com.hypixel.hytale.server.npc.corecomponents.entity.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.ActionSetMarkedTarget;
import com.hypixel.hytale.server.npc.instructions.Action;
import javax.annotation.Nonnull;

public class BuilderActionSetMarkedTarget extends BuilderActionBase {
   protected final StringHolder targetSlot = new StringHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Explicitly sets a marked target in a given slot.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionSetMarkedTarget(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionSetMarkedTarget readConfig(@Nonnull JsonElement data) {
      this.getString(
         data,
         "TargetSlot",
         this.targetSlot,
         "LockedTarget",
         StringNotEmptyValidator.get(),
         BuilderDescriptorState.Stable,
         "The target slot to set a target to.",
         null
      );
      this.requireFeature(Feature.LiveEntity);
      return this;
   }

   public int getTargetSlot(@Nonnull BuilderSupport support) {
      return support.getTargetSlot(this.targetSlot.get(support.getExecutionContext()));
   }
}
