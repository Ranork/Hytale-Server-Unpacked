package com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.EnumHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.NumberArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleSequenceValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.EntityStatExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.EntityFilterStat;
import javax.annotation.Nonnull;

public class BuilderEntityFilterStat extends BuilderEntityFilterBase {
   protected final AssetHolder stat = new AssetHolder();
   protected final EnumHolder<EntityFilterStat.EntityStatTarget> statTarget = new EnumHolder<>();
   protected final AssetHolder relativeTo = new AssetHolder();
   protected final EnumHolder<EntityFilterStat.EntityStatTarget> relativeToTarget = new EnumHolder<>();
   protected final NumberArrayHolder valueRange = new NumberArrayHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Match stat values of the entity";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public IEntityFilter build(@Nonnull BuilderSupport builderSupport) {
      return new EntityFilterStat(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<IEntityFilter> readConfig(@Nonnull JsonElement data) {
      this.requireAsset(data, "Stat", this.stat, EntityStatExistsValidator.required(), BuilderDescriptorState.Stable, "The stat value to check", null);
      this.requireEnum(data, "StatTarget", this.statTarget, EntityFilterStat.EntityStatTarget.class, BuilderDescriptorState.Stable, "The stat target", null);
      this.requireAsset(
         data, "RelativeTo", this.relativeTo, EntityStatExistsValidator.required(), BuilderDescriptorState.Stable, "The stat value to check against", null
      );
      this.requireEnum(
         data, "RelativeToTarget", this.relativeToTarget, EntityFilterStat.EntityStatTarget.class, BuilderDescriptorState.Stable, "The stat target", null
      );
      this.requireDoubleRange(
         data,
         "ValueRange",
         this.valueRange,
         DoubleSequenceValidator.betweenWeaklyMonotonic(0.0, Double.MAX_VALUE),
         BuilderDescriptorState.Stable,
         "The fractional range within which to trigger, with 1 being equal",
         null
      );
      return this;
   }

   public int getStat(@Nonnull BuilderSupport support) {
      return EntityStatType.getAssetMap().getIndex(this.stat.get(support.getExecutionContext()));
   }

   public EntityFilterStat.EntityStatTarget getStatTarget(@Nonnull BuilderSupport support) {
      return this.statTarget.get(support.getExecutionContext());
   }

   public int getRelativeTo(@Nonnull BuilderSupport support) {
      return EntityStatType.getAssetMap().getIndex(this.relativeTo.get(support.getExecutionContext()));
   }

   public EntityFilterStat.EntityStatTarget getRelativeToTarget(@Nonnull BuilderSupport support) {
      return this.relativeToTarget.get(support.getExecutionContext());
   }

   public double[] getValueRange(@Nonnull BuilderSupport support) {
      return this.valueRange.get(support.getExecutionContext());
   }
}
