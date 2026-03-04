package com.hypixel.hytale.server.npc.asset.builder.holder;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderBase;
import com.hypixel.hytale.server.npc.asset.builder.BuilderParameters;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import java.util.EnumSet;
import javax.annotation.Nonnull;

public class EnumSetHolder<E extends Enum<E>> extends ArrayHolder {
   private Class<E> clazz;
   private E[] enumConstants;
   private EnumSet<E> value;

   public EnumSetHolder() {
      super(ValueType.STRING_ARRAY);
   }

   @Override
   public void validate(ExecutionContext context) {
      this.get(context);
   }

   public void readJSON(@Nonnull JsonElement requiredJsonElement, Class<E> clazz, String name, @Nonnull BuilderParameters builderParameters) {
      this.clazz = clazz;
      this.enumConstants = clazz.getEnumConstants();
      this.readJSON(requiredJsonElement, 0, Integer.MAX_VALUE, name, builderParameters);
      if (this.isStatic()) {
         this.value = BuilderBase.stringsToEnumSet(this.expression.getStringArray(null), clazz, this.enumConstants, this.getName());
      }
   }

   public void readJSON(
      JsonElement optionalJsonElement, @Nonnull EnumSet<E> defaultValue, Class<E> clazz, String name, @Nonnull BuilderParameters builderParameters
   ) {
      this.clazz = clazz;
      this.enumConstants = clazz.getEnumConstants();
      this.readJSON(optionalJsonElement, 0, Integer.MAX_VALUE, BuilderBase.enumSetToStrings(defaultValue), name, builderParameters);
      if (this.isStatic()) {
         this.value = BuilderBase.stringsToEnumSet(this.expression.getStringArray(null), clazz, this.enumConstants, this.getName());
      }
   }

   public EnumSet<E> get(ExecutionContext executionContext) {
      return this.rawGet(executionContext);
   }

   public EnumSet<E> rawGet(ExecutionContext executionContext) {
      if (!this.isStatic()) {
         this.value = BuilderBase.stringsToEnumSet(this.expression.getStringArray(executionContext), this.clazz, this.enumConstants, this.getName());
      }

      return this.value;
   }
}
