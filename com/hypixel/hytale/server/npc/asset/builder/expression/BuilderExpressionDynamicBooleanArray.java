package com.hypixel.hytale.server.npc.asset.builder.expression;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.StdScope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import javax.annotation.Nonnull;

public class BuilderExpressionDynamicBooleanArray extends BuilderExpressionDynamic {
   public BuilderExpressionDynamicBooleanArray(String expression, ExecutionContext.Instruction[] instructionSequence) {
      super(expression, instructionSequence);
   }

   @Nonnull
   @Override
   public ValueType getType() {
      return ValueType.BOOLEAN_ARRAY;
   }

   @Override
   public boolean[] getBooleanArray(@Nonnull ExecutionContext executionContext) {
      this.execute(executionContext);
      return executionContext.popBooleanArray();
   }

   @Override
   public void updateScope(@Nonnull StdScope scope, String name, @Nonnull ExecutionContext executionContext) {
      scope.changeValue(name, this.getBooleanArray(executionContext));
   }
}
