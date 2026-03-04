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

public class BuilderExpressionStaticNumberArray extends BuilderExpression {
   public static final BuilderExpressionStaticNumberArray INSTANCE_EMPTY = new BuilderExpressionStaticNumberArray(ArrayUtil.EMPTY_DOUBLE_ARRAY);
   private final double[] numberArray;
   @Nullable
   private int[] cachedIntArray;

   public BuilderExpressionStaticNumberArray(double[] array) {
      this.numberArray = array;
      this.cachedIntArray = null;
   }

   @Nonnull
   @Override
   public ValueType getType() {
      return ValueType.NUMBER_ARRAY;
   }

   @Override
   public boolean isStatic() {
      return true;
   }

   @Override
   public double[] getNumberArray(ExecutionContext executionContext) {
      return this.numberArray;
   }

   @Override
   public int[] getIntegerArray(ExecutionContext executionContext) {
      this.createCacheIfAbsent();
      return this.cachedIntArray;
   }

   @Override
   public void addToScope(String name, @Nonnull StdScope scope) {
      scope.addVar(name, this.numberArray);
   }

   @Override
   public void updateScope(@Nonnull StdScope scope, String name, ExecutionContext executionContext) {
      scope.changeValue(name, this.numberArray);
   }

   private void createCacheIfAbsent() {
      if (this.cachedIntArray == null) {
         this.cachedIntArray = convertDoubleToIntArray(this.numberArray);
      }
   }

   @Nullable
   public static BuilderExpressionStaticNumberArray fromJSON(@Nonnull JsonArray jsonArray) {
      int size = jsonArray.size();
      double[] array = new double[size];

      for (int i = 0; i < size; i++) {
         JsonElement element = jsonArray.get(i);
         if (!element.isJsonPrimitive()) {
            return null;
         }

         JsonPrimitive primitive = element.getAsJsonPrimitive();
         if (!primitive.isNumber()) {
            return null;
         }

         array[i] = primitive.getAsDouble();
      }

      return new BuilderExpressionStaticNumberArray(array);
   }

   public static int[] convertDoubleToIntArray(@Nullable double[] source) {
      if (source == null) {
         return null;
      } else {
         int length = source.length;
         int[] result = new int[length];

         for (int i = 0; i < length; i++) {
            result[i] = (int)Math.round(source[i]);
         }

         return result;
      }
   }

   public static double[] convertIntToDoubleArray(@Nullable int[] source) {
      if (source == null) {
         return null;
      } else {
         int length = source.length;
         double[] result = new double[length];

         for (int i = 0; i < length; i++) {
            result[i] = source[i];
         }

         return result;
      }
   }
}
