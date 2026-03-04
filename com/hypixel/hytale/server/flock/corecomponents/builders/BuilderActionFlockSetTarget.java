package com.hypixel.hytale.server.flock.corecomponents.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.flock.corecomponents.ActionFlockSetTarget;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import javax.annotation.Nonnull;

public class BuilderActionFlockSetTarget extends BuilderActionBase {
   protected boolean clear;
   protected final StringHolder targetSlot = new StringHolder();

   @Nonnull
   public ActionFlockSetTarget build(@Nonnull BuilderSupport builderSupport) {
      return new ActionFlockSetTarget(this, builderSupport);
   }

   @Nonnull
   public BuilderActionFlockSetTarget readConfig(@Nonnull JsonElement data) {
      this.getBoolean(
         data,
         "Clear",
         v -> this.clear = v,
         false,
         BuilderDescriptorState.Stable,
         "Clear locked target if true else set.",
         "If true, clear locked target. If false, set to current target."
      );
      this.getString(
         data, "TargetSlot", this.targetSlot, "LockedTarget", StringNotEmptyValidator.get(), BuilderDescriptorState.Stable, "The target slot to use", null
      );
      this.requireFeature(Feature.LiveEntity);
      return this;
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Set or clear locked target for flock.";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Sets or clears the locked target for the flock the NPC is member of. If Clear flag is true, the locked target is cleared otherwise it is set to the the target.The flock leader is explicitly excluded from this operation.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   public boolean isClear() {
      return this.clear;
   }

   public String getTargetSlot(@Nonnull BuilderSupport support) {
      return this.targetSlot.get(support.getExecutionContext());
   }
}
