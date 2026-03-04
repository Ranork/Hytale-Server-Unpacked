package com.hypixel.hytale.server.npc.corecomponents.combat.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.Feature;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.EntityEffectExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.combat.ActionApplyEntityEffect;
import javax.annotation.Nonnull;

public class BuilderActionApplyEntityEffect extends BuilderActionBase {
   protected final AssetHolder entityEffect = new AssetHolder();
   protected final BooleanHolder useTarget = new BooleanHolder();

   @Nonnull
   public ActionApplyEntityEffect build(@Nonnull BuilderSupport builderSupport) {
      return new ActionApplyEntityEffect(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Applies an entity effect to the target or self";
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
   public BuilderActionApplyEntityEffect readConfig(@Nonnull JsonElement data) {
      this.requireAsset(
         data, "EntityEffect", this.entityEffect, EntityEffectExistsValidator.required(), BuilderDescriptorState.Stable, "The entity effect to apply", null
      );
      this.getBoolean(
         data, "UseTarget", this.useTarget, true, BuilderDescriptorState.Stable, "Use the sensor-provided target for the action, self otherwise", null
      );
      this.requireFeatureIf(this.useTarget, true, Feature.LiveEntity);
      return this;
   }

   public int getEntityEffect(@Nonnull BuilderSupport support) {
      return EntityEffect.getAssetMap().getIndex(this.entityEffect.get(support.getExecutionContext()));
   }

   public boolean isUseTarget(@Nonnull BuilderSupport support) {
      return this.useTarget.get(support.getExecutionContext());
   }
}
