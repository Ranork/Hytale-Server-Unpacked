package com.hypixel.hytale.server.npc.corecomponents.entity.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.DoubleHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSingleValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.ActionOverrideAttitude;
import com.hypixel.hytale.server.npc.instructions.Action;
import javax.annotation.Nonnull;

public class BuilderActionOverrideAttitude extends BuilderActionBase {
   protected final EnumHolder<Attitude> attitude = new EnumHolder<>();
   protected final DoubleHolder duration = new DoubleHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Override this NPCs attitude towards the provided target for a given duration";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public Action build(@Nonnull BuilderSupport builderSupport) {
      return new ActionOverrideAttitude(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   public BuilderActionOverrideAttitude readConfig(@Nonnull JsonElement data) {
      this.requireEnum(data, "Attitude", this.attitude, Attitude.class, BuilderDescriptorState.Stable, "The attitude to set", null);
      this.getDouble(
         data, "Duration", this.duration, 10.0, DoubleSingleValidator.greater0(), BuilderDescriptorState.Stable, "The duration to override for", null
      );
      this.requireFeature(Feature.LiveEntity);
      return this;
   }

   public Attitude getAttitude(@Nonnull BuilderSupport support) {
      return this.attitude.get(support.getExecutionContext());
   }

   public double getDuration(@Nonnull BuilderSupport support) {
      return this.duration.get(support.getExecutionContext());
   }
}
