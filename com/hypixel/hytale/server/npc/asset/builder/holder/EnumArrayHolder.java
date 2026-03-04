package com.hypixel.hytale.server.npc.asset.builder.holder;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderBase;
import com.hypixel.hytale.server.npc.asset.builder.BuilderParameters;
import com.hypixel.hytale.server.npc.asset.builder.validators.EnumArrayValidator;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import javax.annotation.Nonnull;

public class EnumArrayHolder<E extends Enum<E>> extends ArrayHolder {
   private Class<E> clazz;
   private E[] enumConstants;
   private EnumArrayValidator validator;
   private E[] value;

   public EnumArrayHolder() {
      super(ValueType.STRING_ARRAY);
   }

   @Override
   public void validate(ExecutionContext context) {
      this.get(context);
   }

   public void readJSON(
      @Nonnull JsonElement requiredJsonElement, Class<E> clazz, EnumArrayValidator validator, String name, @Nonnull BuilderParameters builderParameters
   ) {
      this.clazz = clazz;
      this.enumConstants = clazz.getEnumConstants();
      this.validator = validator;
      this.readJSON(requiredJsonElement, 0, Integer.MAX_VALUE, name, builderParameters);
      if (this.isStatic()) {
         this.resolve(this.expression.getStringArray(null));
      }
   }

   public E[] get(ExecutionContext executionContext) {
      return this.rawGet(executionContext);
   }

   public E[] rawGet(ExecutionContext executionContext) {
      if (!this.isStatic()) {
         this.resolve(this.expression.getStringArray(executionContext));
      }

      return this.value;
   }

   public void resolve(String[] value) {
      this.value = BuilderBase.stringsToEnumArray(value, this.clazz, this.enumConstants, this.getName());
      if (this.validator != null && !this.validator.test(this.value, this.clazz)) {
         throw new IllegalStateException(this.validator.errorMessage(this.name, this.value));
      }
   }
}
