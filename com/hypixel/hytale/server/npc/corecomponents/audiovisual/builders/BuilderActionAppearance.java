package com.hypixel.hytale.server.npc.corecomponents.audiovisual.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.ModelExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.audiovisual.ActionAppearance;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import javax.annotation.Nonnull;

public class BuilderActionAppearance extends BuilderActionBase {
   protected String appearance;

   @Nonnull
   public ActionAppearance build(BuilderSupport builderSupport) {
      return new ActionAppearance(this);
   }

   @Nonnull
   public BuilderActionAppearance readConfig(@Nonnull JsonElement data) {
      this.requireAsset(data, "Appearance", s -> this.appearance = s, ModelExistsValidator.required(), BuilderDescriptorState.Stable, "Model name to use", null);
      return this;
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Set model displayed for NPC";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Change model of NPC to given appearance.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   public String getAppearance() {
      return this.appearance;
   }
}
