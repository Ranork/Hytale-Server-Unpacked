package com.hypixel.hytale.server.npc.corecomponents.debug.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.StringHolder;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.debug.ActionLog;
import javax.annotation.Nonnull;

public class BuilderActionLog extends BuilderActionBase {
   protected final StringHolder text = new StringHolder();

   @Nonnull
   public ActionLog build(@Nonnull BuilderSupport builderSupport) {
      return new ActionLog(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Log a message to console.";
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

   @Nonnull
   public BuilderActionLog readConfig(@Nonnull JsonElement data) {
      this.requireString(data, "Message", this.text, null, BuilderDescriptorState.Stable, "Text to print to console.", null);
      return this;
   }

   public String getText(@Nonnull BuilderSupport support) {
      return this.text.get(support.getExecutionContext());
   }
}
