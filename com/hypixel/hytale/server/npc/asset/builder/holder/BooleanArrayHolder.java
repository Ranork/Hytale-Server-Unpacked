package com.hypixel.hytale.server.npc.asset.builder.holder;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderParameters;
import com.hypixel.hytale.server.npc.asset.builder.validators.BooleanArrayValidator;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BooleanArrayHolder extends ArrayHolder {
   protected BooleanArrayValidator booleanArrayValidator;

   public BooleanArrayHolder() {
      super(ValueType.BOOLEAN_ARRAY);
   }

   @Override
   public void validate(ExecutionContext context) {
      this.get(context);
   }

   public void readJSON(
      @Nonnull JsonElement requiredJsonElement,
      int minLength,
      int maxLength,
      BooleanArrayValidator validator,
      String name,
      @Nonnull BuilderParameters builderParameters
   ) {
      this.readJSON(requiredJsonElement, minLength, maxLength, name, builderParameters);
      this.booleanArrayValidator = validator;
      if (this.isStatic()) {
         this.validate(this.expression.getBooleanArray(null));
      }
   }

   public void readJSON(
      JsonElement optionalJsonElement,
      int minLength,
      int maxLength,
      boolean[] defaultValue,
      BooleanArrayValidator validator,
      String name,
      @Nonnull BuilderParameters builderParameters
   ) {
      this.readJSON(optionalJsonElement, minLength, maxLength, defaultValue, name, builderParameters);
      this.booleanArrayValidator = validator;
      if (this.isStatic()) {
         this.validate(this.expression.getBooleanArray(null));
      }
   }

   public boolean[] get(ExecutionContext executionContext) {
      return this.rawGet(executionContext);
   }

   public boolean[] rawGet(ExecutionContext executionContext) {
      boolean[] value = this.expression.getBooleanArray(executionContext);
      if (!this.isStatic()) {
         this.validate(value);
      }

      return value;
   }

   public void validate(@Nullable boolean[] value) {
      if (value != null) {
         this.validateLength(value.length);
      }

      if (this.booleanArrayValidator != null && !this.booleanArrayValidator.test(value)) {
         throw new IllegalStateException(this.booleanArrayValidator.errorMessage(this.name, value));
      }
   }
}
