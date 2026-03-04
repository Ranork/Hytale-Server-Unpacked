package com.hypixel.hytale.server.npc.corecomponents.entity.filters.builders;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.Builder;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderObjectListHelper;
import com.hypixel.hytale.server.npc.asset.builder.validators.ArrayNotEmptyValidator;
import com.hypixel.hytale.server.npc.corecomponents.IEntityFilter;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderEntityFilterWithToggle;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

public abstract class BuilderEntityFilterMany extends BuilderEntityFilterWithToggle {
   @Nonnull
   protected BuilderObjectListHelper<IEntityFilter> objectListHelper = new BuilderObjectListHelper<>(IEntityFilter.class, this);

   @Override
   public void registerTags(@Nonnull Set<String> tags) {
      super.registerTags(tags);
      tags.add("logic");
   }

   @Nonnull
   @Override
   public Builder<IEntityFilter> readConfig(@Nonnull JsonElement data) {
      this.requireArray(
         data, "Filters", this.objectListHelper, ArrayNotEmptyValidator.get(), BuilderDescriptorState.Stable, "List of filters", null, this.validationHelper
      );
      return this;
   }

   @Override
   public boolean validate(
      String configName,
      @Nonnull NPCLoadTimeValidationHelper validationHelper,
      @Nonnull ExecutionContext context,
      Scope globalScope,
      @Nonnull List<String> errors
   ) {
      boolean result = super.validate(configName, validationHelper, context, globalScope, errors);
      validationHelper.pushFilterSet();
      result &= this.objectListHelper.validate(configName, validationHelper, this.builderManager, context, globalScope, errors);
      validationHelper.popFilterSet();
      return result;
   }
}
