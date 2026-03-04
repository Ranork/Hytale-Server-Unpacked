package com.hypixel.hytale.server.npc.util.expression.compile.ast;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import com.hypixel.hytale.server.npc.util.expression.compile.Token;
import javax.annotation.Nonnull;

public class ASTOperandNumber extends ASTOperand {
   private final double constantNumber;

   public ASTOperandNumber(@Nonnull Token token, int tokenPosition, double constantNumber) {
      super(ValueType.NUMBER, token, tokenPosition);
      this.constantNumber = constantNumber;
      this.codeGen = scope -> ExecutionContext.genPUSH(this.constantNumber);
   }

   public ASTOperandNumber(@Nonnull Token token, int tokenPosition, @Nonnull Scope scope, String identifier) {
      this(token, tokenPosition, scope.getNumber(identifier));
      if (!scope.isConstant(identifier)) {
         throw new IllegalArgumentException("Value must be constant: " + identifier);
      }
   }

   @Override
   public double getNumber() {
      return this.constantNumber;
   }

   @Override
   public boolean isConstant() {
      return true;
   }

   @Nonnull
   @Override
   public ExecutionContext.Operand asOperand() {
      ExecutionContext.Operand op = new ExecutionContext.Operand();
      op.set(this.constantNumber);
      return op;
   }
}
