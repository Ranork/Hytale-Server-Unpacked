package com.hypixel.hytale.server.npc.asset.builder.expression;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.StdScope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import javax.annotation.Nonnull;

public class BuilderExpressionDynamicNumberArray extends BuilderExpressionDynamic {
   public BuilderExpressionDynamicNumberArray(String expression, ExecutionContext.Instruction[] instructionSequence) {
      super(expression, instructionSequence);
   }

   @Nonnull
   @Override
   public ValueType getType() {
      return ValueType.NUMBER_ARRAY;
   }

   @Override
   public double[] getNumberArray(@Nonnull ExecutionContext executionContext) {
      this.execute(executionContext);
      return executionContext.popNumberArray();
   }

   @Override
   public int[] getIntegerArray(@Nonnull ExecutionContext executionContext) {
      return BuilderExpressionStaticNumberArray.convertDoubleToIntArray(this.getNumberArray(executionContext));
   }

   @Override
   public void updateScope(@Nonnull StdScope scope, String name, @Nonnull ExecutionContext executionContext) {
      scope.changeValue(name, this.getNumberArray(executionContext));
   }
}
