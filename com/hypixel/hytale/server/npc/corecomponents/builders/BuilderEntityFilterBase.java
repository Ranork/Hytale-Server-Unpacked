package com.hypixel.hytale.server.npc.corecomponents.builders;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import javax.annotation.Nonnull;

public abstract class BuilderEntityFilterBase extends BuilderEntityFilterWithToggle {
   @Override
   public boolean validate(
      String configName, @Nonnull NPCLoadTimeValidationHelper validationHelper, ExecutionContext context, Scope globalScope, @Nonnull List<String> errors
   ) {
      boolean result = super.validate(configName, validationHelper, context, globalScope, errors);
      String type = this.getTypeName();
      if (validationHelper.isFilterExternallyProvided(type)) {
         result = false;
         errors.add(
            String.format(
               "%s: includes a filter of type %s which is already externally provided (such as by a prioritiser) at %s",
               configName,
               type,
               this.getBreadCrumbs()
            )
         );
      }

      if (validationHelper.hasSeenFilter(type)) {
         result = false;
         errors.add(String.format("%s: has defined a filter of type %s more than once at %s", configName, type, this.getBreadCrumbs()));
      }

      return result;
   }
}
