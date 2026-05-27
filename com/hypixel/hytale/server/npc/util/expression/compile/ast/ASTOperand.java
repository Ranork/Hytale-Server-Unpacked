package com.hypixel.hytale.server.npc.util.expression.compile.ast;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import com.hypixel.hytale.server.npc.util.expression.compile.CompileContext;
import com.hypixel.hytale.server.npc.util.expression.compile.Parser;
import com.hypixel.hytale.server.npc.util.expression.compile.Token;
import javax.annotation.Nonnull;

public abstract class ASTOperand extends AST {
   public ASTOperand(@Nonnull ValueType valueType, @Nonnull Token token, int tokenPosition) {
      super(valueType, token, tokenPosition);
   }

   @Nonnull
   public static ASTOperand createFromParsedToken(@Nonnull Parser.ParsedToken operand, @Nonnull CompileContext compileContext) {
      Token token = operand.token;
      int tokenPosition = operand.tokenPosition;
      String tokenString = operand.tokenString;
      switch (token) {
         case STRING:
            return new ASTOperandString(token, tokenPosition, tokenString);
         case NUMBER:
            return new ASTOperandNumber(token, tokenPosition, operand.tokenNumber);
         case IDENTIFIER:
            Scope scope = compileContext.getScope();
            if (scope.isConstant(tokenString)) {
               return createFromScopeConstant(token, tokenPosition, scope, tokenString);
            }

            return new ASTOperandIdentifier(scope.getType(tokenString), token, tokenPosition, tokenString);
         default:
            throw new IllegalStateException("Unknown parser operand type in AST" + operand.token);
      }
   }

   @Nonnull
   private static ASTOperand createFromScopeConstant(@Nonnull Token token, int tokenPosition, @Nonnull Scope scope, String identifier) {
      ValueType type = scope.getType(identifier);

      return (ASTOperand)(switch (type) {
         case NUMBER -> new ASTOperandNumber(token, tokenPosition, scope, identifier);
         case STRING -> new ASTOperandString(token, tokenPosition, scope, identifier);
         case BOOLEAN -> new ASTOperandBoolean(token, tokenPosition, scope, identifier);
         case NUMBER_ARRAY -> new ASTOperandNumberArray(token, tokenPosition, scope, identifier);
         case STRING_ARRAY -> new ASTOperandStringArray(token, tokenPosition, scope, identifier);
         case BOOLEAN_ARRAY -> new ASTOperandBooleanArray(token, tokenPosition, scope, identifier);
         case EMPTY_ARRAY -> new ASTOperandEmptyArray(token, tokenPosition);
         case null, default -> throw new IllegalStateException("Illegal constant type encountered" + type);
      });
   }

   @Nonnull
   public static ASTOperand createFromOperand(@Nonnull Token token, int tokenPosition, @Nonnull ExecutionContext.Operand operand) {
      ValueType type = operand.type;

      return (ASTOperand)(switch (type) {
         case NUMBER -> new ASTOperandNumber(token, tokenPosition, operand.number);
         case STRING -> new ASTOperandString(token, tokenPosition, operand.string);
         case BOOLEAN -> new ASTOperandBoolean(token, tokenPosition, operand.bool);
         case NUMBER_ARRAY -> new ASTOperandNumberArray(token, tokenPosition, operand.numberArray);
         case STRING_ARRAY -> new ASTOperandStringArray(token, tokenPosition, operand.stringArray);
         case BOOLEAN_ARRAY -> new ASTOperandBooleanArray(token, tokenPosition, operand.boolArray);
         case EMPTY_ARRAY -> new ASTOperandEmptyArray(token, tokenPosition);
         case null, default -> throw new IllegalStateException("Illegal operand type encountered" + type);
      });
   }
}
