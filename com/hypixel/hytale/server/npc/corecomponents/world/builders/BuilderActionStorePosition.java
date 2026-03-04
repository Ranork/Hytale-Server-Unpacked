package com.hypixel.hytale.server.npc.corecomponents.world.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.world.ActionStorePosition;
import com.hypixel.hytale.server.npc.instructions.Action;
import javax.annotation.Nonnull;

public class BuilderActionStorePosition extends BuilderActionBase {
   protected final StringHolder slot = new StringHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Store the position from the attached sensor";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionStorePosition(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionStorePosition readConfig(@Nonnull JsonElement data) {
      this.requireString(data, "Slot", this.slot, StringNotEmptyValidator.get(), BuilderDescriptorState.Stable, "The slot to store the position in", null);
      return this;
   }

   public int getSlot(@Nonnull BuilderSupport support) {
      return support.getPositionSlot(this.slot.get(support.getExecutionContext()));
   }
}
