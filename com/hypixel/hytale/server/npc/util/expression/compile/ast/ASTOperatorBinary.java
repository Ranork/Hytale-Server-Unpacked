package com.hypixel.hytale.server.npc.util.expression.compile.ast;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import com.hypixel.hytale.server.npc.util.expression.compile.CompileContext;
import com.hypixel.hytale.server.npc.util.expression.compile.OperatorBinary;
import com.hypixel.hytale.server.npc.util.expression.compile.Parser;
import com.hypixel.hytale.server.npc.util.expression.compile.Token;
import java.text.ParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;
import javax.annotation.Nonnull;

public class ASTOperatorBinary extends ASTOperator {
   public ASTOperatorBinary(@Nonnull OperatorBinary operatorBinary, @Nonnull Token token, int tokenPosition, @Nonnull AST lhs, @Nonnull AST rhs) {
      super(operatorBinary.getResultType(), token, tokenPosition);
      this.addArgument(lhs);
      this.addArgument(rhs);
      this.codeGen = operatorBinary.getCodeGen();
   }

   @Override
   public boolean isConstant() {
      return false;
   }

   public static void fromBinaryOperator(@Nonnull Parser.ParsedToken operator, @Nonnull CompileContext compileContext) throws ParseException {
      Stack<AST> operandStack = compileContext.getOperandStack();

      try {
         AST rhs = operandStack.pop();
         AST lhs = operandStack.pop();
         OperatorBinary operatorBinary = OperatorBinary.findOperator(operator.token, lhs.returnType(), rhs.returnType());
         if (operatorBinary == null) {
            throw new ParseException("Type mismatch for operator " + operator.token, operator.tokenPosition);
         } else {
            if (lhs.isConstant() && rhs.isConstant()) {
               ExecutionContext executionContext = compileContext.getExecutionContext();
               List<ExecutionContext.Instruction> instructionList = compileContext.getInstructions();
               instructionList.clear();
               lhs.genCode(instructionList, null);
               rhs.genCode(instructionList, null);
               instructionList.add(operatorBinary.getCodeGen().apply(null));
               ValueType ret = executionContext.execute(instructionList);
               if (ret == ValueType.VOID) {
                  throw new IllegalStateException("Failed to evaluate constant binary AST");
               }

               operandStack.push(ASTOperand.createFromOperand(operator.token, operator.tokenPosition, executionContext.top()));
            } else {
               operandStack.push(new ASTOperatorBinary(operatorBinary, operator.token, operator.tokenPosition, lhs, rhs));
            }
         }
      } catch (NoSuchElementException var9) {
         throw new ParseException("Not enough operands for operator '" + operator.tokenString, operator.tokenPosition);
      }
   }
}
