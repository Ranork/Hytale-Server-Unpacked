package com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetArrayHolder;
import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.TagSetExistsValidator;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterBase;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.EntityFilterNPCGroup;
import com.hypixel.hytale.server.npc.role.support.WorldSupport;
import javax.annotation.Nonnull;

public class BuilderEntityFilterNPCGroup extends BuilderEntityFilterBase {
   protected final AssetArrayHolder includeGroups = new AssetArrayHolder();
   protected final AssetArrayHolder excludeGroups = new AssetArrayHolder();

   @Nonnull
   @Override
   public String getShortDescription() {
      return "Returns whether the entity matches one of the provided NPCGroups";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nonnull
   public IEntityFilter build(@Nonnull BuilderSupport builderSupport) {
      return new EntityFilterNPCGroup(this, builderSupport);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }

   @Nonnull
   @Override
   public Builder<IEntityFilter> readConfig(@Nonnull JsonElement data) {
      this.getAssetArray(
         data,
         "IncludeGroups",
         this.includeGroups,
         null,
         0,
         Integer.MAX_VALUE,
         TagSetExistsValidator.withConfig(AssetValidator.ListCanBeEmpty),
         BuilderDescriptorState.Stable,
         "A set of NPCGroups to include in the match",
         null
      );
      this.getAssetArray(
         data,
         "ExcludeGroups",
         this.excludeGroups,
         null,
         0,
         Integer.MAX_VALUE,
         TagSetExistsValidator.withConfig(AssetValidator.ListCanBeEmpty),
         BuilderDescriptorState.Stable,
         "A set of NPCGroups to exclude from the match",
         null
      );
      this.validateOneSetAssetArray(this.includeGroups, this.excludeGroups);
      return this;
   }

   public int[] getIncludeGroups(@Nonnull BuilderSupport builderSupport) {
      return WorldSupport.createTagSetIndexArray(this.includeGroups.get(builderSupport.getExecutionContext()));
   }

   public int[] getExcludeGroups(@Nonnull BuilderSupport builderSupport) {
      return WorldSupport.createTagSetIndexArray(this.excludeGroups.get(builderSupport.getExecutionContext()));
   }
}
