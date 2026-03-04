package com.hypixel.hytale.server.npc.asset.builder.expression;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.StdScope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class BuilderExpressionDynamicStringArray extends BuilderExpressionDynamic {
   public BuilderExpressionDynamicStringArray(String expression, ExecutionContext.Instruction[] instructionSequence) {
      super(expression, instructionSequence);
   }

   @Nonnull
   @Override
   public ValueType getType() {
      return ValueType.STRING_ARRAY;
   }

   @Nullable
   @Override
   public String[] getStringArray(@Nonnull ExecutionContext executionContext) {
      this.execute(executionContext);
      return executionContext.popStringArray();
   }

   @Override
   public void updateScope(@Nonnull StdScope scope, String name, @Nonnull ExecutionContext executionContext) {
      scope.changeValue(name, this.getStringArray(executionContext));
   }
}
