package com.hypixel.hytale.server.npc.asset.builder.holder;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;

public class DoubleHolder extends DoubleHolderBase {
   @Override
   public void validate(ExecutionContext context) {
      this.get(context);
   }

   public double get(ExecutionContext executionContext) {
      double value = this.rawGet(executionContext);
      this.validateRelations(executionContext, value);
      return value;
   }
}
