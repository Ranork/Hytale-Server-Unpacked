package com.hypixel.hytale.server.npc.asset.builder.holder;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderParameters;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleValidator;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import javax.annotation.Nonnull;

public class FloatHolder extends DoubleHolderBase {
   @Override
   public void validate(ExecutionContext context) {
      this.get(context);
   }

   public void readJSON(
      JsonElement optionalJsonElement, float defaultValue, DoubleValidator validator, String name, @Nonnull BuilderParameters builderParameters
   ) {
      this.readJSON(optionalJsonElement, defaultValue, validator, name, builderParameters);
   }

   public float get(ExecutionContext executionContext) {
      double value = this.rawGet(executionContext);
      this.validateRelations(executionContext, value);
      return (float)value;
   }
}
