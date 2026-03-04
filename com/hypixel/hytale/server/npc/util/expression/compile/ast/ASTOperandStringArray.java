package com.hypixel.hytale.server.npc.util.expression.compile.ast;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import com.hypixel.hytale.server.npc.util.expression.compile.Token;
import java.util.Stack;
import javax.annotation.Nonnull;

public class ASTOperandStringArray extends ASTOperand {
   private final String[] constantStringArray;

   public ASTOperandStringArray(@Nonnull Token token, int tokenPosition, String[] constantStringArray) {
      super(ValueType.STRING_ARRAY, token, tokenPosition);
      this.constantStringArray = constantStringArray;
      this.codeGen = scope -> ExecutionContext.genPUSH(this.constantStringArray);
   }

   public ASTOperandStringArray(@Nonnull Token token, int tokenPosition, @Nonnull Scope scope, String identifier) {
      this(token, tokenPosition, scope.getStringArray(identifier));
      if (!scope.isConstant(identifier)) {
         throw new IllegalArgumentException("Value must be constant: " + identifier);
      }
   }

   public ASTOperandStringArray(@Nonnull Token token, int tokenPosition, @Nonnull Stack<AST> operandStack, int firstArgument, int argumentCount) {
      this(token, tokenPosition, new String[argumentCount]);

      for (int i = 0; i < argumentCount; i++) {
         this.constantStringArray[i] = operandStack.get(firstArgument + i).getString();
      }
   }

   @Override
   public boolean isConstant() {
      return true;
   }

   @Nonnull
   @Override
   public ExecutionContext.Operand asOperand() {
      ExecutionContext.Operand op = new ExecutionContext.Operand();
      op.set(this.constantStringArray);
      return op;
   }
}
