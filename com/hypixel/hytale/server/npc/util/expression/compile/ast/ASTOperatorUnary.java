package com.hypixel.hytale.server.npc.util.expression.compile.ast;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import com.hypixel.hytale.server.npc.util.expression.compile.CompileContext;
import com.hypixel.hytale.server.npc.util.expression.compile.OperatorUnary;
import com.hypixel.hytale.server.npc.util.expression.compile.Parser;
import com.hypixel.hytale.server.npc.util.expression.compile.Token;
import java.text.ParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;
import javax.annotation.Nonnull;

public class ASTOperatorUnary extends ASTOperator {
   public ASTOperatorUnary(@Nonnull OperatorUnary operatorUnary, @Nonnull Token token, int tokenPosition, @Nonnull AST argument) {
      super(operatorUnary.getResultType(), token, tokenPosition);
      this.addArgument(argument);
      this.codeGen = operatorUnary.getCodeGen();
   }

   @Override
   public boolean isConstant() {
      return false;
   }

   public static void fromUnaryOperator(@Nonnull Parser.ParsedToken operand, @Nonnull CompileContext compileContext) throws ParseException {
      int tokenPosition = operand.tokenPosition;
      Token token = operand.token;
      Stack<AST> operandStack = compileContext.getOperandStack();

      try {
         AST node = operandStack.pop();
         OperatorUnary operatorUnary = OperatorUnary.findOperator(token, node.returnType());
         if (operatorUnary == null) {
            throw new ParseException("Type mismatch for operator " + token, tokenPosition);
         } else {
            if (node.isConstant() && operatorUnary.hasCodeGen()) {
               ExecutionContext executionContext = compileContext.getExecutionContext();
               List<ExecutionContext.Instruction> instructionList = compileContext.getInstructions();
               instructionList.clear();
               node.genCode(instructionList, null);
               instructionList.add(operatorUnary.getCodeGen().apply(null));
               ValueType ret = executionContext.execute(instructionList);
               if (ret == ValueType.VOID) {
                  throw new IllegalStateException("Failed to evaluate constant unary AST");
               }

               operandStack.push(ASTOperand.createFromOperand(token, tokenPosition, executionContext.top()));
            } else if (operatorUnary.hasCodeGen()) {
               operandStack.push(new ASTOperatorUnary(operatorUnary, token, tokenPosition, node));
            } else {
               operandStack.push(node);
            }
         }
      } catch (NoSuchElementException var10) {
         throw new ParseException("Not enough operands for operator '" + operand.tokenString, tokenPosition);
      }
   }
}
