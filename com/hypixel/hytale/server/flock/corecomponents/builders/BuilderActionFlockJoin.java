package com.hypixel.hytale.server.flock.corecomponents.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.flock.corecomponents.ActionFlockJoin;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import javax.annotation.Nonnull;

public class BuilderActionFlockJoin extends BuilderActionBase {
   protected boolean forceJoin;

   @Nonnull
   public ActionFlockJoin build(BuilderSupport builderSupport) {
      return new ActionFlockJoin(this);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Join/build a flock with other entity";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return "Tries to build/join flock with target. Fails if both NPC and target are in a flock. If either NPC or target are in a flock, the one not in flock tries to join existing flock.If NPC and target are both not in a flock, a new flock with NPC is created and target is tried to be joined.Joining the flock can be rejected if the joining entity does have the correct type or the flock is full. This can be overridden by setting the ForceJoin flag to true.";
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionFlockJoin readConfig(@Nonnull JsonElement data) {
      this.getBoolean(
         data,
         "ForceJoin",
         b -> this.forceJoin = b,
         false,
         BuilderDescriptorState.Stable,
         "Enforce joining flock if true",
         "Disables checking flock join conditions test and forces joining flock."
      );
      this.requireFeature(Feature.LiveEntity);
      return this;
   }

   public boolean isForceJoin() {
      return this.forceJoin;
   }
}
