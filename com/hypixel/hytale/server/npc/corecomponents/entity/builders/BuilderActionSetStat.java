package com.hypixel.hytale.server.npc.corecomponents.entity.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.BooleanHolder;
import com.hypixel.hytale.server.npc.asset.builder.holder.FloatHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.asset.EntityStatExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.ActionSetStat;
import javax.annotation.Nonnull;

public class BuilderActionSetStat extends BuilderActionBase {
   protected final AssetHolder stat = new AssetHolder();
   protected final FloatHolder value = new FloatHolder();
   protected final BooleanHolder add = new BooleanHolder();

   @Nonnull
   public ActionSetStat build(@Nonnull BuilderSupport builderSupport) {
      return new ActionSetStat(this, builderSupport);
   }

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Sets (or adds to) an entity stat on the NPC.";
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
   public BuilderActionSetStat readConfig(@Nonnull JsonElement data) {
      this.requireAsset(data, "Stat", this.stat, EntityStatExistsValidator.required(), BuilderDescriptorState.Stable, "The entity stat to affect.", null);
      this.requireFloat(data, "Value", this.value, null, BuilderDescriptorState.Stable, "The value to set the stat to.", null);
      this.getBoolean(data, "Add", this.add, false, BuilderDescriptorState.Stable, "Add the value to the existing value instead of setting it.", null);
      return this;
   }

   public int getStat(@Nonnull BuilderSupport support) {
      return EntityStatType.getAssetMap().getIndex(this.stat.get(support.getExecutionContext()));
   }

   public float getValue(@Nonnull BuilderSupport support) {
      return this.value.get(support.getExecutionContext());
   }

   public boolean isAdd(@Nonnull BuilderSupport support) {
      return this.add.get(support.getExecutionContext());
   }
}
