package com.hypixel.hytale.server.npc.util.expression.compile.ast;

import com.hypixel.hytale.server.npc.util.expression.ExecutionContext;
import com.hypixel.hytale.server.npc.util.expression.Scope;
import com.hypixel.hytale.server.npc.util.expression.ValueType;
import com.hypixel.hytale.server.npc.util.expression.compile.CompileContext;
import com.hypixel.hytale.server.npc.util.expression.compile.Token;
import java.text.ParseException;
import java.util.List;
import java.util.Stack;
import javax.annotation.Nonnull;

public class ASTOperatorFunctionCall extends ASTOperator {
   private final String functionName;

   public ASTOperatorFunctionCall(@Nonnull ValueType returnType, String functionName, int tokenPosition) {
      super(returnType, Token.FUNCTION_CALL, tokenPosition);
      this.functionName = functionName;
      this.codeGen = scope -> ExecutionContext.genCALL(this.functionName, this.getArguments().size(), scope);
   }

   @Override
   public boolean isConstant() {
      return false;
   }

   public static void fromParsedFunction(int argumentCount, @Nonnull CompileContext compileContext) throws ParseException {
      Stack<AST> operandStack = compileContext.getOperandStack();
      int len = operandStack.size();
      AST functionNameAST = operandStack.get(len - argumentCount - 1);
      if (!(functionNameAST instanceof ASTOperandIdentifier identifier)) {
         throw new ParseException("Expected identifier for function name but found type " + functionNameAST.getValueType(), functionNameAST.getTokenPosition());
      } else {
         StringBuilder name = new StringBuilder(identifier.getIdentifier()).append('@');
         boolean isConstant = true;
         int firstArgument = len - argumentCount;

         for (int functionName = firstArgument; functionName < len; functionName++) {
            AST ast = operandStack.get(functionName);
            name.append(Scope.encodeType(ast.getValueType()));
            isConstant &= ast.isConstant();
         }

         String functionName = name.toString();
         Scope scope = compileContext.getScope();
         ValueType resultType = scope.getType(functionName);
         if (resultType == null) {
            throw new IllegalStateException("Unable to find function (or argument types are not matching):" + functionName);
         } else {
            isConstant &= scope.isConstant(functionName);
            if (isConstant) {
               List<ExecutionContext.Instruction> instructionList = compileContext.getInstructions();
               ExecutionContext executionContext = compileContext.getExecutionContext();
               instructionList.clear();

               for (int i = firstArgument; i < len; i++) {
                  operandStack.get(i).genCode(instructionList, null);
               }

               instructionList.add(ExecutionContext.genCALL(functionName, argumentCount, null));
               ValueType ret = executionContext.execute(instructionList, scope);
               if (ret == ValueType.VOID) {
                  throw new IllegalStateException("Failed to evaluate constant function AST");
               } else {
                  operandStack.setSize(firstArgument - 1);
                  operandStack.push(ASTOperand.createFromOperand(functionNameAST.getToken(), functionNameAST.getTokenPosition(), executionContext.top()));
               }
            } else {
               ASTOperatorFunctionCall function = new ASTOperatorFunctionCall(resultType, functionName, functionNameAST.getTokenPosition());

               for (int i = firstArgument; i < len; i++) {
                  function.addArgument(operandStack.get(i));
               }

               operandStack.setSize(firstArgument - 1);
               operandStack.push(function);
            }
         }
      }
   }
}
