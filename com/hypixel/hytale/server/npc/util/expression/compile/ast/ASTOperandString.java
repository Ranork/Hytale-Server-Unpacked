package com.hypixel.hytale.server.npc.util.expression.compile.ast;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import com.hypixel.hytale.server.npc.util.expression.compile.Token;
import javax.annotation.Nonnull;

public class ASTOperandString extends ASTOperand {
   protected final String constantString;

   public ASTOperandString(@Nonnull Token token, int tokenPosition, String constantString) {
      super(ValueType.STRING, token, tokenPosition);
      this.constantString = constantString;
      this.codeGen = scope -> ExecutionContext.genPUSH(this.constantString);
   }

   public ASTOperandString(@Nonnull Token token, int tokenPosition, @Nonnull Scope scope, String identifier) {
      this(token, tokenPosition, scope.getString(identifier));
      if (!scope.isConstant(identifier)) {
         throw new IllegalArgumentException("Value must be constant: " + identifier);
      }
   }

   @Override
   public String getString() {
      return this.constantString;
   }

   @Override
   public boolean isConstant() {
      return true;
   }

   @Nonnull
   @Override
   public ExecutionContext.Operand asOperand() {
      ExecutionContext.Operand op = new ExecutionContext.Operand();
      op.set(this.constantString);
      return op;
   }
}
