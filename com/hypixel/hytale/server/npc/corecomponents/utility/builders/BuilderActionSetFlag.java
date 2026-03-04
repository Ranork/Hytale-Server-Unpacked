package com.hypixel.hytale.server.npc.corecomponents.utility.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.utility.ActionSetFlag;
import com.hypixel.hytale.server.npc.instructions.Action;
import javax.annotation.Nonnull;

public class BuilderActionSetFlag extends BuilderActionBase {
   protected final StringHolder name = new StringHolder();
   protected final BooleanHolder value = new BooleanHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Set a named flag to a boolean value";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionSetFlag(this, builderSupport);
   }

   @Nonnull
   public BuilderActionSetFlag readConfig(@Nonnull JsonElement data) {
      this.requireString(data, "Name", this.name, StringNotEmptyValidator.get(), BuilderDescriptorState.Stable, "The name of the flag", null);
      this.getBoolean(data, "SetTo", this.value, true, BuilderDescriptorState.Stable, "The value to set the flag to", null);
      return this;
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   public int getFlagSlot(@Nonnull BuilderSupport support) {
      String flag = this.name.get(support.getExecutionContext());
      return support.getFlagSlot(flag);
   }

   public boolean getValue(@Nonnull BuilderSupport support) {
      return this.value.get(support.getExecutionContext());
   }
}
