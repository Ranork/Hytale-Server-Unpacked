package com.hypixel.hytale.server.npc.asset.builder.expression;

import com.hypixel.hytale.common.util.ArrayUtil;
import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.StdScope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import javax.annotation.Nonnull;

public class BuilderExpressionStaticEmptyArray extends BuilderExpression {
   public static final BuilderExpressionStaticEmptyArray INSTANCE = new BuilderExpressionStaticEmptyArray();

   @Nonnull
   @Override
   public ValueType getType() {
      return ValueType.EMPTY_ARRAY;
   }

   @Override
   public boolean isStatic() {
      return true;
   }

   @Override
   public double[] getNumberArray(ExecutionContext executionContext) {
      return ArrayUtil.EMPTY_DOUBLE_ARRAY;
   }

   @Override
   public int[] getIntegerArray(ExecutionContext executionContext) {
      return ArrayUtil.EMPTY_INT_ARRAY;
   }

   @Nonnull
   @Override
   public String[] getStringArray(ExecutionContext executionContext) {
      return ArrayUtil.EMPTY_STRING_ARRAY;
   }

   @Override
   public boolean[] getBooleanArray(ExecutionContext executionContext) {
      return ArrayUtil.EMPTY_BOOLEAN_ARRAY;
   }

   @Override
   public void addToScope(String name, @Nonnull StdScope scope) {
      scope.addConstEmptyArray(name);
   }

   @Override
   public void updateScope(@Nonnull StdScope scope, String name, ExecutionContext executionContext) {
      scope.changeValueToEmptyArray(name);
   }
}
