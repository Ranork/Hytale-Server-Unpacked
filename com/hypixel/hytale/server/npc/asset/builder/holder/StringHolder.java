package com.hypixel.hytale.server.npc.asset.builder.holder;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderParameters;
import com.hypixel.hytale.server.npc.asset.builder.expression.BuilderExpressionStaticString;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringValidator;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import javax.annotation.Nonnull;

public class StringHolder extends StringHolderBase {
   protected StringValidator stringValidator;

   @Override
   public void validate(ExecutionContext context) {
      this.get(context);
   }

   public void readJSON(@Nonnull JsonElement requiredJsonElement, StringValidator validator, String name, @Nonnull BuilderParameters builderParameters) {
      this.readJSON(requiredJsonElement, name, builderParameters);
      this.stringValidator = validator;
      if (this.isStatic()) {
         this.validate(this.expression.getString(null));
      }
   }

   public void readJSON(
      JsonElement optionalJsonElement, String defaultValue, StringValidator validator, String name, @Nonnull BuilderParameters builderParameters
   ) {
      this.readJSON(optionalJsonElement, () -> new BuilderExpressionStaticString(defaultValue), name, builderParameters);
      this.stringValidator = validator;
      if (this.isStatic()) {
         this.validate(this.expression.getString(null));
      }
   }

   public String get(ExecutionContext executionContext) {
      String value = this.rawGet(executionContext);
      this.validateRelations(executionContext, value);
      return value;
   }

   public String rawGet(ExecutionContext executionContext) {
      String value = this.expression.getString(executionContext);
      if (!this.isStatic()) {
         this.validate(value);
      }

      return value;
   }

   public void validate(String value) {
      if (this.stringValidator != null && !this.stringValidator.test(value)) {
         throw new IllegalStateException(this.stringValidator.errorMessage(value, this.name));
      }
   }
}
