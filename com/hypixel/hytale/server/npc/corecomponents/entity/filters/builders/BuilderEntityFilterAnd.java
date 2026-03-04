package com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders;

import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.EntityFilterAnd;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderEntityFilterAnd extends BuilderEntityFilterMany {
   @Nonnull
   @Override
   public String getShortDescription() {
      return "Logical AND of a list of filters";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nullable
   public IEntityFilter build(@Nonnull BuilderSupport builderSupport) {
      List<IEntityFilter> filters = this.objectListHelper.build(builderSupport);
      return filters.isEmpty() ? null : new EntityFilterAnd(filters);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }
}
