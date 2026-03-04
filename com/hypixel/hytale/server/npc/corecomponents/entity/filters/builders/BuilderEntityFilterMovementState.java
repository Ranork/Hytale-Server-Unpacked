package com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.EntityFilterMovementState;
import com.hypixel.hytale.server.npc.movement.MovementState;
import javax.annotation.Nonnull;

public class BuilderEntityFilterMovementState extends BuilderEntityFilterBase {
   protected MovementState movementState;

   @Nonnull
   public EntityFilterMovementState build(BuilderSupport builderSupport) {
      return new EntityFilterMovementState(this);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Check if the entity is in the given movement state";
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
      this.requireEnum(data, "State", e -> this.movementState = e, MovementState.class, BuilderDescriptorState.Stable, "The movement state to check", null);
      return this;
   }

   public MovementState getMovementState() {
      return this.movementState;
   }
}
