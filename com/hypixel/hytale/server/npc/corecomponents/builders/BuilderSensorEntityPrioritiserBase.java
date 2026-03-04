package com.hypixel.hytale.server.npc.corecomponents.builders;

import com.hypixel.hytale.server.npc.asset.builder.BuilderBase;
import com.hypixel.hytale.server.npc.corecomponents.ISensorEntityPrioritiser;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.validators.NPCLoadTimeValidationHelper;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

public abstract class BuilderSensorEntityPrioritiserBase extends BuilderBase<ISensorEntityPrioritiser> {
   private final Set<String> providedFilterTypes;

   protected BuilderSensorEntityPrioritiserBase(Set<String> providedFilterTypes) {
      this.providedFilterTypes = providedFilterTypes;
   }

   @Nonnull
   @Override
   public Class<ISensorEntityPrioritiser> category() {
      return ISensorEntityPrioritiser.class;
   }

   @Override
   public boolean isEnabled(ExecutionContext context) {
      return true;
   }

   @Override
   public boolean validate(
      String configName, @Nonnull NPCLoadTimeValidationHelper validationHelper, ExecutionContext context, Scope globalScope, @Nonnull List<String> errors
   ) {
      validationHelper.setPrioritiserProvidedFilterTypes(this.providedFilterTypes);
      return super.validate(configName, validationHelper, context, globalScope, errors);
   }

   protected Set<String> getProvidedFilterTypes() {
      return this.providedFilterTypes;
   }
}
