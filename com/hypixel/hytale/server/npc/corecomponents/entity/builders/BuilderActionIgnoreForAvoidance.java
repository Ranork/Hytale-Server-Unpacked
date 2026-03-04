package com.hypixel.hytale.server.npc.corecomponents.entity.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.ActionIgnoreForAvoidance;
import com.hypixel.hytale.server.npc.instructions.Action;
import javax.annotation.Nonnull;

public class BuilderActionIgnoreForAvoidance extends BuilderActionBase {
   protected final StringHolder targetSlot = new StringHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Set the target slot of an entity that should be ignored during avoidance";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionIgnoreForAvoidance(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionIgnoreForAvoidance readConfig(@Nonnull JsonElement data) {
      this.requireString(
         data,
         "TargetSlot",
         this.targetSlot,
         StringNotEmptyValidator.get(),
         BuilderDescriptorState.Stable,
         "The target slot containing the entity to be ignored",
         null
      );
      return this;
   }

   public int getTargetSlot(@Nonnull BuilderSupport support) {
      return support.getTargetSlot(this.targetSlot.get(support.getExecutionContext()));
   }
}
