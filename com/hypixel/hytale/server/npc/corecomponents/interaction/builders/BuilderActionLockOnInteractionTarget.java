package com.hypixel.hytale.server.npc.corecomponents.interaction.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.InstructionType;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.interaction.ActionLockOnInteractionTarget;
import com.hypixel.hytale.server.npc.instructions.Action;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class BuilderActionLockOnInteractionTarget extends BuilderActionBase {
   protected final StringHolder targetSlot = new StringHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Locks on to the currently iterated player in the interaction instruction";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionLockOnInteractionTarget(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionLockOnInteractionTarget readConfig(@Nonnull JsonElement data) {
      this.getString(
         data, "TargetSlot", this.targetSlot, "LockedTarget", StringNotEmptyValidator.get(), BuilderDescriptorState.Stable, "The target slot to use", null
      );
      this.requireInstructionType(EnumSet.of(InstructionType.Interaction));
      return this;
   }

   public int getTargetSlot(@Nonnull BuilderSupport support) {
      return support.getTargetSlot(this.targetSlot.get(support.getExecutionContext()));
   }
}
