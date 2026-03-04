package com.hypixel.hytale.server.npc.asset.builder.holder;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderParameters;
import com.hypixel.hytale.server.npc.asset.builder.validators.StringArrayValidator;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class StringArrayHolder extends ArrayHolder {
   protected StringArrayValidator stringArrayValidator;
   protected List<BiConsumer<ExecutionContext, String[]>> relationValidators;

   public StringArrayHolder() {
      super(ValueType.STRING_ARRAY);
   }

   @Override
   public void validate(ExecutionContext context) {
      this.get(context);
   }

   public void readJSON(
      @Nonnull JsonElement requiredJsonElement,
      int minLength,
      int maxLength,
      StringArrayValidator validator,
      String name,
      @Nonnull BuilderParameters builderParameters
   ) {
      this.readJSON(requiredJsonElement, minLength, maxLength, name, builderParameters);
      this.stringArrayValidator = validator;
      if (this.isStatic()) {
         this.validate(this.expression.getStringArray(null));
      }
   }

   public void readJSON(
      JsonElement optionalJsonElement,
      int minLength,
      int maxLength,
      String[] defaultValue,
      StringArrayValidator validator,
      String name,
      @Nonnull BuilderParameters builderParameters
   ) {
      this.readJSON(optionalJsonElement, minLength, maxLength, defaultValue, name, builderParameters);
      this.stringArrayValidator = validator;
      if (this.isStatic()) {
         this.validate(this.expression.getStringArray(null));
      }
   }

   @Nullable
   public String[] get(ExecutionContext executionContext) {
      String[] value = this.rawGet(executionContext);
      this.validateRelations(executionContext, value);
      return value;
   }

   @Nullable
   public String[] rawGet(ExecutionContext executionContext) {
      String[] value = this.expression.getStringArray(executionContext);
      if (!this.isStatic()) {
         this.validate(value);
      }

      return value;
   }

   public void validate(@Nullable String[] value) {
      if (value != null) {
         this.validateLength(value.length);
      }

      if (this.stringArrayValidator != null && !this.stringArrayValidator.test(value)) {
         throw new IllegalStateException(this.stringArrayValidator.errorMessage(this.name, value));
      }
   }

   public void addRelationValidator(BiConsumer<ExecutionContext, String[]> validator) {
      if (this.relationValidators == null) {
         this.relationValidators = new ObjectArrayList();
      }

      this.relationValidators.add(validator);
   }

   protected void validateRelations(ExecutionContext executionContext, String[] value) {
      if (this.relationValidators != null) {
         for (BiConsumer<ExecutionContext, String[]> executionContextConsumer : this.relationValidators) {
            executionContextConsumer.accept(executionContext, value);
         }
      }
   }
}
