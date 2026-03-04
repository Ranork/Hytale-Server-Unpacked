package com.hypixel.hytale.server.npc.util.expression.compile.ast;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import com.hypixel.hytale.server.npc.util.expression.compile.Token;
import java.util.Stack;
import javax.annotation.Nonnull;

public class ASTOperandBooleanArray extends ASTOperand {
   private final boolean[] constantBoolArray;

   public ASTOperandBooleanArray(@Nonnull Token token, int tokenPosition, boolean[] constantBoolArray) {
      super(ValueType.BOOLEAN_ARRAY, token, tokenPosition);
      this.constantBoolArray = constantBoolArray;
      this.codeGen = scope -> ExecutionContext.genPUSH(this.constantBoolArray);
   }

   public ASTOperandBooleanArray(@Nonnull Token token, int tokenPosition, @Nonnull Scope scope, String identifier) {
      this(token, tokenPosition, scope.getBooleanArray(identifier));
      if (!scope.isConstant(identifier)) {
         throw new IllegalArgumentException("Value must be constant: " + identifier);
      }
   }

   public ASTOperandBooleanArray(@Nonnull Token token, int tokenPosition, @Nonnull Stack<AST> operandStack, int firstArgument, int argumentCount) {
      this(token, tokenPosition, new boolean[argumentCount]);

      for (int i = 0; i < argumentCount; i++) {
         this.constantBoolArray[i] = operandStack.get(firstArgument + i).getBoolean();
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
      op.set(this.constantBoolArray);
      return op;
   }
}
