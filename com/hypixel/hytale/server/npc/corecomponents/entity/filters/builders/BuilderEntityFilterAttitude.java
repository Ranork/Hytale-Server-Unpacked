package com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.core.asset.type.attitude.Attitude;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.ComponentContext;
import com.hypixel.hytale.server.npc.asset.builder.InstructionType;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumSetHolder;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.EntityFilterAttitude;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class BuilderEntityFilterAttitude extends BuilderEntityFilterBase {
   protected final EnumSetHolder<Attitude> attitudes = new EnumSetHolder<>();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Matches the attitude towards the locked target";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public IEntityFilter build(@Nonnull BuilderSupport builderSupport) {
      return new EntityFilterAttitude(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<IEntityFilter> readConfig(@Nonnull JsonElement data) {
      this.requireEnumSet(data, "Attitudes", this.attitudes, Attitude.class, BuilderDescriptorState.Stable, "The attitudes to match", null);
      this.requireContext(InstructionType.Any, ComponentContext.NotSelfEntitySensor);
      return this;
   }

   public EnumSet<Attitude> getAttitudes(@Nonnull BuilderSupport support) {
      return this.attitudes.get(support.getExecutionContext());
   }
}
