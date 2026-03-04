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

public class BuilderExpressionStaticBooleanArray extends BuilderExpression {
   public static final BuilderExpressionStaticBooleanArray INSTANCE_EMPTY = new BuilderExpressionStaticBooleanArray(ArrayUtil.EMPTY_BOOLEAN_ARRAY);
   private final boolean[] booleanArray;

   public BuilderExpressionStaticBooleanArray(boolean[] array) {
      this.booleanArray = array;
   }

   @Nonnull
   @Override
   public ValueType getType() {
      return ValueType.BOOLEAN_ARRAY;
   }

   @Override
   public boolean isStatic() {
      return true;
   }

   @Override
   public boolean[] getBooleanArray(ExecutionContext executionContext) {
      return this.booleanArray;
   }

   @Override
   public void addToScope(String name, @Nonnull StdScope scope) {
      scope.addVar(name, this.booleanArray);
   }

   @Override
   public void updateScope(@Nonnull StdScope scope, String name, ExecutionContext executionContext) {
      scope.changeValue(name, this.booleanArray);
   }

   @Nullable
   public static BuilderExpressionStaticBooleanArray fromJSON(@Nonnull JsonArray jsonArray) {
      int size = jsonArray.size();
      boolean[] array = new boolean[size];

      for (int i = 0; i < size; i++) {
         JsonElement element = jsonArray.get(i);
         if (!element.isJsonPrimitive()) {
            return null;
         }

         JsonPrimitive primitive = element.getAsJsonPrimitive();
         if (!primitive.isBoolean()) {
            return null;
         }

         array[i] = primitive.getAsBoolean();
      }

      return new BuilderExpressionStaticBooleanArray(array);
   }
}
