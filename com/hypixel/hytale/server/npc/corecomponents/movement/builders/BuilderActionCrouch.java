package com.hypixel.hytale.server.npc.corecomponents.movement.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.movement.ActionCrouch;
import com.hypixel.hytale.server.npc.instructions.Action;
import javax.annotation.Nonnull;

public class BuilderActionCrouch extends BuilderActionBase {
   protected final BooleanHolder crouching = new BooleanHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Set NPC crouching state";
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
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionCrouch(this, this.crouching.get(builderSupport.getExecutionContext()));
   }

   @Nonnull
   @Override
   public Builder<Action> readConfig(@Nonnull JsonElement data) {
      this.getBoolean(data, "Crouch", this.crouching, true, BuilderDescriptorState.Stable, "True for crouching, false for non-crouching", null);
      return this;
   }
}
