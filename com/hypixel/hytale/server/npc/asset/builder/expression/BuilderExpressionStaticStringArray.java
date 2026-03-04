package com.hypixel.hytale.server.npc.asset.builder.expression;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.StdScope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderExpressionStaticStringArray extends BuilderExpression {
   public static final BuilderExpressionStaticStringArray INSTANCE_EMPTY = new BuilderExpressionStaticStringArray(ArrayUtil.EMPTY_STRING_ARRAY);
   private final String[] stringArray;

   public BuilderExpressionStaticStringArray(String[] array) {
      this.stringArray = array;
   }

   @Nonnull
   @Override
   public ValueType getType() {
      return ValueType.STRING_ARRAY;
   }

   @Override
   public boolean isStatic() {
      return true;
   }

   @Override
   public String[] getStringArray(ExecutionContext executionContext) {
      return this.stringArray;
   }

   @Override
   public void addToScope(String name, @Nonnull StdScope scope) {
      scope.addVar(name, this.stringArray);
   }

   @Override
   public void updateScope(@Nonnull StdScope scope, String name, ExecutionContext executionContext) {
      scope.changeValue(name, this.stringArray);
   }

   @Nullable
   public static BuilderExpressionStaticStringArray fromJSON(@Nonnull JsonArray jsonArray) {
      int size = jsonArray.size();
      String[] array = new String[size];

      for (int i = 0; i < size; i++) {
         JsonElement element = jsonArray.get(i);
         if (!element.isJsonPrimitive()) {
            return null;
         }

         JsonPrimitive primitive = element.getAsJsonPrimitive();
         if (!primitive.isString()) {
            return null;
         }

         array[i] = primitive.getAsString();
      }

      return new BuilderExpressionStaticStringArray(array);
   }
}
