package com.hypixel.hytale.server.npc.util.expression.compile;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import java.util.function.Function;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class OperatorUnary {
   private Token token;
   private ValueType argument;
   private ValueType result;
   private Function<Scope, ExecutionContext.Instruction> codeGen;
   @Nonnull
   private static OperatorUnary[] operators = new OperatorUnary[]{
      of(Token.UNARY_PLUS, ValueType.NUMBER, ValueType.NUMBER, null),
      of(Token.UNARY_MINUS, ValueType.NUMBER, ValueType.NUMBER, scope -> ExecutionContext.UNARY_MINUS),
      of(Token.LOGICAL_NOT, ValueType.BOOLEAN, ValueType.BOOLEAN, scope -> ExecutionContext.LOGICAL_NOT),
      of(Token.BITWISE_NOT, ValueType.NUMBER, ValueType.NUMBER, scope -> ExecutionContext.BITWISE_NOT)
   };

   private OperatorUnary(Token token, ValueType argument, ValueType result, Function<Scope, ExecutionContext.Instruction> codeGen) {
      this.token = token;
      this.argument = argument;
      this.result = result;
      this.codeGen = codeGen;
   }

   public boolean hasCodeGen() {
      return this.codeGen != null;
   }

   public ValueType getResultType() {
      return this.result;
   }

   public Function<Scope, ExecutionContext.Instruction> getCodeGen() {
      return this.codeGen;
   }

   @Nonnull
   private static OperatorUnary of(Token token, ValueType argument, ValueType result, Function<Scope, ExecutionContext.Instruction> codeGen) {
      return new OperatorUnary(token, argument, result, codeGen);
   }

   @Nullable
   public static OperatorUnary findOperator(Token token, ValueType type) {
      for (OperatorUnary op : operators) {
         if (op.token == token && op.argument == type) {
            return op;
         }
      }

      return null;
   }
}
