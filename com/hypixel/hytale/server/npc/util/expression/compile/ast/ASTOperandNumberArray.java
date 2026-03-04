package com.hypixel.hytale.server.npc.util.expression.compile.ast;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import com.hypixel.hytale.server.npc.util.expression.compile.Token;
import java.util.Stack;
import javax.annotation.Nonnull;

public class ASTOperandNumberArray extends ASTOperand {
   private final double[] constantNumberArray;

   public ASTOperandNumberArray(@Nonnull Token token, int tokenPosition, double[] constantNumberArray) {
      super(ValueType.NUMBER_ARRAY, token, tokenPosition);
      this.constantNumberArray = constantNumberArray;
      this.codeGen = scope -> ExecutionContext.genPUSH(this.constantNumberArray);
   }

   public ASTOperandNumberArray(@Nonnull Token token, int tokenPosition, @Nonnull Scope scope, String identifier) {
      this(token, tokenPosition, scope.getNumberArray(identifier));
      if (!scope.isConstant(identifier)) {
         throw new IllegalArgumentException("Value must be constant: " + identifier);
      }
   }

   public ASTOperandNumberArray(@Nonnull Token token, int tokenPosition, @Nonnull Stack<AST> operandStack, int firstArgument, int argumentCount) {
      this(token, tokenPosition, new double[argumentCount]);

      for (int i = 0; i < argumentCount; i++) {
         this.constantNumberArray[i] = operandStack.get(firstArgument + i).getNumber();
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
      op.set(this.constantNumberArray);
      return op;
   }
}
