package com.hypixel.hytale.server.npc.corecomponents.lifecycle.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.lifecycle.ActionDespawn;
import javax.annotation.Nonnull;

public class BuilderActionDespawn extends BuilderActionBase {
   protected boolean force;

   @Nonnull
   public ActionDespawn build(BuilderSupport builderSupport) {
      return new ActionDespawn(this);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Trigger the NPC to despawn";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Trigger the NPC to start the despawning cycle. If the script contains a despawn sensor it will run that action/motion before removing.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionDespawn readConfig(@Nonnull JsonElement data) {
      this.getBoolean(data, "Force", b -> this.force = b, false, BuilderDescriptorState.Stable, "Force the NPC to remove automatically", null);
      return this;
   }

   public boolean isForced() {
      return this.force;
   }
}
