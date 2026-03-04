package com.hypixel.hytale.server.npc.util.expression.compile.ast;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import com.hypixel.hytale.server.npc.util.expression.compile.Token;
import javax.annotation.Nonnull;

public class ASTOperandIdentifier extends ASTOperand {
   private final String identifier;

   public ASTOperandIdentifier(@Nonnull ValueType returnType, @Nonnull Token token, int tokenPosition, String identifier) {
      super(returnType, token, tokenPosition);
      this.identifier = identifier;
      this.codeGen = scope -> ExecutionContext.genREAD(this.identifier, this.getValueType(), scope);
   }

   public String getIdentifier() {
      return this.identifier;
   }

   @Override
   public boolean isConstant() {
      return false;
   }
}
