package com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders;

import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.entity.filters.EntityFilterOr;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderEntityFilterOr extends BuilderEntityFilterMany {
   @Nonnull
   @Override
   public String getShortDescription() {
      return "Logical OR of a list of filters";
   }

   @Nonnull
   @Override
   public String getLongDescription() {
      return this.getShortDescription();
   }

   @Nullable
   public IEntityFilter build(@Nonnull BuilderSupport builderSupport) {
      List<IEntityFilter> filters = this.objectListHelper.build(builderSupport);
      return filters.isEmpty() ? null : new EntityFilterOr(filters);
   }

   @Nonnull
   @Override
   public BuilderDescriptorState getBuilderDescriptorState() {
      return BuilderDescriptorState.Stable;
   }
}
