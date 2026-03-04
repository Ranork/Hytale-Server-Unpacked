package com.hypixel.hytale.server.npc.util.expression.compile.ast;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import com.hypixel.hytale.server.npc.util.expression.compile.CompileContext;
import com.hypixel.hytale.server.npc.util.expression.compile.Parser;
import com.hypixel.hytale.server.npc.util.expression.compile.Token;
import java.util.Stack;
import javax.annotation.Nonnull;

public class ASTOperatorTuple extends ASTOperator {
   public ASTOperatorTuple(@Nonnull ValueType arrayType, @Nonnull Token token, int tokenPosition) {
      super(arrayType, token, tokenPosition);
      this.codeGen = scope -> ExecutionContext.genPACK(this.getValueType(), this.getArguments().size());
   }

   @Override
   public boolean isConstant() {
      return false;
   }

   public static void fromParsedTuple(@Nonnull Parser.ParsedToken openingToken, int argumentCount, @Nonnull CompileContext compileContext) {
      Token token = openingToken.token;
      if (token != Token.OPEN_SQUARE_BRACKET) {
         throw new IllegalStateException("Bad opening bracket for tuple: " + token.get());
      } else {
         int tokenPosition = openingToken.tokenPosition;
         Stack<AST> operandStack = compileContext.getOperandStack();
         if (argumentCount == 0) {
            operandStack.push(new ASTOperandEmptyArray(token, tokenPosition));
         } else {
            int len = operandStack.size();
            int firstArgument = len - argumentCount;
            ValueType argumentType = operandStack.get(firstArgument).getValueType();

            ValueType arrayType = switch (argumentType) {
               case NUMBER -> ValueType.NUMBER_ARRAY;
               case STRING -> ValueType.STRING_ARRAY;
               case BOOLEAN -> ValueType.BOOLEAN_ARRAY;
               default -> throw new IllegalStateException("Invalid type in array: " + argumentType);
            };
            boolean isConstant = true;

            for (int i = firstArgument; i < len; i++) {
               AST ast = operandStack.get(i);
               isConstant &= ast.isConstant();
               if (ast.getValueType() != argumentType) {
                  throw new IllegalStateException("Mismatching types in array. Expected " + argumentType + ", found " + ast.getValueType());
               }
            }

            if (isConstant) {
               ASTOperand item = (ASTOperand)(switch (arrayType) {
                  case NUMBER_ARRAY -> new ASTOperandNumberArray(token, tokenPosition, operandStack, firstArgument, argumentCount);
                  case STRING_ARRAY -> new ASTOperandStringArray(token, tokenPosition, operandStack, firstArgument, argumentCount);
                  case BOOLEAN_ARRAY -> new ASTOperandBooleanArray(token, tokenPosition, operandStack, firstArgument, argumentCount);
                  default -> throw new IllegalStateException("Unexpected array type when creating constant array: " + arrayType);
               });
               operandStack.setSize(firstArgument);
               operandStack.push(item);
            } else {
               ASTOperatorTuple ast = new ASTOperatorTuple(arrayType, token, tokenPosition);

               for (int ix = firstArgument; ix < len; ix++) {
                  ast.addArgument(operandStack.get(ix));
               }

               operandStack.setSize(firstArgument);
               operandStack.push(ast);
            }
         }
      }
   }
}
