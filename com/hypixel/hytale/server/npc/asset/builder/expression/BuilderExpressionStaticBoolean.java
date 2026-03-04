package com.hypixel.hytale.server.npc.asset.builder.expression;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.StdScope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import javax.annotation.Nonnull;

public class BuilderExpressionStaticBoolean extends BuilderExpression {
   private final boolean bool;

   public BuilderExpressionStaticBoolean(boolean bool) {
      this.bool = bool;
   }

   @Nonnull
   @Override
   public ValueType getType() {
      return ValueType.BOOLEAN;
   }

   @Override
   public boolean isStatic() {
      return true;
   }

   @Override
   public boolean getBoolean(ExecutionContext executionContext) {
      return this.bool;
   }

   @Override
   public void addToScope(String name, @Nonnull StdScope scope) {
      scope.addVar(name, this.bool);
   }

   @Override
   public void updateScope(@Nonnull StdScope scope, String name, ExecutionContext executionContext) {
      scope.changeValue(name, this.bool);
   }
}
