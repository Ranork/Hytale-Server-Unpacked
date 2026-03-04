package com.hypixel.hytale.server.flock.corecomponents.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.flock.corecomponents.EntityFilterFlock;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.hypixel.hytale.server.npc.movement.FlockMembershipType;
import com.hypixel.hytale.server.npc.movement.FlockPlayerMembership;
import javax.annotation.Nonnull;

public class BuilderEntityFilterFlock extends BuilderEntityFilterBase {
   protected FlockMembershipType flockMembership;
   protected FlockPlayerMembership flockPlayerMembership;
   protected int[] size;
   protected boolean checkCanJoin;

   @Nonnull
   public IEntityFilter build(BuilderSupport builderSupport) {
      return new EntityFilterFlock(this);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Test for flock membership and related properties";
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
   @Override
   public Builder<IEntityFilter> readConfig(@Nonnull JsonElement data) {
      this.getEnum(
         data,
         "FlockStatus",
         v -> this.flockMembership = v,
         FlockMembershipType.class,
         FlockMembershipType.Any,
         BuilderDescriptorState.Stable,
         "Test for NPC status in relation to flock",
         null
      );
      this.getEnum(
         data,
         "FlockPlayerStatus",
         v -> this.flockPlayerMembership = v,
         FlockPlayerMembership.class,
         FlockPlayerMembership.Any,
         BuilderDescriptorState.Stable,
         "Test for Player status for flock NPC is member",
         null
      );
      this.getIntRange(data, "Size", v -> this.size = v, null, null, BuilderDescriptorState.Stable, "Check for a certain range of NPCs in the flock", null);
      this.getBoolean(
         data,
         "CheckCanJoin",
         v -> this.checkCanJoin = v,
         false,
         BuilderDescriptorState.Stable,
         "If true, will filter entities in a flock the executor can join",
         null
      );
      return this;
   }

   public int[] getSize() {
      return this.size;
   }

   public FlockMembershipType getFlockMembership() {
      return this.flockMembership;
   }

   public FlockPlayerMembership getFlockPlayerMembership() {
      return this.flockPlayerMembership;
   }

   public boolean isCheckCanJoin() {
      return this.checkCanJoin;
   }
}
