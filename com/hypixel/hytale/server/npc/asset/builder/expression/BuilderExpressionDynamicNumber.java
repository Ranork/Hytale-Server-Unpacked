package com.hypixel.hytale.server.npc.asset.builder.expression;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.StdScope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import javax.annotation.Nonnull;

public class BuilderExpressionDynamicNumber extends BuilderExpressionDynamic {
   public BuilderExpressionDynamicNumber(String expression, ExecutionContext.Instruction[] instructionSequence) {
      super(expression, instructionSequence);
   }

   @Nonnull
   @Override
   public ValueType getType() {
      return ValueType.NUMBER;
   }

   @Override
   public double getNumber(@Nonnull ExecutionContext executionContext) {
      this.execute(executionContext);
      return executionContext.popNumber();
   }

   @Override
   public void updateScope(@Nonnull StdScope scope, String name, @Nonnull ExecutionContext executionContext) {
      scope.changeValue(name, this.getNumber(executionContext));
   }
}
