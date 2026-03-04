package com.hypixel.hytale.server.npc.util.expression.compile.ast;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import com.hypixel.hytale.server.npc.util.expression.compile.Token;
import javax.annotation.Nonnull;

public class ASTOperandBoolean extends ASTOperand {
   private final boolean constantBool;

   public ASTOperandBoolean(@Nonnull Token token, int tokenPosition, boolean value) {
      super(ValueType.BOOLEAN, token, tokenPosition);
      this.constantBool = value;
      this.codeGen = scope -> ExecutionContext.genPUSH(this.constantBool);
   }

   public ASTOperandBoolean(@Nonnull Token token, int tokenPosition, @Nonnull Scope scope, String identifier) {
      this(token, tokenPosition, scope.getBoolean(identifier));
      if (!scope.isConstant(identifier)) {
         throw new IllegalArgumentException("Value must be constant: " + identifier);
      }
   }

   @Override
   public boolean getBoolean() {
      return this.constantBool;
   }

   @Override
   public boolean isConstant() {
      return true;
   }

   @Nonnull
   @Override
   public ExecutionContext.Operand asOperand() {
      ExecutionContext.Operand op = new ExecutionContext.Operand();
      op.set(this.constantBool);
      return op;
   }
}
