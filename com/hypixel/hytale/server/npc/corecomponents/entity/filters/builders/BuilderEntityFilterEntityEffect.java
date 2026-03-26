package com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.EntityEffectExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.EntityFilterEntityEffect;
import javax.annotation.Nonnull;

public class BuilderEntityFilterEntityEffect extends BuilderEntityFilterBase {
   protected final AssetHolder entityEffect = new AssetHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Check whether an entity has a specific entity effect";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public IEntityFilter build(@Nonnull BuilderSupport builderSupport) {
      return new EntityFilterEntityEffect(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<IEntityFilter> readConfig(@Nonnull JsonElement data) {
      this.requireAsset(
         data, "EffectId", this.entityEffect, EntityEffectExistsValidator.required(), BuilderDescriptorState.Stable, "The entity effect to check for.", null
      );
      return this;
   }

   public int getEntityEffectIndex(@Nonnull BuilderSupport support) {
      return EntityEffect.getAssetMap().getIndex(this.entityEffect.get(support.getExecutionContext()));
   }
}
