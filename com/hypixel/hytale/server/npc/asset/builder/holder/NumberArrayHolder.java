package com.hypixel.hytale.server.npc.asset.builder.holder;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderParameters;
import com.hypixel.hytale.server.npc.asset.builder.expression.BuilderExpressionStaticNumberArray;
import com.hypixel.hytale.server.npc.asset.builder.validators.DoubleArrayValidator;
import com.hypixel.hytale.server.npc.asset.builder.validators.IntArrayValidator;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class NumberArrayHolder extends ArrayHolder {
   protected IntArrayValidator intArrayValidator;
   protected DoubleArrayValidator doubleArrayValidator;

   public NumberArrayHolder() {
      super(ValueType.NUMBER_ARRAY);
   }

   @Override
   public void validate(ExecutionContext context) {
      this.get(context);
   }

   public void readJSON(
      @Nonnull JsonElement requiredJsonElement,
      int minLength,
      int maxLength,
      IntArrayValidator validator,
      String name,
      @Nonnull BuilderParameters builderParameters
   ) {
      this.readJSON(requiredJsonElement, minLength, maxLength, name, builderParameters);
      this.intArrayValidator = validator;
      if (this.isStatic()) {
         this.validate(this.expression.getIntegerArray(null));
      }
   }

   public void readJSON(
      @Nonnull JsonElement requiredJsonElement,
      int minLength,
      int maxLength,
      DoubleArrayValidator validator,
      String name,
      @Nonnull BuilderParameters builderParameters
   ) {
      this.readJSON(requiredJsonElement, minLength, maxLength, name, builderParameters);
      this.doubleArrayValidator = validator;
      if (this.isStatic()) {
         this.validate(this.expression.getNumberArray(null));
      }
   }

   public void readJSON(
      JsonElement optionalJsonElement,
      int minLength,
      int maxLength,
      int[] defaultValue,
      IntArrayValidator validator,
      String name,
      @Nonnull BuilderParameters builderParameters
   ) {
      this.readJSON(
         optionalJsonElement, minLength, maxLength, BuilderExpressionStaticNumberArray.convertIntToDoubleArray(defaultValue), name, builderParameters
      );
      this.intArrayValidator = validator;
      if (this.isStatic()) {
         this.validate(this.expression.getIntegerArray(null));
      }
   }

   public void readJSON(
      JsonElement optionalJsonElement,
      int minLength,
      int maxLength,
      double[] defaultValue,
      DoubleArrayValidator validator,
      String name,
      @Nonnull BuilderParameters builderParameters
   ) {
      this.readJSON(optionalJsonElement, minLength, maxLength, defaultValue, name, builderParameters);
      this.doubleArrayValidator = validator;
      if (this.isStatic()) {
         this.validate(this.expression.getNumberArray(null));
      }
   }

   public double[] get(ExecutionContext executionContext) {
      return this.rawGet(executionContext);
   }

   public double[] rawGet(ExecutionContext executionContext) {
      double[] value = this.expression.getNumberArray(executionContext);
      if (!this.isStatic()) {
         this.validate(value);
      }

      return value;
   }

   public int[] getIntArray(ExecutionContext executionContext) {
      return this.rawGetIntArray(executionContext);
   }

   public int[] rawGetIntArray(ExecutionContext executionContext) {
      int[] value = this.expression.getIntegerArray(executionContext);
      if (!this.isStatic()) {
         this.validate(value);
      }

      return value;
   }

   public void validate(@Nullable int[] value) {
      if (value != null) {
         this.validateLength(value.length);
      }

      if (this.intArrayValidator != null && !this.intArrayValidator.test(value)) {
         throw new IllegalStateException(this.intArrayValidator.errorMessage(value, this.name));
      }
   }

   public void validate(@Nullable double[] value) {
      if (value != null) {
         this.validateLength(value.length);
      }

      if (this.doubleArrayValidator != null && !this.doubleArrayValidator.test(value)) {
         throw new IllegalStateException(this.doubleArrayValidator.errorMessage(value, this.name));
      }
   }
}
